package de.swirtz.lwdemo.controller

import de.swirtz.lwdemo.service.ReportValidationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ControllerErrorHandling {

    /**
     * Global controller error handler to transform [ReportValidationException] into HTTP 400 errors
     * telling the clients that there was a mistake in the reported data.
     */
    @ExceptionHandler(ReportValidationException::class)
    fun handleRuntimeException(ex: ReportValidationException): ResponseEntity<*> {
        return ResponseEntity<Any>(ex.message, HttpStatus.BAD_REQUEST);
    }
}

