package de.swirtz.lwdemo.service

import de.swirtz.lwdemo.data.ReportsRepository
import de.swirtz.lwdemo.domain.ClientContactDetails
import de.swirtz.lwdemo.domain.ClientPhoneNumber
import de.swirtz.lwdemo.domain.KVReport
import de.swirtz.lwdemo.domain.KVReportData
import de.swirtz.lwdemo.domain.ReportId
import de.swirtz.lwdemo.domain.ReportStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests are slow due to Testcontainers.
 */
@DataJpaTest
@ActiveProfiles("postgres")
class ReportsServiceTests(
    @Autowired private val reportsRepository: ReportsRepository
) {

    private val reportsService = ReportsService(reportsRepository)

    @BeforeEach
    fun `clear repo`() {
        reportsRepository.deleteAll()
    }

    @Test
    fun `report can be saved`() {
        val report = KVReport(
            KVReportData("test", LocalDate.now(), BigDecimal.valueOf(100)), ClientContactDetails(
                "test", "tester", "add",
                ClientPhoneNumber("1738888888")
            )
        )
        val id = reportsService.saveReport(report)
        assertTrue(id > 0)
    }

    @Test
    fun `report can be updated`() {
        val report = KVReport(
            KVReportData("test", LocalDate.now(), BigDecimal.valueOf(100)), ClientContactDetails(
                "test", "tester", "add",
                ClientPhoneNumber("1738888888")
            )
        )
        val id = reportsService.saveReport(report)
        assertEquals(ReportStatus.AWAITING_SEND.description, reportsService.getReportStatus(id)?.status)

        reportsService.updateReportStatus(report.copy(id = ReportId(id)), ReportStatus.FAILED)
        assertEquals(ReportStatus.FAILED.description, reportsService.getReportStatus(id)?.status)
    }
}
