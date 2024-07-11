package com.valensas.notificationservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.mail")
data class SMTPProperties(
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val properties: Map<String, String>,
)
