package de.swirtz.lwdemo.service.resolving

import de.swirtz.lwdemo.controller.dto.ClientContactDetailsDTO
import de.swirtz.lwdemo.createKVReportDTO
import de.swirtz.lwdemo.createKVReportEntity
import de.swirtz.lwdemo.createPNUnifer
import de.swirtz.lwdemo.createRVReportDTO
import de.swirtz.lwdemo.createRVReportEntity
import de.swirtz.lwdemo.domain.ReportType
import de.swirtz.lwdemo.service.ReportValidationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class KVReportResolverTests {
    private val resolver = KVReportResolver(createPNUnifer())

    @Test
    fun `valid data can be resolved from client`() {
        val report = createKVReportDTO()

        val converted = resolver.convertFromClient(report)

        assertEquals(ReportType.SV_KV, converted.reportType)
    }

    @Test
    fun `valid data can be resolved from persistence`() {
        val report = createKVReportEntity()

        val converted = resolver.convertFromPersistence(report)

        assertEquals(ReportType.SV_KV, converted.reportType)
    }

    @Test
    fun `wrong report type can not be resolved from client`() {
        val report = createRVReportDTO()

        assertFailsWith<ReportValidationException>() {
            resolver.convertFromClient(report)
        }
    }

    @Test
    fun `wrong report type can not be resolved from persistence`() {
        val report = createRVReportEntity()

        assertFailsWith<ReportValidationException>() {
            resolver.convertFromPersistence(report)
        }
    }

    @Test
    fun `invalid phone number in client data leads to error`() {
        val report = createKVReportDTO(
            contact = ClientContactDetailsDTO(
                "test", "test", "test", "INVALID_PN"
            )
        )

        assertFailsWith<ReportValidationException>() {
            resolver.convertFromClient(report)
        }
    }

    @Test
    fun `invalid amount in client data leads to error`() {
        val base = createKVReportDTO()
        val invalidAmountData = base.data.toMutableMap()
        invalidAmountData["amount"] = "xy"

        val report = base.copy(data = invalidAmountData)

        assertFailsWith<ReportValidationException>() {
            resolver.convertFromClient(report)
        }
    }

    @Test
    fun `invalid amount in persistence data leads to error`() {
        val base = createKVReportEntity()
        val invalidAmountData = base.data.toMutableMap()
        invalidAmountData["amount"] = "xy"

        val report = base.copy(data = invalidAmountData)

        assertFailsWith<ReportValidationException>() {
            resolver.convertFromPersistence(report)
        }
    }

}
