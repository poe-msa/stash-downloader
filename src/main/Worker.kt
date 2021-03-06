import exception.WorkerException
import fetcher.Fetcher
import fetcher.exception.FetcherException
import fetcher.result.FetchResult
import metrics.MetricsService
import parser.Parser
import parser.exception.ParserException
import parser.result.ParserResult
import writer.Writer
import writer.exception.WriterException
import writer.result.WriterResult
import java.util.logging.Logger

class Worker(
        private val fetcher: Fetcher,
        private val context: WorkerContext,
        private val parser: Parser,
        private val writer: Writer,
        private val metrics: MetricsService,
        private val localConfig: LocalConfig
) {
    val logger = Logger.getLogger(this.javaClass.name)

    fun runOnce() {
        try {
            logger.info("Starting work for change ID '${context.currentChangeId}'.")
            val fetchResult = getFetchResultFromContext()

            if (fetchResult.isSuccessful) {
                val parsedResult = getParserResultFromContext(fetchResult)
                val writerResult = getWriterResultFromContext(parsedResult)

                if (writerResult.isSuccessful) {
                    context.currentChangeId = parsedResult.nextChangeId
                }
            }
        } catch (ex: WorkerException) {
            throw ex
        }
    }

    fun loop() {
        while (true) {
            try {
                runOnce()
            } catch (ex: WorkerException) {
                ex.printStackTrace()
            }

            Thread.sleep(localConfig.workerRunDelayMs) // ToDo: Figure out how to make wait work
        }
    }

    private fun getFetchResultFromContext(): FetchResult {
        try {
            val fetchResult = fetcher.getApiResultByChangeId(context.currentChangeId)

            if (fetchResult.isSuccessful) {
                if (fetchResult.fromCache) {
                    logger.info("Fetch successful for change ID '${context.currentChangeId}' (loaded from cache).")
                } else {
                    logger.info("Fetch successful for change ID '${context.currentChangeId}' (${fetchResult.timeMs} ms taken).")
                }
                metrics.appendFetchSuccess()

                if (!fetchResult.fromCache) {
                    metrics.appendFetchRequestTime(fetchResult.timeMs)
                }
            } else {
                logger.warning("Fetch failed for change ID '${context.currentChangeId}' due to remote error. HTTP code: ${fetchResult.remoteHttpCode}.")
                metrics.appendFetchFailureFromApi(fetchResult.remoteHttpCode)

                throw WorkerException("Cannot continue: API fetch failed", null)
            }

            return fetchResult
        } catch (ex: FetcherException) {
            logger.warning("Failed to process fetched result successfully: ${ex.message}")

            metrics.appendFetchFailureDueToLocal()

            throw WorkerException(null, ex)
        }
    }

    private fun getParserResultFromContext(fetchResult: FetchResult): ParserResult {
        try {
            val parserResult = parser.parse(fetchResult.rawData)

            metrics.appendParseSuccess()
            metrics.appendParseTime(parserResult.timeMs)

            logger.info("Parsed payload of change ID '${context.currentChangeId}' successfully (${parserResult.timeMs} ms taken).")

            return parserResult
        } catch (ex: ParserException) {
            logger.warning("Failed to parse result successfully: ${ex.message}")

            metrics.appendParseFailure()

            throw WorkerException(null, ex)
        }
    }

    private fun getWriterResultFromContext(parsedResult: ParserResult): WriterResult {
        try {
            val writerResult = writer.writeChangeIdResult(context.currentChangeId, parsedResult.parsedData)

            if (writerResult.isSuccessful) {
                logger.info("Write successful change ID '${context.currentChangeId}'. Assigned file identifier: '${writerResult.fileIdentifier}' (${writerResult.timeMs}ms taken)")
                metrics.appendWriterSuccess()
                metrics.appendWriteRequestTime(writerResult.timeMs)
            } else {
                logger.warning("Write failed for change ID '${context.currentChangeId}' due to remote error. HTTP code: ${writerResult.remoteHttpCode}.")
                metrics.appendWriterFailureFromApi(writerResult.remoteHttpCode)

                throw WorkerException("Cannot continue: write failed", null)
            }

            return writerResult
        } catch (ex: WriterException) {
            logger.warning("Failed to write parsed result successfully: ${ex.message}")

            metrics.appendWriterFailureDueToLocal()

            throw WorkerException(null, ex)
        }
    }
}