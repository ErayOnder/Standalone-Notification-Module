package com.valensas.notificationservice.controller

import com.valensas.notificationservice.service.PushService
import com.valensas.notificationservice.model.PushModel
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PushController(
    private val pushService: PushService
) {
    @PostMapping("/v1/push")
    fun sendPush(@RequestBody pushModel: PushModel): ResponseEntity<String> {
        pushService.sendPush(pushModel)
        return ResponseEntity.ok("Push sent!")
    }
}