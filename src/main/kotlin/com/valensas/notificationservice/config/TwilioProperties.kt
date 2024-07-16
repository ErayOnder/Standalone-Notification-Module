package com.valensas.notificationservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "twilio")
data class TwilioProperties(
    var accountSid: String,
    var authToken: String,
    var fromPhoneNumber: String,
)
