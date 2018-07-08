package fetcher.result

class FetchResult(val isSuccessful: Boolean, val remoteHttpCode: Int, val rawData: String, val timeMs: Int, val fromCache: Boolean) {
}