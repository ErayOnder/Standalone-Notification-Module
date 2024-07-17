package com.valensas.notificationservice.model

data class SmsModel(
    val receiver: String,
    val body: String,
    val type: String?,
) {
    init {
        require(type?.lowercase() == "promotional" || type?.lowercase() == "transactional" || type == null)
    }

    val formattedReceiver: String
        get() = receiver.replace(" ", "")
}
