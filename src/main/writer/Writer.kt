package writer

import LocalConfig
import com.google.auth.Credentials
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.*
import writer.result.WriterResult
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.util.zip.GZIPOutputStream

class Writer(val localConfig: LocalConfig) {
    fun writeChangeIdResult(changeId: String, data: String): WriterResult {
        val beforeUploadMs = System.currentTimeMillis()
        val credentials = GoogleCredentials.fromStream(FileInputStream(localConfig.googleAuthKeyFilename))
        val storage = StorageOptions.newBuilder().setCredentials(credentials).build().service

        // ToDo: Bucket could be created here
        val bucket = storage.get(localConfig.writerBucketName)
        val fileIdentifier = getIdentifierFromChangeId(changeId)
        val afterUploadMs = System.currentTimeMillis()
        val timeUploadMs = (afterUploadMs - beforeUploadMs).toInt()

        val outputStream = ByteArrayOutputStream()
        val gzipStream = GZIPOutputStream(outputStream)
        gzipStream.write(data.toByteArray())
        gzipStream.close()
        val bytes = outputStream.toByteArray()

        bucket.create(fileIdentifier, bytes, "application/octet-stream")

        val blobId = BlobId.of(localConfig.writerBucketName, fileIdentifier)
        val blob = storage.get(blobId)
        blob.toBuilder().setContentEncoding("gzip").build().update()

        return WriterResult(true, 200, "", fileIdentifier, timeUploadMs)
    }

    private fun getIdentifierFromChangeId(changeId: String): String {
        return when (changeId) {
            "" -> "initial.json"
            else -> "${changeId}.json"
        }
    }
}