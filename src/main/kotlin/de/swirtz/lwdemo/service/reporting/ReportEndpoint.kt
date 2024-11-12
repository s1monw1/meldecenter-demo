package de.swirtz.lwdemo.service.reporting

import de.swirtz.lwdemo.domain.Report
import de.swirtz.lwdemo.domain.ReportType

/**
 * Interface to report client-provided reports to downstream services (Normally HTTP, but out of scope for this application).
 */
interface ReportEndpoint<R: Report<*>> {
    /**
     * Takes a list of reports [R] to process them to the corresponding reporting endpoint.
     */
    fun report(reports: List<R>): ReportingResult

    /**
     * This function is required to allow compatibility across the application because of this function
     * might not necessarily know the actual report type [R] at runtime.
     *
     * This function takes a list of arbitrary reports but should only work on reports of Type [R].
     *
     * If [reports] contains types other than [R] an [IllegalArgumentException] should be thrown.
     */
    fun reportAny(reports: List<Report<*>>): ReportingResult

    /**
     * Returns the [ReportType] the implementation of this interface can handle.
     */
    fun getReportType(): ReportType
}
