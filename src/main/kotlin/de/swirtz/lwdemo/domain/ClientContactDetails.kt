package de.swirtz.lwdemo.domain

data class ClientContactDetails(
    val name: String,
    val surName: String,
    val address: String,
    val phoneNumber: ClientPhoneNumber
)

data class ClientPhoneNumber(
    val phoneNumber: String
)
