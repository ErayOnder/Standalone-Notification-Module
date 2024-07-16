package com.valensas.notificationservice.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.services.sns.SnsClient

@Configuration
@ConditionalOnProperty("notification-service.sms.service", havingValue = "sns")
@EnableConfigurationProperties(AWSProperties::class)
class SnsConfig(
    private val awsProperties: AWSProperties,
) {
    @Bean
    fun snsClient(): SnsClient {
        val basicAWSCredentials =
            AwsBasicCredentials.create(
                awsProperties.credentials.accessKey,
                awsProperties.credentials.secretKey,
            )
        return SnsClient.builder()
            .credentialsProvider(StaticCredentialsProvider.create(basicAWSCredentials))
            .region(awsProperties.region.static)
            .build()
    }
}
