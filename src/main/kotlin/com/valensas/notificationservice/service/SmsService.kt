package com.valensas.notificationservice.service

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.valensas.notificationservice.model.SmsModel
import org.springframework.http.ResponseEntity

abstract class SmsService {
    abstract fun send(smsModel: SmsModel): ResponseEntity<String>

    protected fun validatePhoneNumber(phoneNumber: String) {
        val phoneNumberUtil = PhoneNumberUtil.getInstance()
        val number = phoneNumberUtil.parse(phoneNumber, "TR")
        if (!phoneNumberUtil.isValidNumber(number)) {
            throw IllegalArgumentException("Invalid phone number.")
        }
    }
}
