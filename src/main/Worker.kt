import exception.WorkerException
import fetcher.Fetcher
import fetcher.exception.FetcherException
import fetcher.result.FetchResult
import jdk.nashorn.internal.runtime.ParserException
import metrics.MetricsService
import parser.Parser
import parser.result.ParserResult
import writer.Writer
import writer.result.WriterResult
import java.util.logging.Logger

class Worker(
        private val fetcher: Fetcher,
        private val context: WorkerContext,
        private val parser: Parser,
        private val writer: Writer,
        private val metrics: MetricsService
) {
    val logger = Logger.getLogger(this.javaClass.name)

    fun runOnce() {
        val fetchResult = getFetchResultFromContext()

        if (fetchResult.isSuccessful) {
            val parsedResult = getParserResultFromContext(fetchResult)
            val writerResult = getWriterResultFromContext(parsedResult)

            if (writerResult.isSuccessful) {
                context.currentChangeId = parsedResult.nextChangeId
            }
        }
    }

    fun loop() {
        while (true) {
            runOnce()
        }
    }

    private fun getFetchResultFromContext(): FetchResult {
        try {
            val fetchResult = fetcher.getApiResultByRemoteId(context.currentChangeId)
            metrics.appendBytesRead(fetchResult.bytesRead)

            if (fetchResult.isSuccessful) {
                metrics.appendFetchSuccess()
            } else {
                metrics.appendFetchFailureFromApi(fetchResult.remoteHttpCode)
            }

            return fetchResult
        } catch (ex: FetcherException) {
            logger.warning("Failed to process fetched result successfully: ${ex.message}")

            metrics.appendFetchFailureDueToLocal()

            throw WorkerException(ex)
        }
    }

    private fun getParserResultFromContext(fetchResult: FetchResult): ParserResult {
        try {
            val parserResult = parser.parse(fetchResult.rawData)

            metrics.appendParseSuccess()

            return parserResult
        } catch (ex: ParserException) {
            logger.warning("Failed to parse result successfully: ${ex.message}")

            metrics.appendParseFailure()

            throw WorkerException(ex)
        }
    }

    private fun getWriterResultFromContext(parsedResult: ParserResult): WriterResult {
        try {
            val writerResult = writer.writeChangeIdResult(context.currentChangeId, parsedResult.parsedData)

            if (writerResult.isSuccessful) {
                metrics.appendWriterSuccess()
            } else {
                metrics.appendFetchFailureFromApi(writerResult.remoteHttpCode)
            }

            return writerResult
        } catch (ex: ParserException) {
            logger.warning("Failed to write parsed result successfully: ${ex.message}")

            metrics.appendWriterFailureDueToLocal()

            throw WorkerException(ex)
        }
    }
}