package com.valensas.notificationservice.service

import com.valensas.notificationservice.model.SmsModel
import org.springframework.http.ResponseEntity

interface SmsService {
    fun send(smsModel: SmsModel): ResponseEntity<String>
}
