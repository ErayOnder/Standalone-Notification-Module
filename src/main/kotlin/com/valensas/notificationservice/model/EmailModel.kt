package com.valensas.notificationservice.model

data class EmailModel(
    val receiversTo: List<String>,
    val receiversCc: List<String>?,
    val receiversBcc: List<String>?,
    val body: EmailBody,
    val subject: String,
)

data class EmailBody(
    val plainMessage: String,
    val htmlMessage: String?,
)
