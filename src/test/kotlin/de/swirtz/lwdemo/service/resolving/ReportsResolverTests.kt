package de.swirtz.lwdemo.service.resolving

import de.swirtz.lwdemo.createPNUnifer
import de.swirtz.lwdemo.createRVReportDTO
import de.swirtz.lwdemo.createRVReportEntity
import de.swirtz.lwdemo.domain.ReportType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ReportsResolverTests {

    @Test
    fun `no resolvers leads to ISE when resolving from transfer`() {
        val resolver = ReportsResolver(listOf())

        assertFailsWith<IllegalStateException> {
            resolver.convertFromTransfer(createRVReportDTO())
        }
    }

    @Test
    fun `no resolvers leads to ISE when resolving from persistence`() {
        val resolver = ReportsResolver(listOf())

        assertFailsWith<IllegalStateException> {
            resolver.convertFromPersistence(createRVReportEntity())
        }
    }

    @Test
    fun `resolving from transfer works if resolver is registered`() {
        val resolver = ReportsResolver(
            listOf(
                RVReportResolver(createPNUnifer())
            )
        )

        val resolved = resolver.convertFromTransfer(createRVReportDTO())

        assertEquals(ReportType.SV_RV, resolved.reportType)
    }

    @Test
    fun `resolving from persistence works if resolver is registered`() {
        val resolver = ReportsResolver(
            listOf(
                RVReportResolver(createPNUnifer())
            )
        )

        val resolved = resolver.convertFromPersistence(createRVReportEntity())

        assertEquals(ReportType.SV_RV, resolved.reportType)
    }
}
