package fetcher.cache

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CacheStoreTest {
    private fun getPayloadOfSize(bytesSize: Int): String {
        return "a".repeat(bytesSize)
    }

    @Test
    fun test_givenEmptyStore_whenAppendChangeIdWithValidSizeData_thenGetChangeIdReturnsSomething() {
        val store = CacheStore(1)

        store.append("", getPayloadOfSize(100))

        val actual = store.getByChangeId("")

        assertThat(actual).isNotNull
    }

    @Test
    fun test_givenEmptyStore_whenAppendExactCacheSizeData_thenGetChangeIdReturnsSomething() {
        val store = CacheStore(1)

        store.append("", getPayloadOfSize(1024 * 1024))

        val actual = store.getByChangeId("")

        assertThat(actual).isNotNull
    }

    @Test
    fun test_givenEmptyStore_whenAppendTooLargeData_thenGetChangeIdReturnsNull() {
        val store = CacheStore(1)

        store.append("", getPayloadOfSize(1024 * 1024 + 1))

        val actual = store.getByChangeId("")

        assertThat(actual).isNull()
    }

    @Test
    fun test_given50PctFilledStore_whenAppendDataIsRemaining50Pct_thenGetChangeIdReturnsSomething() {
        val store = CacheStore(1)
        store.append("a", getPayloadOfSize(1024 * 512))

        store.append("actual", getPayloadOfSize(1024 * 512))

        val actual = store.getByChangeId("actual")

        assertThat(actual).isNotNull
    }

    @Test
    fun test_given50PctFilledStore_whenAppendDataIsRemaining50PctPlus1_thenGetChangeIdReturnsAddedEntry() {
        val store = CacheStore(1)
        store.append("initial", getPayloadOfSize(1024 * 512))

        store.append("actual", getPayloadOfSize(1024 * 512 + 1))

        val actual = store.getByChangeId("actual")

        assertThat(actual).isNotNull
    }

    @Test
    fun test_given50PctFilledStore_whenAppendDataIsRemaining50PctPlus1_thenGetChangeIdOfInitialReturnsNull() {
        val store = CacheStore(1)
        store.append("initial", getPayloadOfSize(1024 * 512))

        store.append("actual", getPayloadOfSize(1024 * 512 + 1))

        val actual = store.getByChangeId("initial")

        assertThat(actual).isNull()
    }

    @Test
    fun test_givenDataInStore_whenGetDataIsCalled_thenDataReturnedContainsPayload() {
        val expectedPayload = getPayloadOfSize(1024 * 512)

        val store = CacheStore(1)
        store.append("actual", expectedPayload)

        val actual = store.getByChangeId("actual")

        assertThat(actual!!.rawResponseData).isEqualTo(expectedPayload)
    }
}