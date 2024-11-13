package de.swirtz.lwdemo.service.dispatching

import de.swirtz.lwdemo.domain.Report
import de.swirtz.lwdemo.domain.ReportStatus.*
import de.swirtz.lwdemo.domain.ReportType
import de.swirtz.lwdemo.service.ReportsService
import de.swirtz.lwdemo.service.reporting.ReportEndpoint
import de.swirtz.lwdemo.service.reporting.ReportingResult
import de.swirtz.lwdemo.service.reporting.ReportingResultEntry
import de.swirtz.lwdemo.service.resolving.ReportsResolver
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import kotlin.collections.component1
import kotlin.collections.component2

/**
 *
 * Disclaimer: This scheduling implementation is naive and would not work as expected in a productive environment with multiple instances of this service
 * running. To keep it simple this is acceptable for the demo application. A more sophisticated approach would move the scheduling
 * logic into the environment (e.g. Kubernetes crons) or have a leader node elected.
 *
 */
@Service
class ReportsDispatcher(
    private val reportsService: ReportsService,
    /* Inject a list of [ReportEndpoint] so we know which report types can be dispatched to downstream services */
    private val reportingEndpoints: List<ReportEndpoint<*>>,
    private val reportsResolver: ReportsResolver,
    private val properties: DispatcherProperties
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(initialDelay = 10, fixedRate = 20, timeUnit = TimeUnit.SECONDS)
    fun triggerReportDispatching() {
        reportingEndpoints.forEach { endpoint ->
            val reportType = endpoint.getReportType()
            val latestReportEntities = reportsService.getLatestReportsByType(
                reportType,
                listOf(AWAITING_SEND),
                properties.batchSize
            )
            if (latestReportEntities.isEmpty()) {
                logger.info("No reports to process for $reportType")
                // local return: skip current iteration and continue with next reportType
                return@forEach
            }
            val latestReports: Map<Int, Report<*>> = latestReportEntities
                .map { reportsResolver.convertFromPersistence(it) }
                .associateBy { it.reportId?.id ?: throw IllegalStateException("ReportID should not be null.") }

            latestReports.forEach { (_, report) ->
                reportsService.updateReportStatus(report, IN_TRANSIT)
            }

            val result = reportToEndpoint(latestReports, endpoint, reportType)
            if (result == null) {
                logger.error("Reporting failed for all reports ${latestReports.map { it.value.reportId?.id }}")

                latestReports.forEach { (_, report) ->
                    reportsService.updateReportStatus(report, FAILED)
                }
            } else {
                result.reports.forEach {
                    val reported = latestReports.getValue(it.reportId)
                    handleReportingResult(it, reported)
                }
            }
        }
    }

    private fun reportToEndpoint(
        latestReports: Map<Int, Report<*>>,
        endpoint: ReportEndpoint<*>,
        reportType: ReportType
    ): ReportingResult? =
        try {
            endpoint.reportAny(latestReports.values.toList()).also {
                logger.info("reporting results for $reportType: $it")
            }
        } catch (e: Exception) {
            logger.error("Exception while reporting", e)
            null
        }

    private fun handleReportingResult(result: ReportingResultEntry, report: Report<*>) {
        val newStatus = if (result.successfulReporting) SENT else FAILED
        reportsService.updateReportStatus(report, newStatus)
    }

}

@ConfigurationProperties("services.dispatcher")
class DispatcherProperties(val batchSize: Int)
