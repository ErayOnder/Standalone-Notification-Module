package com.valensas.notificationservice.controller

import com.valensas.notificationservice.model.SmsModel
import com.valensas.notificationservice.service.SmsService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@ConditionalOnExpression("'\${notification.sms.service}' == 'twilio' || '\${notification.sms.service}' == 'sns'")
class SmsController(
    @Qualifier("smsService")
    private val smsService: SmsService,
) {
    @PostMapping("/sms")
    fun sendSms(
        @RequestBody smsModel: SmsModel,
    ): ResponseEntity<String> = smsService.send(smsModel)
}
