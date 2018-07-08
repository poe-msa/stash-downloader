package fetcher.cache

import java.util.*
import kotlin.NoSuchElementException

// ToDo: Unit testable
class CacheStore (val maxSizeMb: Int) {
    var requests: List<CacheStoreEntry> = listOf()

    private fun getMaxSizeBytes(): Long {
        return maxSizeMb.toLong() * 1024 * 1024
    }

    fun getCurrentSizeBytes(): Int {
        return requests.sumBy { entry -> entry.size }
    }

    fun append(changeId: String, rawData: String) {
        val dataLength = rawData.length
        if (dataLength > getMaxSizeBytes()) {
            return // Stop here, data provided exceeds max cache size...
        }

        while (dataLength + getCurrentSizeBytes() > getMaxSizeBytes()) {
            requests = requests.drop(1)
        }

        requests += CacheStoreEntry(rawData, changeId)
    }

    fun getByChangeId(changeId: String): CacheStoreEntry? {
        return try {
            requests.first { request -> request.changeId == changeId }
        } catch (ex: NoSuchElementException) {
            null
        }
    }
}