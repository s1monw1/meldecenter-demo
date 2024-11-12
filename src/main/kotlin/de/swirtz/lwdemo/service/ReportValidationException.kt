package de.swirtz.lwdemo.service

/**
 * A custom [RuntimeException] for delegating validation errors back to the reporting client.
 */
class ReportValidationException : RuntimeException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}
