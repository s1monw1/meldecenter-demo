package de.swirtz.lwdemo.domain

import java.math.BigDecimal
import java.time.LocalDate

data class RVReport(
    val data: RVReportData,
    val contact: ClientContactDetails,
    val id: ReportId? = null,
) : Report<RVReportData>(ReportType.SV_RV, id, data, contact) {

    override fun getReportDataAsMap(): Map<String, String> =
        hashMapOf(
            "date" to data.date.toString(),
            "amount" to data.amount.toString(),
            "rvNumber" to data.personalRVNumber,
        )

    override fun getContactDetailsAsMap(): Map<String, String> =
        hashMapOf(
            "name" to contact.name,
            "surName" to contact.surName,
            "address" to contact.address,
            "phoneNumber" to contact.phoneNumber.phoneNumber,
        )

}

data class RVReportData(
    val personalRVNumber: String,
    val date: LocalDate,
    val amount: BigDecimal
)
