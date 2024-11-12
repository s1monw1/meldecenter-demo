package de.swirtz.lwdemo.service.reporting

import de.swirtz.lwdemo.domain.RVReport
import de.swirtz.lwdemo.domain.Report
import de.swirtz.lwdemo.domain.ReportType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
* REDUCED SCOPE: See [KVReportService] for detailed information.
 *
 * This service assumes that all reporting succeeds.
*/
@Service
class RVReportService : ReportEndpoint<RVReport> {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun report(reports: List<RVReport>): ReportingResult {
        return ReportingResult(reports.map { ReportingResultEntry(it.reportId!!.id, true) })
    }

    override fun reportAny(reports: List<Report<*>>): ReportingResult {
        val rvReports = reports.filterIsInstance<RVReport>()
        if (rvReports.size != reports.size) {
            throw IllegalArgumentException("reports contain types that cannot be processed.")
        }
        return report(rvReports)
    }

    override fun getReportType() = ReportType.SV_RV

}
