package de.swirtz.lwdemo.domain

/**
 * Status a [Report] currently has.
 */
enum class ReportStatus (val description: String) {
    /* Report delivered by client but not sent to downstream services */
    AWAITING_SEND("Versand ausstehend"),
    /* Report currently being sent to downstream services */
    IN_TRANSIT("Report befindet sich in Versendung"),
    /* Report successfully sent to downstream services */
    SENT("Report erfolgreich versandt"),
    /* Report delivery failed and could not be sent to downstream services */
    FAILED("Versand fehlgeschlagen")
}
