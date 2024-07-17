package com.valensas.notificationservice.controller

import com.valensas.notificationservice.model.EmailModel
import com.valensas.notificationservice.service.EmailService
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@ConditionalOnExpression("'\${notification.email.service}' == 'smtp' || '\${notification.email.service}' == 'ses'")
class EmailController(
    private val emailService: EmailService,
) {
    @PostMapping("/email")
    fun sendMail(
        @RequestBody emailModel: EmailModel,
    ): ResponseEntity<String> = emailService.send(emailModel)
}
