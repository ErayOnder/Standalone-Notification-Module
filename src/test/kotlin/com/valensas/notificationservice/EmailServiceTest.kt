package com.valensas.notificationservice

import com.valensas.notificationservice.model.EmailBody
import com.valensas.notificationservice.model.EmailChannel
import com.valensas.notificationservice.model.EmailModel
import com.valensas.notificationservice.service.EmailService
import jakarta.mail.Message
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.mail.MailAuthenticationException
import org.springframework.mail.MailSendException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertEquals

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ActiveProfiles("test")
class EmailServiceTest {
    @Mock
    @Qualifier("sesJavaMailSender")
    private lateinit var sesMailSender: JavaMailSender

    @Mock
    @Qualifier("smtpJavaMailSender")
    private lateinit var smtpMailSender: JavaMailSender

    private val awsSender = "awstest@example.com"
    private val smtpSender = "awstest@example.com"

    private lateinit var awsEmailModel: EmailModel
    private lateinit var smtpEmailModel: EmailModel
    private lateinit var awsEmailModelWithoutHTML: EmailModel
    private lateinit var smtpEmailModelWithoutHTML: EmailModel
    private lateinit var emailService: EmailService

    @Captor
    private lateinit var mimeMessageCaptor: ArgumentCaptor<MimeMessage>

    @BeforeEach
    fun init() {
        emailService =
            EmailService(
                sesJavaMailSender = sesMailSender,
                smtpJavaMailSender = smtpMailSender,
                awsSender = awsSender,
                smtpSender = smtpSender,
            )

        awsEmailModel =
            EmailModel(
                "test@example.com",
                EmailBody(
                    "Plain message",
                    "<h1>HTML message</h1>",
                ),
                "Test subject",
                EmailChannel.AWS,
            )

        smtpEmailModel =
            EmailModel(
                "test@example.com",
                EmailBody(
                    "Plain message",
                    "<h1>HTML message</h1>",
                ),
                "Test subject",
                EmailChannel.SMTP,
            )

        awsEmailModelWithoutHTML =
            EmailModel(
                "test@example.com",
                EmailBody(
                    "Plain message",
                    null,
                ),
                "Test subject",
                EmailChannel.AWS,
            )

        smtpEmailModelWithoutHTML =
            EmailModel(
                "test@example.com",
                EmailBody(
                    "Plain message",
                    null,
                ),
                "Test subject",
                EmailChannel.SMTP,
            )
    }

    @Test
    fun `sender success for aws channel`() {
        Mockito.`when`(sesMailSender.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage::class.java))
        val response = emailService.send(awsEmailModel)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Mail sent successfully to ${awsEmailModel.receiver} with subject ${awsEmailModel.subject}.", response.body)
    }

    @Test
    fun `sender success for smtp channel`() {
        Mockito.`when`(smtpMailSender.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage::class.java))
        val response = emailService.send(smtpEmailModel)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Mail sent successfully to ${smtpEmailModel.receiver} with subject ${smtpEmailModel.subject}.", response.body)
    }

    @Test
    fun `sender authentication fail for aws channel`() {
        Mockito.`when`(sesMailSender.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage::class.java))
        Mockito.`when`(sesMailSender.send(Mockito.any(MimeMessage::class.java))).thenThrow(
            MailAuthenticationException(""),
        )
        val response = emailService.send(awsEmailModel)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("Authentication of server configurations failed.", response.body)
    }

    @Test
    fun `sender authentication fail for smtp channel`() {
        Mockito.`when`(smtpMailSender.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage::class.java))
        Mockito.`when`(smtpMailSender.send(Mockito.any(MimeMessage::class.java))).thenThrow(
            MailAuthenticationException(""),
        )
        val response = emailService.send(smtpEmailModel)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("Authentication of server configurations failed.", response.body)
    }

    @Test
    fun `sender fails to send mail for aws channel`() {
        Mockito.`when`(sesMailSender.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage::class.java))
        Mockito.`when`(sesMailSender.send(Mockito.any(MimeMessage::class.java))).thenThrow(
            MailSendException(""),
        )
        val response = emailService.send(awsEmailModel)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("Mail failed to sent to ${awsEmailModel.receiver} with subject ${awsEmailModel.subject}.", response.body)
    }

    @Test
    fun `sender fails to send mail for smtp channel`() {
        Mockito.`when`(smtpMailSender.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage::class.java))
        Mockito.`when`(smtpMailSender.send(Mockito.any(MimeMessage::class.java))).thenThrow(
            MailSendException(""),
        )
        val response = emailService.send(smtpEmailModel)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("Mail failed to sent to ${smtpEmailModel.receiver} with subject ${smtpEmailModel.subject}.", response.body)
    }

    @Test
    fun `send mime message verification for aws channel`() {
        Mockito.`when`(sesMailSender.createMimeMessage()).thenReturn(JavaMailSenderImpl().createMimeMessage())
        emailService.send(awsEmailModel)
        verify(sesMailSender).send(mimeMessageCaptor.capture())
        val mimeMessage = mimeMessageCaptor.value

        assertEquals(awsSender, mimeMessage.from[0].toString())
        assertEquals(awsEmailModel.subject, mimeMessage.subject)
        assertEquals(awsEmailModel.receiver, mimeMessage.getRecipients(Message.RecipientType.TO)[0].toString())

        val plainTextContent = getContentFromMultipart(mimeMessage.content as MimeMultipart, "text/plain")
        val htmlTextContent = getContentFromMultipart(mimeMessage.content as MimeMultipart, "text/html")
        assertEquals(awsEmailModel.body.plainMessage, plainTextContent)
        assertEquals(awsEmailModel.body.htmlMessage, htmlTextContent)
    }

    @Test
    fun `send mime message verification for smtp channel`() {
        Mockito.`when`(smtpMailSender.createMimeMessage()).thenReturn(JavaMailSenderImpl().createMimeMessage())
        emailService.send(smtpEmailModel)
        verify(smtpMailSender).send(mimeMessageCaptor.capture())
        val mimeMessage = mimeMessageCaptor.value

        assertEquals(smtpSender, mimeMessage.from[0].toString())
        assertEquals(smtpEmailModel.subject, mimeMessage.subject)
        assertEquals(smtpEmailModel.receiver, mimeMessage.getRecipients(Message.RecipientType.TO)[0].toString())

        val plainTextContent = getContentFromMultipart(mimeMessage.content as MimeMultipart, "text/plain")
        val htmlTextContent = getContentFromMultipart(mimeMessage.content as MimeMultipart, "text/html")
        assertEquals(smtpEmailModel.body.plainMessage, plainTextContent)
        assertEquals(smtpEmailModel.body.htmlMessage, htmlTextContent)
    }

    @Test
    fun `send mime message verification with html null for aws channel`() {
        Mockito.`when`(sesMailSender.createMimeMessage()).thenReturn(JavaMailSenderImpl().createMimeMessage())
        emailService.send(awsEmailModelWithoutHTML)
        verify(sesMailSender).send(mimeMessageCaptor.capture())
        val mimeMessage = mimeMessageCaptor.value

        assertEquals(awsSender, mimeMessage.from[0].toString())
        assertEquals(awsEmailModelWithoutHTML.subject, mimeMessage.subject)
        assertEquals(awsEmailModelWithoutHTML.receiver, mimeMessage.getRecipients(Message.RecipientType.TO)[0].toString())

        val plainTextContent = getContentFromMultipart(mimeMessage.content as MimeMultipart, "text/plain")
        val htmlTextContent = getContentFromMultipart(mimeMessage.content as MimeMultipart, "text/html")
        assertEquals(awsEmailModelWithoutHTML.body.plainMessage, plainTextContent)
        assertEquals(awsEmailModelWithoutHTML.body.htmlMessage, htmlTextContent)
    }

    @Test
    fun `send mime message verification with html null for smtp channel`() {
        Mockito.`when`(smtpMailSender.createMimeMessage()).thenReturn(JavaMailSenderImpl().createMimeMessage())
        emailService.send(smtpEmailModelWithoutHTML)
        verify(smtpMailSender).send(mimeMessageCaptor.capture())
        val mimeMessage = mimeMessageCaptor.value

        assertEquals(smtpSender, mimeMessage.from[0].toString())
        assertEquals(smtpEmailModelWithoutHTML.subject, mimeMessage.subject)
        assertEquals(smtpEmailModelWithoutHTML.receiver, mimeMessage.getRecipients(Message.RecipientType.TO)[0].toString())

        val plainTextContent = getContentFromMultipart(mimeMessage.content as MimeMultipart, "text/plain")
        val htmlTextContent = getContentFromMultipart(mimeMessage.content as MimeMultipart, "text/html")
        assertEquals(smtpEmailModelWithoutHTML.body.plainMessage, plainTextContent)
        assertEquals(smtpEmailModelWithoutHTML.body.htmlMessage, htmlTextContent)
    }

    fun getContentFromMultipart(
        mimeMultipart: MimeMultipart,
        type: String,
    ): String? {
        val typeIdx =
            when (type) {
                "text/plain" -> 0
                "text/html" -> 1
                else -> throw RuntimeException("Invalid type")
            }

        for (i in 0 until mimeMultipart.count) {
            val bodyPart = mimeMultipart.getBodyPart(i)
            when (bodyPart.content) {
                is MimeMultipart -> return getContentFromMultipart(bodyPart.content as MimeMultipart, type)
                is String -> {
                    if (i == typeIdx) {
                        return bodyPart.content as String
                    }
                }
            }
        }
        return null
    }
}
