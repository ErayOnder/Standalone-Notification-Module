package com.valensas.notificationservice.service

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.valensas.notificationservice.model.SmsModel

abstract class SmsService {
    abstract fun send(smsModel: SmsModel): List<SmsResponse>

    protected fun validatePhoneNumber(phoneNumber: String) {
        val phoneNumberUtil = PhoneNumberUtil.getInstance()
        val number = phoneNumberUtil.parse(phoneNumber, "TR")
        if (!phoneNumberUtil.isValidNumber(number)) {
            throw IllegalArgumentException("Invalid phone number.")
        }
    }

    data class SmsResponse(
        val receiver: String,
        val status: SmsStatus,
        val message: String,
    ) {
        enum class SmsStatus {
            SUCCESS,
            FAILED,
        }
    }
}
