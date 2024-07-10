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
    fun send(emailModel: EmailModel): ResponseEntity<String> {
        val mailSender: JavaMailSender
        val senderAddress: String
        when (emailModel.channel) {
            EmailChannel.AWS -> {
                mailSender = sesJavaMailSender
                senderAddress = awsSender
            }
            EmailChannel.SMTP -> {
                mailSender = smtpJavaMailSender
                senderAddress = smtpSender
            }
        }

        val mimeMessage = mailSender.createMimeMessage()
        val mimeHelper = MimeMessageHelper(mimeMessage, true)

        mimeHelper.setFrom(senderAddress)
        mimeHelper.setTo(emailModel.receiver)
        mimeHelper.setSubject(emailModel.subject)

        emailModel.body.htmlMessage?.let {
            mimeHelper.setText(emailModel.body.plainMessage, emailModel.body.htmlMessage)
        } ?: mimeHelper.setText(emailModel.body.plainMessage, false)

        mailSender.send(mimeMessage)
        return ResponseEntity.ok().body("Mail sent Successfully to " + emailModel.receiver + " with subject " + emailModel.subject)
    }
}