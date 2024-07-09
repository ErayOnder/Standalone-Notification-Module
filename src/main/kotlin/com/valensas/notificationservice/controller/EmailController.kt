package com.valensas.notificationservice.controller

import com.valensas.notificationservice.service.EmailService
import com.valensas.notificationservice.config.AWSProperties
import com.valensas.notificationservice.model.EmailModel
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@ConditionalOnProperty("notification-service.email.enabled", havingValue = "true")
@EnableConfigurationProperties(AWSProperties::class)
class EmailController(
    private val emailService: EmailService
) {
    @PostMapping("/v1/email")
    fun sendMail(@RequestBody emailModel: EmailModel): ResponseEntity<String> {
        emailService.send(emailModel)
        return ResponseEntity.ok("Email sent!")
    }
}