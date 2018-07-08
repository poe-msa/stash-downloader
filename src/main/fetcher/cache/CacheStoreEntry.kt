package fetcher.cache

class CacheStoreEntry(val rawResponseData: String, val changeId: String) {
    val size = rawResponseData.length
}