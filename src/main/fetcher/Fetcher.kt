package fetcher

import LocalConfig
import com.github.kittinunf.fuel.httpGet
import fetcher.cache.CacheStore
import fetcher.exception.FetcherException
import fetcher.result.FetchResult
import java.util.logging.Logger

class Fetcher (val localConfig: LocalConfig) {
    private val cacheStore = CacheStore(localConfig.fetcherCacheSizeMaxMb)
    val logger = Logger.getLogger(this.javaClass.name)

    fun getApiResultByChangeId(changeId: String): FetchResult {
        try {
            val cachedEntry = cacheStore.getByChangeId(changeId)
            if (cachedEntry != null) {
                return FetchResult(true, 200, cachedEntry.rawResponseData, 0, true)
            }

            val beforeRequestMs = System.currentTimeMillis()
            val (request, response, result) = localConfig.fetcherApiUrl.httpGet(listOf("next_change_id" to changeId))
                    .timeout(localConfig.fetcherTimeout)
                    .timeoutRead(localConfig.fetcherTimeout)
                    .response()
            val afterRequestMs = System.currentTimeMillis()
            val requestTimeMs = (afterRequestMs - beforeRequestMs).toInt()

            return if (response.statusCode != 200) {
                FetchResult(false, response.statusCode, response.responseMessage, requestTimeMs, false)
            } else {
                cacheStore.append(changeId, response.responseMessage)
                FetchResult(true, response.statusCode, response.responseMessage, requestTimeMs, false)
            }
        } catch (ex: Exception) {
            throw FetcherException("An internal fetcher error has occured: ${ex.message}", ex)
        }
    }
}