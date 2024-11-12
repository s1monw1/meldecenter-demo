package de.swirtz.lwdemo.domain

import java.math.BigDecimal
import java.time.LocalDate

data class KVReport(
    val data: KVReportData,
    val contact: ClientContactDetails,
    val id: ReportId? = null,
) : Report<KVReportData>(ReportType.SV_KV, id, data, contact) {

    override fun getReportDataAsMap(): Map<String, String> =
        hashMapOf(
            "date" to data.date.toString(),
            "amount" to data.amount.toString(),
            "kvNumber" to data.personalKVNumber,
        )

    override fun getContactDetailsAsMap(): Map<String, String> =
        hashMapOf(
            "name" to contact.name,
            "surName" to contact.surName,
            "address" to contact.address,
            "phoneNumber" to contact.phoneNumber.phoneNumber,
        )

}

data class KVReportData(
    val personalKVNumber: String,
    val date: LocalDate,
    val amount: BigDecimal
)
