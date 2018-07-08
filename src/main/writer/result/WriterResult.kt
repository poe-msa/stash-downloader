package writer.result

class WriterResult(val isSuccessful: Boolean, val remoteHttpCode: Short, val rawResponse: String, val fileIdentifier: String, val bytesWritten: Long) {
}