package fetcher.result

class FetchResult(val isSuccessful: Boolean, val remoteHttpCode: Short, val rawData: String, val bytesRead: Long) {
}