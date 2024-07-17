package com.valensas.notificationservice.config

import io.awspring.cloud.ses.SimpleEmailServiceJavaMailSender
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.services.ses.SesClient

@Configuration
@ConditionalOnProperty("notification.email.service", havingValue = "ses")
@EnableConfigurationProperties(AWSProperties::class)
class SesConfig(
    private val awsProperties: AWSProperties,
) {
    @Bean
    fun sesClient(): SesClient {
        val credentials =
            AwsBasicCredentials.create(
                awsProperties.credentials.accessKey,
                awsProperties.credentials.secretKey,
            )
        return SesClient.builder()
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .region(awsProperties.region.static)
            .build()
    }

    @Bean
    fun javaMailSender(sesClient: SesClient): JavaMailSender = SimpleEmailServiceJavaMailSender(sesClient)
}
