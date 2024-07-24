package com.valensas.notificationservice.controller

import com.valensas.notificationservice.model.SmsModel
import com.valensas.notificationservice.service.SmsService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@ConditionalOnBean(SmsService::class)
class SmsController(
    @Qualifier("smsService")
    private val smsService: SmsService,
) {
    @PostMapping("/sms")
    fun sendSms(
        @RequestBody smsModel: SmsModel,
    ): ResponseEntity<List<SmsService.SmsResponse>> {
        try {
            val response = smsService.send(smsModel)
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            val message = e.message ?: "An error occurred while sending sms"
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR,
            ).body(listOf(SmsService.SmsResponse("", SmsService.SmsStatus.FAILED, message)))
        }
    }
}
