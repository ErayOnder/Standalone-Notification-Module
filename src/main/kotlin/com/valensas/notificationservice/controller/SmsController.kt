package com.valensas.notificationservice.controller

import com.valensas.notificationservice.service.SmsService
import com.valensas.notificationservice.model.SmsModel
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@ConditionalOnProperty(name = ["notification-service.sms.enabled"], havingValue = "true")
class SmsController(
    private val smsService: SmsService
) {
    @PostMapping("/v1/sms")
    fun sendSms(@RequestBody smsModel: SmsModel): ResponseEntity<String> {
        smsService.send(smsModel)
        return ResponseEntity.ok("SMS sent!")
    }
}