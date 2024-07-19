package com.valensas.notificationservice.service

import com.valensas.notificationservice.model.EmailModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.mail.MailAuthenticationException
import org.springframework.mail.MailSendException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
@ConditionalOnExpression("'\${notification.email.service}' == 'smtp' || '\${notification.email.service}' == 'ses'")
class EmailService(
    private val mailSender: JavaMailSender,
    @Value("\${notification.email.sender}")
    private val senderAddress: String,
) {
    fun send(emailModel: EmailModel): ResponseEntity<String> {
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
            return ResponseEntity.ok().body("Mail sent successfully to ${emailModel.receiver} with subject ${emailModel.subject}.")
        } catch (e: Exception) {
            val responseMessage =
                when (e) {
                    is MailAuthenticationException -> "Authentication of server configurations failed."
                    is MailSendException -> "Mail failed to sent to ${emailModel.receiver} with subject ${emailModel.subject}."
                    else -> e.message ?: "An unknown error occurred."
                }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMessage)
        }
    }
}
