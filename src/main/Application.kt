import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.overriding
import fetcher.Fetcher
import metrics.MetricsService
import org.kodein.di.Kodein
import org.kodein.di.generic.*
import org.kodein.di.newInstance
import parser.Parser
import writer.Writer
import java.io.File

// ToDo: Split parts that become too big into modules (see http://kodein.org/Kodein-DI/?5.2/getting-started#_separation)
// For now, this is too small and would lead to unnecessary work.
val kodein = Kodein {
    bind<LocalConfig>() with singleton { buildConfig() }
    bind<WorkerContext>() with singleton { WorkerContext(instance()) }

    bind<Fetcher>() with singleton { Fetcher(instance()) }

    bind<MetricsService>() with singleton { MetricsService() }

    bind<Parser>() with singleton { Parser() }

    bind<Writer>() with singleton { Writer(instance()) }
}

fun main(args: Array<String>) {
    val worker by kodein.newInstance { Worker(instance(), instance(), instance(), instance(), instance(), instance()) }
    worker.loop()
}

fun buildConfig(): LocalConfig {
    val rawConfig = EnvironmentVariables() overriding
        ConfigurationProperties.fromFile(File("local.properties")) overriding
        ConfigurationProperties.fromFile(File("defaults.properties"))

    return LocalConfig(rawConfig)
}