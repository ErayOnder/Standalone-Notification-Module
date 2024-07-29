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
    override fun send(smsModel: SmsModel): List<SmsResponse> {
        val responseList = mutableListOf<SmsResponse>()
        smsModel.formattedReceivers.forEach { receiver ->
            val message =
                Message.creator(
                    PhoneNumber(receiver),
                    PhoneNumber(sender),
                    smsModel.body,
                )
            try {
                validatePhoneNumber(receiver)
                message.create()
                responseList += SmsResponse(receiver, SmsResponse.SmsStatus.SUCCESS, "Sent successfully.")
            } catch (e: Exception) {
                responseList += SmsResponse(receiver, SmsResponse.SmsStatus.FAILED, e.message ?: "Failed to send.")
            }
        }
        return responseList
    }
}
