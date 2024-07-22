package com.valensas.notificationservice

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.valensas.notificationservice.model.SmsModel

abstract class SmsServiceTest {
    protected lateinit var smsModel: SmsModel
    protected lateinit var smsModelFormatted: SmsModel
    protected lateinit var smsModelNull: SmsModel
    protected lateinit var smsModelInvalidNumbers: SmsModel

    open fun init() {
        smsModel =
            SmsModel(
                listOf(
                    generatePhoneNumber(true, "US", PhoneNumberUtil.PhoneNumberFormat.E164),
                    generatePhoneNumber(true, "TR", PhoneNumberUtil.PhoneNumberFormat.E164),
                ),
                "Test SMS",
                SmsModel.SmsType.TRANSACTIONAL,
            )

        smsModelFormatted =
            SmsModel(
                listOf(
                    generatePhoneNumber(true, "US", PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL),
                    generatePhoneNumber(true, "TR", PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL),
                    generatePhoneNumber(true, "TR", PhoneNumberUtil.PhoneNumberFormat.RFC3966),
                ),
                "Test SMS",
                SmsModel.SmsType.TRANSACTIONAL,
            )

        smsModelNull =
            SmsModel(
                listOf(
                    generatePhoneNumber(false, "US", PhoneNumberUtil.PhoneNumberFormat.E164),
                    generatePhoneNumber(false, "TR", PhoneNumberUtil.PhoneNumberFormat.E164),
                ),
                "Test SMS",
                null,
            )

        smsModelInvalidNumbers =
            SmsModel(
                listOf(
                    generatePhoneNumber(false, "US", PhoneNumberUtil.PhoneNumberFormat.E164),
                    generatePhoneNumber(false, "TR", PhoneNumberUtil.PhoneNumberFormat.E164),
                ),
                "Test SMS",
                SmsModel.SmsType.TRANSACTIONAL,
            )
    }

    private fun generatePhoneNumber(
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
}
