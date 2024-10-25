package com.valensas.notificationservice.controller

import com.valensas.notificationservice.model.EmailModel
import com.valensas.notificationservice.service.EmailService
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@ConditionalOnBean(EmailService::class)
class EmailController(
    private val emailService: EmailService,
) {
    @PostMapping("/email")
    fun sendMail(
        @RequestBody emailModel: EmailModel,
    ): ResponseEntity<String> {
        try {
            val response = emailService.send(emailModel)
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            val message = e.message ?: "An error occurred while sending the email"
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message)
        }
    }
}
