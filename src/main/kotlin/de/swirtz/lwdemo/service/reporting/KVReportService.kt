package de.swirtz.lwdemo.service.reporting

import de.swirtz.lwdemo.domain.KVReport
import de.swirtz.lwdemo.domain.Report
import de.swirtz.lwdemo.domain.ReportType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.random.Random
import kotlin.random.nextLong

/**
 * REDUCED SCOPE: I decided not to integrate mocked web services to communicate the reports to as it would increase the
 * scope of the exercise drastically without adding too much value. I hope that these implementations still show the most
 * relevant aspects of the expected scope.
 *
 * The service will randomly make reports fail to demonstrate error handling in the application.
 * The service will use custom delays when handling reports to again allow for some more realistic behavior.
 */
@Service
class KVReportService(
    private val config: ReportServiceConfiguration = ReportServiceConfiguration()
) : ReportEndpoint<KVReport> {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun report(reports: List<KVReport>): ReportingResult {
        val results = reports.map {
            logger.info("Report received with ID ${it.reportId?.id}")
            Thread.sleep(config.standardDelayMs.toLong())

            with(Random.Default) {
                if (config.randomDelayActive) {
                    val delayRange = 1000L..3000L
                    Thread.sleep(nextLong(delayRange))
                }
                val resultsInRandomError = config.failureRatePercentage > 0 &&
                        (nextDouble() * 100).toInt() <= config.failureRatePercentage
                val reportId = it.reportId?.id ?: -1
                if (resultsInRandomError) {
                    logger.error("Report handling randomly failed for ID ${it.reportId?.id}")
                    ReportingResultEntry(reportId, false, "random error generated", 400)
                } else {
                    logger.error("Report handling succeeded for ID ${it.reportId?.id}")
                    ReportingResultEntry(reportId, true)
                }
            }
        }
        return ReportingResult(results)

    }

    override fun reportAny(reports: List<Report<*>>): ReportingResult {
        val kvReports = reports.filterIsInstance<KVReport>()
        if (kvReports.size != reports.size) {
            throw IllegalArgumentException("reports contain types that cannot be processed.")
        }
        return report(kvReports)
    }

    override fun getReportType() = ReportType.SV_KV
}

//TODO this config would be better handled as part of the application context configuration
data class ReportServiceConfiguration(
    val failureRatePercentage: Int = 50,
    val standardDelayMs: Int = 500,
    val randomDelayActive: Boolean = true,
    val serviceAvailable: Boolean = true,
)
