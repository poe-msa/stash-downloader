package fetcher.cache

import java.util.*

class CacheStoreEntry(val rawResponseData: String, val changeId: String, val dateCreated: Date) {
    val size = changeId.length
}