package com.kakao.iron.util

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.kakao.iron.R
import com.kakao.iron.ui.storage.StorageData
import com.kakao.kakaolink.v2.KakaoLinkResponse
import com.kakao.kakaolink.v2.KakaoLinkService
import com.kakao.message.template.*
import com.kakao.network.ErrorResult
import com.kakao.network.callback.ResponseCallback
import com.kakao.network.storage.ImageUploadResponse
import java.io.File
import java.io.FileOutputStream

object KakaoLinkProvider {

    private const val KAKAO_BASE_LINK = "https://developers.kakao.com"
    const val KAKAO_EVENT_CODE = "1000"

    fun sendKakaoLink(context: Context, data: StorageData) {
        when(data.form) {
            0 -> {
                val params = makeTemplate(context, data.imageUrl, data)
                sendMessage(context, params)
            }
            1 -> {
                val bitmap = decodeSampledBitmap(data.filePath, 200, 200)
                val file = File(context.cacheDir, "send.png")
                val stream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.close()

                KakaoLinkService.getInstance()
                    .uploadImage(context, true, file, object : ResponseCallback<ImageUploadResponse>() {
                        override fun onSuccess(result: ImageUploadResponse?) {
                            val url = result?.original?.url ?: "empty"
                            val params = makeTemplate(context, url, data)
                            sendMessage(context, params)
                        }
                        override fun onFailure(errorResult: ErrorResult?) {
                            Log.e("KAKAO_IMAGE_UPLOAD", errorResult?.errorMessage ?: "error")
                        }
                    })
            }
        }
    }

    private fun sendMessage(
        context: Context,
        params: TemplateParams
    ) {
        KakaoLinkService.getInstance()
            .sendDefault(context, params, object : ResponseCallback<KakaoLinkResponse>() {
                override fun onFailure(errorResult: ErrorResult) {
                    Log.e("KAKAO_API", "카카오링크 공유 실패: $errorResult")
                }
                override fun onSuccess(result: KakaoLinkResponse) {
                    Log.e("KAKAO_API", "카카오링크 공유 성공")

                    // 카카오링크 보내기에 성공했지만 아래 경고 메시지가 존재할 경우 일부 컨텐츠가 정상 동작하지 않을 수 있습니다.
                    Log.e("KAKAO_API", "warning messages: " + result.warningMsg)
                    Log.e("KAKAO_API", "argument messages: " + result.argumentMsg)
                }
            })
    }

    private fun makeTemplate(
        context: Context,
        url: String,
        data: StorageData
    ) : FeedTemplate {
        val text = replaceLine(data.text)
        val label = replace(data.label)
        return FeedTemplate
            .newBuilder(
                ContentObject.newBuilder(
                    text,
                    url,
                    LinkObject.newBuilder()
                        .setWebUrl(KAKAO_BASE_LINK)
                        .setMobileWebUrl(KAKAO_BASE_LINK)
                        .build()
                )
                    .setDescrption(label)
                    .setImageWidth(data.imageWidth)
                    .setImageHeight(data.imageHeight)
                    .build()
            )
            .addButton(
                ButtonObject(
                    context.getString(R.string.text_move_storage), LinkObject.newBuilder()
                        .setWebUrl(KAKAO_BASE_LINK)
                        .setMobileWebUrl(KAKAO_BASE_LINK)
                        .setAndroidExecutionParams("event=${KAKAO_EVENT_CODE}")
                        .build()
                ))
            .build()
    }

    private fun replace(list: List<String>): String {
        val sb = StringBuilder()
        list.toString()
            .replace("[^\uAC00-\uD7A3]".toRegex(), " ")
            .split(" ")
            .filter { it.isNotBlank() && it.isNotEmpty() }
            .map { sb.append("#$it ") }
        return sb.toString()
    }

    private fun replaceLine(list: List<String>): String {
        val sb = StringBuilder()
        val map = linkedMapOf<String, Int>()
        list.forEach {
            if(map[it] == null) map[it] = 1
            else map[it] = map[it]?.plus(1) as Int
        }
        map.keys
            .map { it.replace("[^\uAC00-\uD7A3]".toRegex(), " ").trim() }
            .filter { it.length <= 10 && it.isNotEmpty() }
            .map { sb.append("#$it ") }
        return sb.toString()
    }
}