package com.valensas.notificationservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cloud.aws")
data class AWSProperties (
    val credentials: AWSCredentials,
    val region: AWSRegion
)

data class AWSCredentials (
    val accessKey: String,
    val secretKey: String
)

data class AWSRegion (
    val static: String,
    val auto: Boolean
)