package de.swirtz.lwdemo.data

import de.swirtz.lwdemo.data.model.ReportEntity
import de.swirtz.lwdemo.domain.ReportStatus
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

/**
 * JPA Repository to interact with records in `reports` table
 */
interface ReportsRepository : CrudRepository<ReportEntity, Int> {

    /**
     * Fetches reports with given [reportType] and one of the [allowedStatus], limited to a maximum of [limit].
     * The records are ordered by [ReportEntity.reportingTime] with the oldest being returned first following a FIFO approach.
     */
    @Query(
        value = "SELECT * FROM reports r WHERE r.type = ?1 AND r.status IN ?2 ORDER BY reporting_time LIMIT ?3",
        nativeQuery = true
    )
    fun findByReportTypeLastXUnsent(
        reportType: String,
        allowedStatus: List<String> = listOf(ReportStatus.AWAITING_SEND.toString()),
        limit: Int = 3
    ): List<ReportEntity>
}
