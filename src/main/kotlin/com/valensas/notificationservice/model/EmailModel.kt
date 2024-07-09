package com.valensas.notificationservice.model

data class EmailModel(
    val receiver: String,
    val body: EmailBody,
    val subject: String,
    val channel: EmailChannels
)

data class EmailBody(
    val plainMessage: String,
    val htmlMessage: String?
)