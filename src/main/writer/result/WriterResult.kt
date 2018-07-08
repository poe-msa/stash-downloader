package writer.result

class WriterResult(val isSuccessful: Boolean, val remoteHttpCode: Int, val rawResponse: String, val fileIdentifier: String, val timeMs: Int) {
}