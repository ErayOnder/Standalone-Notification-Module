package com.valensas.notificationservice.service

import com.twilio.exception.ApiException
import com.twilio.rest.api.v2010.account.Message
import com.twilio.type.PhoneNumber
import com.valensas.notificationservice.config.TwilioProperties
import com.valensas.notificationservice.model.SmsModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service("smsService")
@ConditionalOnProperty("notification.sms.service", havingValue = "twilio")
@EnableConfigurationProperties(TwilioProperties::class)
class TwilioService(
    @Value("\${twilio.from-phone-number}")
    private val sender: String,
) : SmsService {
    override fun send(smsModel: SmsModel): ResponseEntity<String> {
        try {
            val message =
                Message.creator(
                    PhoneNumber(smsModel.receiver),
                    PhoneNumber(sender),
                    smsModel.body,
                ).create()
            return ResponseEntity.ok(
                "SMS sent successfully to ${smsModel.receiver}",
            )
        } catch (e: ApiException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
        }
    }
}
