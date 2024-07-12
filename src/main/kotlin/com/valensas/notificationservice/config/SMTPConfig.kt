package com.valensas.notificationservice.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.mail.MailProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl

@Configuration
@ConditionalOnProperty("notification-service.email.enabled", havingValue = "true")
@EnableConfigurationProperties(MailProperties::class)
class SMTPConfig(
    private val smtpProperties: MailProperties,
) {
    @Bean("smtpJavaMailSender")
    fun javaMailSender(): JavaMailSender {
        val mailSender = JavaMailSenderImpl()
        mailSender.host = smtpProperties.host
        mailSender.port = smtpProperties.port
        mailSender.username = smtpProperties.username
        mailSender.password = smtpProperties.password

        val props = mailSender.javaMailProperties
        smtpProperties.properties.forEach { (key, value) -> props[key] = value }

        return mailSender
    }
}
