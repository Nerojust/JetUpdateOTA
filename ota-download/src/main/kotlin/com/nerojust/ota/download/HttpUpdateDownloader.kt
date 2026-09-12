package com.nerojust.ota.download

import com.nerojust.ota.core.DownloadProgress
import com.nerojust.ota.core.OTAException
import com.nerojust.ota.core.UpdateDownloader
import com.nerojust.ota.core.model.UpdateManifest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest

class HttpUpdateDownloader(private val httpClient: OkHttpClient) : UpdateDownloader {
    override fun download(
        manifest: UpdateManifest,
        targetDir: File,
    ): Flow<DownloadProgress> =
        flow {
            val targetFile = File(targetDir, "update_${manifest.versionCode}.apk")
            if (targetFile.exists() && targetFile.length() > manifest.fileSize) {
                targetFile.delete()
            }
            val startOffset = if (targetFile.exists()) targetFile.length() else 0L

            val requestBuilder = Request.Builder().url(manifest.apkUrl)
            if (startOffset > 0) {
                requestBuilder.addHeader("Range", "bytes=$startOffset-")
            }

            httpClient.newCall(requestBuilder.build()).execute().use { response ->
                if (!response.isSuccessful) {
                    emit(DownloadProgress.Failed(OTAException.NetworkError(IOException("HTTP ${response.code}"))))
                    return@flow
                }
                val body = response.body
                if (body == null) {
                    emit(DownloadProgress.Failed(OTAException.NetworkError(IOException("Empty response body"))))
                    return@flow
                }

                val append = response.code == 206 && startOffset > 0
                var downloaded = if (append) startOffset else 0L
                FileOutputStream(targetFile, append).use { output ->
                    body.byteStream().use { input ->
                        val buffer = ByteArray(8192)
                        while (true) {
                            val read = input.read(buffer)
                            if (read == -1) break
                            output.write(buffer, 0, read)
                            downloaded += read
                            emit(DownloadProgress.InProgress(downloaded, manifest.fileSize))
                        }
                    }
                }
            }

            if (!verifyChecksum(targetFile, manifest.checksum)) {
                targetFile.delete()
                emit(DownloadProgress.Failed(OTAException.ChecksumMismatch()))
                return@flow
            }

            emit(DownloadProgress.Complete(targetFile))
        }
            .flowOn(Dispatchers.IO)
            .catch { e ->
                val cause = e as? IOException ?: IOException(e)
                emit(DownloadProgress.Failed(OTAException.NetworkError(cause)))
            }

    private fun verifyChecksum(
        file: File,
        expected: String,
    ): Boolean {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        val actual = digest.digest().joinToString("") { "%02x".format(it) }
        return actual.equals(expected, ignoreCase = true)
    }
}

fun defaultUpdateDownloader(httpClient: OkHttpClient): UpdateDownloader = HttpUpdateDownloader(httpClient)
