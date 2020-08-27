package com.kakao.iron.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.util.Log

fun exifOrientationToDegrees(
    exifOrientation: Int
): Int {
    return when (exifOrientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }
}

fun rotate(
    bitmap: Bitmap,
    degree: Float
): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(degree)
    return Bitmap.createBitmap(
        bitmap,
        0,
        0,
        bitmap.width,
        bitmap.height,
        matrix,
        true
    )
}

fun calculateInSampleSize(
    reqWidth: Int,
    reqHeight: Int
): Int {
    val (height: Int, width: Int) = 3024 to 4032
    Log.e("$height", "$width")
    var inSampleSize = 1
    if(height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width/ 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize / 2
}

fun decodeSampledBitmap(
    fileName: String,
    reqWidth: Int,
    reqHeight: Int
): Bitmap {
    return BitmapFactory.Options().run {
        inJustDecodeBounds = true
        BitmapFactory.decodeFile(fileName, this)
        inSampleSize = calculateInSampleSize(reqWidth, reqHeight)
        inJustDecodeBounds = false
        BitmapFactory.decodeFile(fileName, this)
        val exif = ExifInterface(fileName)
        val exifOrientation: Int = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val exifDegree: Int = exifOrientationToDegrees(exifOrientation)
        val bitmap = BitmapFactory.decodeFile(fileName, this)
        when(exifDegree) {
            0 -> bitmap
            else -> rotate(bitmap, exifDegree.toFloat())
        }
    }
}