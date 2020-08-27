package com.kakao.iron.util

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.kakao.iron.BuildConfig
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.buffer
import okio.sink
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
class HttpInterceptor(
    val context: Context
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var retryCount = 0
        val maxRetryTimes = 3
        val formatDate = SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS", Locale.KOREA)
        val request: Request = chain.request()
        val nanoStartTime = System.nanoTime()
        val startTime = formatDate.format(Date(System.currentTimeMillis()))
        var response = chain.proceed(
            chain.request()
                .newBuilder()
                .header("Authorization", BuildConfig.SEARCH_API_KEY)
                .build()
        )

        while (!response.isSuccessful && retryCount < maxRetryTimes) {
            Log.e("intercept", "Request is not successful - $retryCount times")
            retryCount++

            response.close()
            response = chain.proceed(
                chain.request()
                    .newBuilder()
                    .header("Authorization", BuildConfig.SEARCH_API_KEY)
                    .build()
            )
        }

        when(response.isSuccessful) {
            true -> {
                val nanoEndTime = System.nanoTime()
                val diff = (nanoEndTime - nanoStartTime) / 1e6
                val url = request.url().toString()
                val header = request.headers().toString()
                val code = response.code().toString() + " " + response.message()
                val endTime = formatDate.format(Date(System.currentTimeMillis()))
                val sb = StringBuilder().apply {
                    append("request 시각 -> \n").append("$startTime \n\n")
                        .append("response 시각 -> \n").append("$endTime \n\n")
                        .append("총 걸린 시간 -> \n").append("$diff ns \n\n")
                        .append("request url -> \n").append("$url \n\n")
                        .append("request header -> \n").append(header+"\n")
                        .append("response code -> \n").append(code)
                }

                writeToFile(context, sb)
            }
            false -> {
                Log.e("intercept", "Request is failed")
            }
        }
        return response
    }

    private fun writeToFile(context: Context, sb: StringBuilder) {
        runCatching {
            val formatDate = SimpleDateFormat("yyyyMMdd_hhmmssSSS", Locale.KOREA)
            val date = Date(System.currentTimeMillis())
            val mFileName = formatDate.format(date)
            val file = File(context.externalCacheDir, "log_$mFileName.txt")
            file.let{
                it.createNewFile()
                it.sink().buffer().use { sink ->
                    sink.write(
                        sb.toString().toByteArray()
                    )
                }
            }
        }.onFailure {
            throw it.cause ?: Exception()
        }
    }
}