package com.valensas.notificationservice.service

import com.valensas.notificationservice.model.SmsModel
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest

@Service("smsService")
@ConditionalOnProperty("notification-service.sms.service", havingValue = "sns")
class SnsService(
    private val snsClient: SnsClient,
) : SmsService {
    override fun send(smsModel: SmsModel): ResponseEntity<String> {
        val request =
            PublishRequest.builder()
                .phoneNumber(smsModel.receiver)
                .message(smsModel.body)
                .messageAttributes(
                    HashMap<String, MessageAttributeValue>().apply {
                        put(
                            "AWS.SNS.SMS.SMSType",
                            MessageAttributeValue.builder()
                                .stringValue(smsModel.type)
                                .dataType("String").build(),
                        )
                    },
                ).build()

        try {
            snsClient.publish(request)
            return ResponseEntity.ok().body("SMS sent successfully to ${smsModel.receiver}")
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body(e.message)
        }
    }
}