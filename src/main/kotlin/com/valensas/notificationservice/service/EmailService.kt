package com.valensas.notificationservice.service

import com.valensas.notificationservice.model.EmailModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import com.valensas.notificationservice.model.EmailChannel
import software.amazon.awssdk.services.ses.SesClient
import software.amazon.awssdk.services.ses.model.*

@Service
class EmailService(
    private val javaMailSender: JavaMailSender,
    private val sesClient: SesClient,
    @Value("\${cloud.aws.sender}")
    private val sender: String
) {
    fun send(emailModel: EmailModel): ResponseEntity<String> =
        when (emailModel.channel) {
            EmailChannel.AWS -> doSendWithAWS(emailModel)
            EmailChannel.SMTP -> doSendWithSMTP(emailModel)
        }

    fun doSendWithAWS(emailModel: EmailModel): ResponseEntity<String> {
        val destination = Destination.builder().toAddresses(emailModel.receiver).build()
        val subjectContent = Content.builder().data(emailModel.subject).build()
        val textContent = Content.builder().data(emailModel.body.plainMessage).build()
        val htmlContent = Content.builder().data(emailModel.body.htmlMessage).build()
        val mailBody = Body.builder().text(textContent).html(htmlContent).build()
        val mailMessage = Message.builder()
            .body(mailBody)
            .subject(subjectContent)
            .build()

        val request = SendEmailRequest.builder()
            .destination(destination)
            .source(sender)
            .message(mailMessage)
            .build()

        sesClient.sendEmail(request)
        return ResponseEntity.ok().body("Mail sent Successfully to " + emailModel.receiver + " with subject " + emailModel.subject)
    }

    fun doSendWithSMTP(emailModel: EmailModel): ResponseEntity<String> {
        return ResponseEntity.ok("ok")
    }
}