package com.valensas.notificationservice.model

data class PushModel(
    val title: String,
    val body: String,
    val token: String,
    val channel: PushChannel
)