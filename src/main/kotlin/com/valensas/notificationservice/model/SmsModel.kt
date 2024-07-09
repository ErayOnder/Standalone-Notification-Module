package com.valensas.notificationservice.model

data class SmsModel(
    val receiver: String,
    val body: String,
    val channel: SmsChannel
)