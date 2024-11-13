package de.swirtz.lwdemo.controller

import de.swirtz.lwdemo.controller.dto.ReportDTO
import de.swirtz.lwdemo.service.ReportsService
import de.swirtz.lwdemo.service.RequestedReportStatus
import de.swirtz.lwdemo.service.resolving.ReportsResolver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Main controller for interacting with reports.
 */
@RestController
@RequestMapping("/api/meldecenter")
class MeldecenterController(
    private val reportsService: ReportsService,
    private val reportsResolver: ReportsResolver
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @PostMapping
    fun report(@RequestBody report: ReportDTO): ResponseEntity<String> {
        logger.info("Received report: $report")
        val converted = reportsResolver.convertFromTransfer(report)
        val newID = reportsService.saveReport(converted)
        logger.info("Stored report with ID $newID: $report")
        return ok(newID.toString())
    }

    @GetMapping("/{id}")
    fun getReportStatus(@PathVariable id: Int): ResponseEntity<RequestedReportStatus> {
        logger.info("Received report status request for report $id")
        val report = reportsService.getReportStatus(id)
        return if (report == null) {
            notFound().build<RequestedReportStatus>()
        } else ok(report)
    }

}

