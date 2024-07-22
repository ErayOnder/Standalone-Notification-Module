package com.valensas.notificationservice.service

import com.twilio.rest.api.v2010.account.Message
import com.twilio.type.PhoneNumber
import com.valensas.notificationservice.config.TwilioProperties
import com.valensas.notificationservice.model.SmsModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Service

@Service("smsService")
@ConditionalOnProperty("notification.sms.service", havingValue = "twilio")
@EnableConfigurationProperties(TwilioProperties::class)
class TwilioService(
    @Value("\${twilio.from-phone-number}")
    private val sender: String,
) : SmsService() {
    override fun send(smsModel: SmsModel): List<String> {
        val responseList = mutableListOf<String>()
        smsModel.formattedReceivers.forEach { receiver ->
            try {
                validatePhoneNumber(receiver)
                Message.creator(
                    PhoneNumber(receiver),
                    PhoneNumber(sender),
                    smsModel.body,
                ).create()
                responseList += "$receiver: Sent successfully."
            } catch (e: Exception) {
                responseList += "$receiver: Failed to sent - ${e.message ?: "Unknown error."}"
            }
        }
        return responseList
    }
}
