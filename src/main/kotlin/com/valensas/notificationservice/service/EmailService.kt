package com.valensas.notificationservice.service

import com.valensas.notificationservice.model.EmailModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import com.valensas.notificationservice.model.EmailChannel
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.mail.*
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

        try {
            mailSender.send(mimeMessage)
            return ResponseEntity.ok().body("Mail sent successfully to " + emailModel.receiver + " with subject " + emailModel.subject)
        } catch (e: MailAuthenticationException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication of user " + senderAddress + " failed")
        } catch (e: MailSendException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Mail failed to sent to " + emailModel.receiver + " with subject " + emailModel.subject)
        } catch (e: MailException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
        }
    }
}