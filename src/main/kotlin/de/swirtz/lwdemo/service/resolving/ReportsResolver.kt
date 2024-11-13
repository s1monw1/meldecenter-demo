package de.swirtz.lwdemo.service.resolving

import de.swirtz.lwdemo.controller.dto.ReportDTO
import de.swirtz.lwdemo.data.model.ReportEntity
import de.swirtz.lwdemo.domain.Report
import de.swirtz.lwdemo.domain.ReportType
import org.springframework.stereotype.Service

/**
 * Main resolver to translate [ReportDTO] into a known [Report] type. Based on the [ReportDTO.type] it will
 * delegate to a registered [DeliveredReportResolver] and use its [DeliveredReportResolver.convertFromClient] method to handle
 * the conversion.
 * The resolver also handles conversion from persistence types [ReportEntity] into a know [Report] type using [DeliveredReportResolver.convertFromPersistence].
 *
 * If a required [DeliveredReportResolver] cannot be found, an [IllegalStateException] is thrown.
 */
@Service
class ReportsResolver(
    typeResolvers: List<DeliveredReportResolver<*>>
) {

    private val resolversByType: Map<ReportType, DeliveredReportResolver<*>> =
        typeResolvers.associateBy(DeliveredReportResolver<*>::resolvesFor)

    fun convertFromTransfer(report: ReportDTO): Report<*> {
        val resolver = resolversByType[report.type]
            ?: throw IllegalStateException("No registered resolver found for report type ${report.type}")

        return resolver.convertFromClient(report)
    }

    fun convertFromPersistence(report: ReportEntity): Report<*> {
        val resolver = resolversByType[report.type]
            ?: throw IllegalStateException("No registered resolver found for report type ${report.type}")

        return resolver.convertFromPersistence(report)
    }

}
