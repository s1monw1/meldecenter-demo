package de.swirtz.lwdemo.service

import de.swirtz.lwdemo.data.ReportsRepository
import de.swirtz.lwdemo.data.model.ReportEntity
import de.swirtz.lwdemo.domain.Report
import de.swirtz.lwdemo.domain.ReportStatus
import de.swirtz.lwdemo.domain.ReportType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

data class RequestedReportStatus(
    val id: Int,
    val status: String,
)

/**
 * Service offering CRUD operations for [Report]s.
 */
@Service
class ReportsService(
    private val reportsRepository: ReportsRepository
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun getLatestReportsByType(
        type: ReportType,
        allowedStatus: List<ReportStatus> = listOf(ReportStatus.AWAITING_SEND),
        limit: Int = 3
    ): List<ReportEntity> {
        return reportsRepository.findByReportTypeLastXUnsent(
            type.toString(),
            allowedStatus.map { it.toString() },
            limit
        )
    }

    fun saveReport(report: Report<*>): Int {
        return reportsRepository.save(report.toEntity()).id
    }

    fun updateReportStatus(report: Report<*>, newStatus: ReportStatus): Int {
        if (report.reportId?.id == null) {
            throw IllegalArgumentException("Report ID must be specified!")
        }
        val entity = reportsRepository.findById(report.reportId.id)
        val updated = entity.get().copy(status = newStatus)
        return reportsRepository.save(updated).id
    }

    fun getReportStatus(reportId: Int): RequestedReportStatus? {
        val findById = reportsRepository.findById(reportId)
        if (!findById.isPresent) {
            logger.error("Could not find report with id: $reportId")
            return null
        }
        val entity = findById.get()
        return RequestedReportStatus(entity.id, entity.status.description)
    }

}

fun Report<*>.toEntity(): ReportEntity = ReportEntity(
    reportingTime = LocalDateTime.now(),
    reportingClientId = UUID.randomUUID().toString(), // We should use something meaningful, e.g. from session/auth
    data = getReportDataAsMap(),
    contact = getContactDetailsAsMap(),
    type = reportType,
    status = ReportStatus.AWAITING_SEND
)
