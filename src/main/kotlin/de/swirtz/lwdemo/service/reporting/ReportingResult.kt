package de.swirtz.lwdemo.service.reporting

data class ReportingResult(
    val reports: List<ReportingResultEntry>
)

data class ReportingResultEntry(
    val reportId: Int,
    val successfulReporting: Boolean,
    val errorMessage: String? = null,
    val errorCode: Int? = null
)
