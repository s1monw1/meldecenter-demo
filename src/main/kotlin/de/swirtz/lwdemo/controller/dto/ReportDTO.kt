package de.swirtz.lwdemo.controller.dto

import de.swirtz.lwdemo.domain.ReportStatus
import de.swirtz.lwdemo.domain.ReportType

/**
 * File containing data transfer objects that are representing formats expected from the client
 */


data class ReportDTO(
    val type: ReportType,
    val data: Map<String, Any>,
    val contact: ClientContactDetailsDTO,
    val status: ReportStatus? = null
)

data class ClientContactDetailsDTO(
    val name: String,
    val surName: String,
    val address: String,
    val phoneNumber: String
)
