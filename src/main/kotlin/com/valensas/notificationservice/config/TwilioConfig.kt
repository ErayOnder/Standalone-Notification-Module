package com.valensas.notificationservice.config

import com.twilio.Twilio
import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty("notification-service.sms.service", havingValue = "twilio")
@EnableConfigurationProperties(TwilioProperties::class)
class TwilioConfig(
    private val twilioProperties: TwilioProperties,
) {
    @PostConstruct
    fun initializeTwilio() {
        Twilio.init(twilioProperties.accountSid, twilioProperties.authToken)
    }
}
