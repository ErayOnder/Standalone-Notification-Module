package com.valensas.notificationservice.model

import com.google.i18n.phonenumbers.PhoneNumberUtil

data class SmsModel(
    val receivers: List<String>,
    val body: String,
    val type: SmsType?,
) {
    enum class SmsType {
        PROMOTIONAL,
        TRANSACTIONAL,
    }

    val formattedReceivers: List<String>
        get() =
            receivers.map { receiver ->
                val phoneNumberUtil = PhoneNumberUtil.getInstance()
                val number = phoneNumberUtil.parse(receiver, "TR")
                phoneNumberUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164)
            }
}
