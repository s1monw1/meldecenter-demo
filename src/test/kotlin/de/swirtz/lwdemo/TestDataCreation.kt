package de.swirtz.lwdemo

import com.google.i18n.phonenumbers.PhoneNumberUtil
import de.swirtz.lwdemo.controller.dto.ClientContactDetailsDTO
import de.swirtz.lwdemo.controller.dto.ReportDTO
import de.swirtz.lwdemo.data.model.ReportEntity
import de.swirtz.lwdemo.domain.ReportStatus
import de.swirtz.lwdemo.domain.ReportType
import de.swirtz.lwdemo.domain.ReportType.SV_RV
import de.swirtz.lwdemo.domain.ReportType.SV_KV
import de.swirtz.lwdemo.service.PhoneNumberUnifier
import java.time.LocalDate
import java.time.LocalDateTime

val RV_DATA_BASE = mapOf(
    "rvNumber" to "123",
    "date" to LocalDate.now().toString(),
    "amount" to "1000"
)

val KV_DATA_BASE = mapOf(
    "kvNumber" to "123",
    "date" to LocalDate.now().toString(),
    "amount" to "1000"
)

val CONTACT_BASE = ClientContactDetailsDTO(
    "test", "test", "test_add", "01731234567"
)

val CONTACT_BASE_MAP = mapOf(
    "name" to CONTACT_BASE.name,
    "surName" to CONTACT_BASE.surName,
    "address" to CONTACT_BASE.address,
    "phoneNumber" to CONTACT_BASE.phoneNumber,
)

fun createRVReportEntity(
    clientId: String = "tester",
    data: Map<String, Any>? = null,
    contact: Map<String, Any>? = null,
    status: ReportStatus = ReportStatus.AWAITING_SEND
) =
    ReportEntity(
        0, LocalDateTime.now(), clientId, data ?: RV_DATA_BASE, contact ?: CONTACT_BASE_MAP, SV_RV, status
    )

fun createKVReportEntity(
    clientId: String = "tester",
    data: Map<String, Any>? = null,
    contact: Map<String, Any>? = null,
    status: ReportStatus = ReportStatus.AWAITING_SEND
) =
    ReportEntity(
        0, LocalDateTime.now(), clientId, data ?: KV_DATA_BASE, contact ?: CONTACT_BASE_MAP, SV_KV, status
    )


fun createRVReportDTO(data: Map<String, String>? = null, contact: ClientContactDetailsDTO? = null): ReportDTO {
    return ReportDTO(
        SV_RV,
        data = data ?: RV_DATA_BASE,
        contact = contact ?: CONTACT_BASE
    )
}

fun createKVReportDTO(data: Map<String, String>? = null, contact: ClientContactDetailsDTO? = null): ReportDTO {
    return ReportDTO(
        ReportType.SV_KV,
        data = data ?: KV_DATA_BASE,
        contact = contact ?: CONTACT_BASE
    )
}

fun createPNUnifer() = PhoneNumberUnifier(PhoneNumberUtil.getInstance())
