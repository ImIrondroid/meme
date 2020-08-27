package com.kakao.iron.ui.camera

import android.os.Bundle
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.*
import android.media.ExifInterface
import android.media.ImageReader
import android.os.*
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import androidx.core.app.ActivityCompat
import com.kakao.iron.R
import com.kakao.iron.databinding.ActivityCameraBinding
import com.kakao.iron.ui.base.BaseActivity
import com.kakao.iron.util.exifOrientationToDegrees
import com.kakao.iron.util.extension.showToast
import com.kakao.iron.util.rotate
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class CameraActivity : BaseActivity<ActivityCameraBinding>() {

    private lateinit var file: File
    private lateinit var cameraId: String
    private lateinit var cameraCaptureSession: CameraCaptureSession
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private lateinit var imageDimensions: Size
    private lateinit var imageReader: ImageReader

    private var cameraDevice: CameraDevice? = null
    private var mBackgroundHandler: Handler? = null
    private var mBackgroundThread: HandlerThread? = null
    private val orientations = SparseIntArray().apply {
        append(Surface.ROTATION_0, 90)
        append(Surface.ROTATION_90, 0)
        append(Surface.ROTATION_180, 270)
        append(Surface.ROTATION_270, 180)
    }

    override fun getLayoutId(): Int = R.layout.activity_camera

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        back.setOnClickListener {
            onBackPressed()
        }

        take.setOnClickListener {
            takePicture()
        }
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        if (texture.isAvailable) {
            kotlin.runCatching {
                openCamera()
            }.onFailure {
                it.printStackTrace()
            }
        } else {
            texture.surfaceTextureListener = textureListener
        }
    }

    override fun onPause() {
        stopBackgroundThread()
        super.onPause()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        super.onBackPressed()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                showToast("Sorry, Camera permission is necessary")
                finish()
            }
        }
    }

    private fun openCamera() {
        val context: Context = this
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = manager.cameraIdList[0]
        val characteristics = manager.getCameraCharacteristics(cameraId)
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        imageDimensions = map?.getOutputSizes(SurfaceTexture::class.java)?.get(0)
            ?: Size(0, 0)

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
            return
        }

        manager.openCamera(cameraId, stateCallback, null)
    }

    private fun takePicture() {
        if (cameraDevice == null) return
        else {
            val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val characteristics = cameraDevice?.id?.let { manager.getCameraCharacteristics(it) }
            val jpegSize: Array<Size> =
                characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    ?.getOutputSizes(ImageFormat.JPEG) ?: arrayOf()
            var width = 640
            var height = 480
            if (jpegSize.isNotEmpty()) {
                width = jpegSize[0].width
                height = jpegSize[0].height
            }

            imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
            val outputSurfaces = arrayListOf<Surface>().apply {
                add(imageReader.surface)
                add(Surface(texture.surfaceTexture))
            }

            val captureBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureBuilder?.addTarget(imageReader.surface)
            captureBuilder?.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

            val rotation = windowManager.defaultDisplay.rotation
            captureBuilder?.set(CaptureRequest.JPEG_ORIENTATION, orientations.get(rotation))

            val formatDate = SimpleDateFormat("yyyyMMdd_hhmmssSSS", Locale.KOREA)
            val date = Date(System.currentTimeMillis()).time
            val fileName = "camera_${date}.jpg"
            file = File(cacheDir, fileName)

            val readerListener = ImageReader.OnImageAvailableListener {
                kotlin.runCatching {
                    val image = it.acquireLatestImage()
                    val buffer = image.planes[0].buffer
                    val byte = ByteArray(buffer.capacity())
                    buffer.get(byte)
                    save(byte)
                    image.close()
                }.onFailure {
                    it.printStackTrace()
                }
            }

            imageReader.setOnImageAvailableListener(readerListener, mBackgroundHandler)

            val captureListener = object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    super.onCaptureCompleted(session, request, result)
                    kotlin.runCatching {
                        createCameraPreview()
                    }
                }
            }

            cameraDevice?.createCaptureSession(
                outputSurfaces,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigureFailed(p0: CameraCaptureSession) {}
                    override fun onConfigured(p0: CameraCaptureSession) {
                        kotlin.runCatching {
                            p0.capture(
                                captureBuilder?.build()!!,
                                captureListener,
                                mBackgroundHandler
                            )
                        }
                    }
                },
                mBackgroundHandler
            )
        }
    }

    private fun save(byteArray: ByteArray) {
        kotlin.runCatching {
            file.createNewFile()
            val outputStream = FileOutputStream(file)
            outputStream.write(byteArray)
            outputStream.close()

            val exif = ExifInterface(file.path)
            val exifOrientation: Int = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (val exifDegree: Int = exifOrientationToDegrees(exifOrientation)) {
                0 -> {
                    val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    effect_image.setImageBitmap(bitmap)
                }
                else -> {
                    val mBitmap: Bitmap = BitmapFactory.decodeFile(file.path)
                    val rotateBitmap = rotate(mBitmap, exifDegree.toFloat())
                    effect_image.setImageBitmap(rotateBitmap)
                }
            }
            outputStream.close()
        }.onFailure {
            it.printStackTrace()
        }
    }

    private fun createCameraPreview() {
        val texture = texture.surfaceTexture
        texture?.setDefaultBufferSize(imageDimensions.width, imageDimensions.height)
        val surface = Surface(texture)
        captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)!!
        captureRequestBuilder.addTarget(surface)
        cameraDevice?.createCaptureSession(
            listOf(surface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigureFailed(p0: CameraCaptureSession) {
                    showToast("Configuration Changed")
                }

                override fun onConfigured(p0: CameraCaptureSession) {
                    if (cameraDevice == null) return
                    else {
                        cameraCaptureSession = p0
                        kotlin.runCatching {
                            updatePreview()
                        }.onFailure {
                            it.printStackTrace()
                        }
                    }
                }
            },
            null
        )
    }

    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread(THREAD_NAME)
        mBackgroundThread?.start()
        mBackgroundHandler = Handler(mBackgroundThread?.looper!!)
    }

    private fun stopBackgroundThread() {
        mBackgroundHandler = null
        mBackgroundThread?.let {
            it.quitSafely()
            it.join()
            null
        }
    }

    private fun updatePreview() {
        if (cameraDevice == null) return
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        cameraCaptureSession.setRepeatingRequest(
            captureRequestBuilder.build(),
            null,
            mBackgroundHandler
        )
    }

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(p0: CameraDevice) {
            cameraDevice = p0
            kotlin.runCatching {
                createCameraPreview()
            }.onFailure {
                it.printStackTrace()
            }
        }

        override fun onDisconnected(p0: CameraDevice) {
            cameraDevice?.close()
        }

        override fun onError(p0: CameraDevice, p1: Int) {
            cameraDevice?.let {
                it.close()
                null
            }
        }
    }

    private val textureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {}
        override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {}
        override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
            return true
        }

        override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
            kotlin.runCatching {
                openCamera()
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    companion object {
        const val PERMISSION_REQUEST_CODE = 101
        const val THREAD_NAME = "CameraWorkerThread"
    }
}