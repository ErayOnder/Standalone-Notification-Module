package com.valensas.notificationservice.model

import com.google.i18n.phonenumbers.PhoneNumberUtil

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
    val phoneNumberUtil = PhoneNumberUtil.getInstance()
    val number = phoneNumberUtil.parse(phoneNumber, "TR")
    return phoneNumberUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164)
}

fun validatePhoneNumber(phoneNumber: String) {
    val phoneNumberUtil = PhoneNumberUtil.getInstance()
    val number = phoneNumberUtil.parse(phoneNumber, "TR")
    if (!phoneNumberUtil.isValidNumber(number)) {
        throw IllegalArgumentException("Invalid phone number.")
    }
}

fun generatePhoneNumber(
    isValid: Boolean,
    regionCode: String,
    format: PhoneNumberUtil.PhoneNumberFormat,
): String {
    val phoneNumberUtil = PhoneNumberUtil.getInstance()
    val phoneNumber =
        when (isValid) {
            true -> phoneNumberUtil.getExampleNumber(regionCode)
            false -> phoneNumberUtil.getInvalidExampleNumber(regionCode)
        }
    return phoneNumberUtil.format(phoneNumber, format)
}
