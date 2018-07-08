import com.natpryce.konfig.*

class LocalConfig (config: Configuration) {
    val fetcherApiUrl = config[Key("fetcher.api_url", stringType)]
    val fetcherGzip = config[Key("fetcher.gzip", booleanType)]
    val fetcherInitialChangeId = config[Key("fetcher.initial_change_id", stringType)]

    val googleAuthKeyFilename = config[Key("google.auth_key_filename", stringType)]

    val metricsNamespace = config[Key("metrics.namespace", stringType)]
    val metricsProjectId = config[Key("metrics.project_id", stringType)]

    val workerRunDelayMs = config[Key("worker.run_delay_ms", longType)]

    val writerBucketName = config[Key("writer.bucket_name", stringType)]
    val writerGzipFiles = config[Key("writer.gzip_files", booleanType)]
}