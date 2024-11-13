package de.swirtz.lwdemo.controller

import de.swirtz.lwdemo.ReportsRepositoryFake
import de.swirtz.lwdemo.controller.dto.ClientContactDetailsDTO
import de.swirtz.lwdemo.controller.dto.ReportDTO
import de.swirtz.lwdemo.createKVReportDTO
import de.swirtz.lwdemo.createPNUnifer
import de.swirtz.lwdemo.createRVReportDTO
import de.swirtz.lwdemo.domain.ReportStatus
import de.swirtz.lwdemo.domain.ReportType
import de.swirtz.lwdemo.service.ReportValidationException
import de.swirtz.lwdemo.service.ReportsService
import de.swirtz.lwdemo.service.resolving.KVReportResolver
import de.swirtz.lwdemo.service.resolving.RVReportResolver
import de.swirtz.lwdemo.service.resolving.ReportsResolver
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

/**
 * Integration test spinning up a [MeldecenterController] with a fake repository and otherwise real services to
 * simulate the reporting and status requests.
 */
class MeldecenterControllerIntegrationTests {
    private val pnUnifier = createPNUnifer()
    private val repo = ReportsRepositoryFake()
    private val controller = MeldecenterController(
        ReportsService(repo),
        ReportsResolver(listOf(KVReportResolver(pnUnifier), RVReportResolver(pnUnifier)))
    )

    @BeforeEach
    fun cleanup() {
        repo.deleteAll()
    }

    @Test
    fun `KV report can be stored`() {
        val dto = createKVReportDTO()
        val result = controller.report(dto)
        assertNotNull(result.body)
    }

    @Test
    fun `report with missing info results in client error`() {
        val dto = createRVReportDTO()
    }

    @Test
    fun `report with invalid phone number results in client error`() {
        val dto = createRVReportDTO(
            contact = ClientContactDetailsDTO(
                "test", "test", "test_add", "INVALID_PN"
            )
        )
        assertFailsWith<ReportValidationException> {
            controller.report(dto)
        }
    }

    @Test
    fun `report with invalid amount results in client error`() {
        val dto = createRVReportDTO(
            contact = ClientContactDetailsDTO(
                "test", "test", "test_add", "INVALID_PN"
            ),
            data = mapOf(
                "rvNumber" to "123",
                "date" to LocalDate.now().toString(),
                "amount" to "xy"
            ),
        )
        assertFailsWith<ReportValidationException> {
            controller.report(dto)
        }
    }

    @Test
    fun `RV report can be stored`() {
        val dto = ReportDTO(
            ReportType.SV_RV,
            data = mapOf(
                "rvNumber" to "123",
                "date" to LocalDate.now().toString(),
                "amount" to "1000"
            ),
            contact = ClientContactDetailsDTO(
                "test", "test", "test_add", "01731234567"
            )
        )
        val result = controller.report(dto)
        assertNotNull(result.body)
    }

    @Test
    fun `before storing, status cannot be retrieved`() {

        val result = controller.getReportStatus(1)

        assertEquals(HttpStatus.NOT_FOUND, result.statusCode)
    }

    @Test
    fun `status can be retrieved for existing report`() {
        val dto = ReportDTO(
            ReportType.SV_RV,
            data = mapOf(
                "rvNumber" to "123",
                "date" to LocalDate.now().toString(),
                "amount" to "1000"
            ),
            contact = ClientContactDetailsDTO(
                "test", "test", "test_add", "01731234567"
            )
        )
        val reported = controller.report(dto)

        val result = controller.getReportStatus(reported.body!!.toInt())

        assertEquals(ReportStatus.AWAITING_SEND.description, result.body!!.status)
    }
}
