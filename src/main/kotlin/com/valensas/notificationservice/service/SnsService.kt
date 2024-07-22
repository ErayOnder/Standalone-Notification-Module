package com.valensas.notificationservice.service

import com.valensas.notificationservice.model.SmsModel
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest

@Service("smsService")
@ConditionalOnProperty("notification.sms.service", havingValue = "sns")
class SnsService(
    private val snsClient: SnsClient,
) : SmsService() {
    override fun send(smsModel: SmsModel): List<String> {
        smsModel.type ?: throw IllegalArgumentException("SMS 'type' attribute is required.")

        val publisherRequestBuilder =
            PublishRequest.builder()
                .message(smsModel.body)
                .messageAttributes(
                    HashMap<String, MessageAttributeValue>().apply {
                        put(
                            "AWS.SNS.SMS.SMSType",
                            MessageAttributeValue.builder()
                                .stringValue(smsModel.type.toString())
                                .dataType("String").build(),
                        )
                    },
                )

        val responseList = mutableListOf<String>()
        smsModel.formattedReceivers.forEach { receiver ->
            try {
                validatePhoneNumber(receiver)
                snsClient.publish(publisherRequestBuilder.phoneNumber(receiver).build())
                responseList += "$receiver: Sent successfully."
            } catch (e: Exception) {
                responseList += "$receiver: Failed to sent - ${e.message ?: "Unknown error."}"
            }
        }

        return responseList
    }
}
