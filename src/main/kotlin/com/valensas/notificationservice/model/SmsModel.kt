package com.valensas.notificationservice.model

data class SmsModel(
    val receivers: List<String>,
    val body: String,
    val type: String?,
) {
    init {
        require(type?.lowercase() == "promotional" || type?.lowercase() == "transactional" || type == null)
    }

    val formattedReceivers: List<String>
        get() = receivers.map { receiver -> formatPhoneNumber(receiver) }
}

fun formatPhoneNumber(phoneNumber: String): String {
    val prefix =
        if (phoneNumber[0] == '+') {
            ""
        } else if (phoneNumber[0] == '0') {
            "+9"
        } else {
            "+90"
        }
    return (prefix + phoneNumber).replace(" ", "")
}
