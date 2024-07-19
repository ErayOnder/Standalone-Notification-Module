package com.valensas.notificationservice.model

data class EmailModel(
    val receiver: String,
    val body: EmailBody,
    val subject: String,
)

data class EmailBody(
    val plainMessage: String,
    val htmlMessage: String?,
)
