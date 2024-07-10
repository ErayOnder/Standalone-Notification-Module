package com.valensas.notificationservice.service

import com.valensas.notificationservice.model.EmailModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import com.valensas.notificationservice.model.EmailChannel
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.mail.javamail.MimeMessageHelper

@Service
class EmailService(
    @Qualifier("sesJavaMailSender")
    private val sesJavaMailSender: JavaMailSender,
    @Value("\${cloud.aws.sender}")
    private val awsSender: String,

    @Qualifier("smtpJavaMailSender")
    private val smtpJavaMailSender: JavaMailSender,
    @Value("\${spring.mail.username}")
    private val smtpSender: String
) {
    fun send(emailModel: EmailModel): ResponseEntity<String> =
        when (emailModel.channel) {
            EmailChannel.AWS -> doSendWithAWS(emailModel)
            EmailChannel.SMTP -> doSendWithSMTP(emailModel)
        }

    fun doSendWithAWS(emailModel: EmailModel): ResponseEntity<String> {
        val mimeMessage = sesJavaMailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, true)

        helper.setFrom(smtpSender)
        helper.setTo(emailModel.receiver)
        helper.setSubject(emailModel.subject)

        emailModel.body.htmlMessage?.let {
            helper.setText(emailModel.body.plainMessage, emailModel.body.htmlMessage)
        } ?: helper.setText(emailModel.body.plainMessage, false)

        sesJavaMailSender.send(mimeMessage)

        return ResponseEntity.ok().body("Mail sent Successfully to " + emailModel.receiver + " with subject " + emailModel.subject)
    }

    fun doSendWithSMTP(emailModel: EmailModel): ResponseEntity<String> {
        val mimeMessage = smtpJavaMailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, true)

        helper.setFrom(smtpSender)
        helper.setTo(emailModel.receiver)
        helper.setSubject(emailModel.subject)

        emailModel.body.htmlMessage?.let {
            helper.setText(emailModel.body.plainMessage, emailModel.body.htmlMessage)
        } ?: helper.setText(emailModel.body.plainMessage, false)

        smtpJavaMailSender.send(mimeMessage)

        return ResponseEntity.ok().body("Mail sent Successfully to " + emailModel.receiver + " with subject " + emailModel.subject)
    }
}