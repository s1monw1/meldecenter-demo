package de.swirtz.lwdemo.domain

sealed class Report<T>(
    val reportType: ReportType,
    val reportId: ReportId?,
    val reportData: T,
    val contactDetails: ClientContactDetails,
) {
    abstract fun getReportDataAsMap(): Map<String, String>
    abstract fun getContactDetailsAsMap(): Map<String, String>
}

@JvmInline
value class ReportId(val id: Int)
