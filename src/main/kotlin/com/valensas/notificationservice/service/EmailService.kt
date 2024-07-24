package com.valensas.notificationservice.service

import com.valensas.notificationservice.model.EmailModel
import jakarta.mail.internet.AddressException
import jakarta.mail.internet.InternetAddress
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
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
    fun send(emailModel: EmailModel): String {
        emailModel.receiversTo.forEach { validateEmailAddress(it) }
        emailModel.receiversCc?.forEach { validateEmailAddress(it) }
        emailModel.receiversBcc?.forEach { validateEmailAddress(it) }

        val mimeMessage = mailSender.createMimeMessage()
        val mimeHelper = MimeMessageHelper(mimeMessage, true)

        mimeHelper.setFrom(senderAddress)
        mimeHelper.setTo(emailModel.receiversTo.toTypedArray())
        emailModel.receiversCc?.let { mimeHelper.setCc(it.toTypedArray()) }
        emailModel.receiversBcc?.let { mimeHelper.setBcc(it.toTypedArray()) }

        mimeHelper.setSubject(emailModel.subject)
        emailModel.body.htmlMessage?.let {
            mimeHelper.setText(emailModel.body.plainMessage, emailModel.body.htmlMessage)
        } ?: mimeHelper.setText(emailModel.body.plainMessage, false)

        mailSender.send(mimeMessage)
        return "Mail sent successfully to ${emailModel.receiversTo} with subject ${emailModel.subject}."
    }

    private fun validateEmailAddress(emailAddress: String) {
        try {
            val emailAddr = InternetAddress(emailAddress, true)
            emailAddr.validate()
        } catch (e: AddressException) {
            throw IllegalArgumentException("Invalid email address: '$emailAddress'")
        }
    }
}
