package com.valensas.notificationservice

import com.valensas.notificationservice.model.EmailBody
import com.valensas.notificationservice.model.EmailChannel
import com.valensas.notificationservice.model.EmailModel
import com.valensas.notificationservice.service.EmailService
import jakarta.mail.internet.MimeMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.mail.MailAuthenticationException
import org.springframework.mail.MailSendException
import org.springframework.mail.javamail.JavaMailSender
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
    }

    @Test
    fun `send returns 200 response for aws channel`() {
        Mockito.`when`(sesMailSender.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage::class.java))
        val response = emailService.send(awsEmailModel)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Mail sent successfully to ${awsEmailModel.receiver} with subject ${awsEmailModel.subject}", response.body)
    }

    @Test
    fun `send returns 200 response for smtp channel`() {
        Mockito.`when`(smtpMailSender.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage::class.java))
        val response = emailService.send(smtpEmailModel)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Mail sent successfully to ${smtpEmailModel.receiver} with subject ${smtpEmailModel.subject}", response.body)
    }

    @Test
    fun `send returns 401 response for aws channel`() {
        Mockito.`when`(sesMailSender.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage::class.java))
        Mockito.`when`(sesMailSender.send(Mockito.any(MimeMessage::class.java))).thenThrow(
            MailAuthenticationException(""),
        )
        val response = emailService.send(awsEmailModel)

        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertEquals("Authentication of user $awsSender failed", response.body)
    }

    @Test
    fun `send returns 401 response for smtp channel`() {
        Mockito.`when`(smtpMailSender.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage::class.java))
        Mockito.`when`(smtpMailSender.send(Mockito.any(MimeMessage::class.java))).thenThrow(
            MailAuthenticationException(""),
        )
        val response = emailService.send(smtpEmailModel)

        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertEquals("Authentication of user $smtpSender failed", response.body)
    }

    @Test
    fun `send returns 500 response for aws channel`() {
        Mockito.`when`(sesMailSender.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage::class.java))
        Mockito.`when`(sesMailSender.send(Mockito.any(MimeMessage::class.java))).thenThrow(
            MailSendException(""),
        )
        val response = emailService.send(awsEmailModel)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("Mail failed to sent to ${awsEmailModel.receiver} with subject ${awsEmailModel.subject}", response.body)
    }

    @Test
    fun `send returns 500 response for smtp channel`() {
        Mockito.`when`(smtpMailSender.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage::class.java))
        Mockito.`when`(smtpMailSender.send(Mockito.any(MimeMessage::class.java))).thenThrow(
            MailSendException(""),
        )
        val response = emailService.send(smtpEmailModel)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("Mail failed to sent to ${smtpEmailModel.receiver} with subject ${smtpEmailModel.subject}", response.body)
    }
}