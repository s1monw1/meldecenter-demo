package de.swirtz.lwdemo.service.reporting

import de.swirtz.lwdemo.createKVReport
import de.swirtz.lwdemo.createRVReport
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class RVReportServiceTests {

    private val service = RVReportService()

    @Test
    fun `reportAny rejects other types via reportAny`() {
        assertFailsWith<IllegalArgumentException> {
            service.reportAny(listOf(createKVReport()))
        }
    }

    @Test
    fun `reportAny only accepts kv types via reportAny`() {
        val reports = listOf(createRVReport())

        val result = service.reportAny(reports)

        assertTrue(result.reports.first().successfulReporting)
    }

    @Test
    fun `reportAny only accepts kv types via report`() {
        val reports = listOf(createRVReport())

        val result = service.report(reports)

        assertTrue(result.reports.first().successfulReporting)
    }
}
