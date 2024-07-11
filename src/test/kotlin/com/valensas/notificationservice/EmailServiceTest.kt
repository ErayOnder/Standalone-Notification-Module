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

    private lateinit var emailService: EmailService

    @Captor
    private lateinit var mimeMessageCaptor: ArgumentCaptor<MimeMessage>

    private val awsSender = "awstest@example.com"
    private val smtpSender = "awstest@example.com"

    @BeforeEach
    fun init() {
        emailService =
            EmailService(
                sesJavaMailSender = sesMailSender,
                smtpJavaMailSender = smtpMailSender,
                awsSender = awsSender,
                smtpSender = smtpSender,
            )
    }

    @Test
    fun `send returns 200 response for aws channel`() {
        val email =
            EmailModel(
                "test@example.com",
                EmailBody(
                    "Plain message",
                    "<h1>HTML message</h1>",
                ),
                "Test subject",
                EmailChannel.AWS,
            )

        Mockito.`when`(sesMailSender.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage::class.java))

        val response = emailService.send(email)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Mail sent successfully to ${email.receiver} with subject ${email.subject}", response.body)
    }

    @Test
    fun `send returns 200 response for smtp channel`() {
        val email =
            EmailModel(
                "test@example.com",
                EmailBody(
                    "Plain message",
                    "<h1>HTML message</h1>",
                ),
                "Test subject",
                EmailChannel.SMTP,
            )

        Mockito.`when`(smtpMailSender.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage::class.java))

        val response = emailService.send(email)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Mail sent successfully to ${email.receiver} with subject ${email.subject}", response.body)
    }

    @Test
    fun `send returns 401 response for aws channel`() {
        val email =
            EmailModel(
                "test@example.com",
                EmailBody(
                    "Plain message",
                    "<h1>HTML message</h1>",
                ),
                "Test subject",
                EmailChannel.AWS,
            )

        Mockito.`when`(sesMailSender.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage::class.java))
        Mockito.`when`(sesMailSender.send(Mockito.any(MimeMessage::class.java))).thenThrow(
            MailAuthenticationException("")
        )

        val response = emailService.send(email)

        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertEquals("Authentication of user $awsSender failed", response.body)
    }

    @Test
    fun `send returns 401 response for smtp channel`() {
        val email =
            EmailModel(
                "test@example.com",
                EmailBody(
                    "Plain message",
                    "<h1>HTML message</h1>",
                ),
                "Test subject",
                EmailChannel.SMTP,
            )

        Mockito.`when`(smtpMailSender.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage::class.java))
        Mockito.`when`(smtpMailSender.send(Mockito.any(MimeMessage::class.java))).thenThrow(
            MailAuthenticationException("")
        )

        val response = emailService.send(email)

        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertEquals("Authentication of user $smtpSender failed", response.body)
    }

    @Test
    fun `send returns 500 response for aws channel`() {
        val email =
            EmailModel(
                "test@example.com",
                EmailBody(
                    "Plain message",
                    "<h1>HTML message</h1>",
                ),
                "Test subject",
                EmailChannel.AWS,
            )

        Mockito.`when`(sesMailSender.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage::class.java))
        Mockito.`when`(sesMailSender.send(Mockito.any(MimeMessage::class.java))).thenThrow(
            MailSendException("")
        )

        val response = emailService.send(email)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("Mail failed to sent to ${email.receiver} with subject ${email.subject}", response.body)
    }

    @Test
    fun `send returns 500 response for smtp channel`() {
        val email =
            EmailModel(
                "test@example.com",
                EmailBody(
                    "Plain message",
                    "<h1>HTML message</h1>",
                ),
                "Test subject",
                EmailChannel.SMTP,
            )

        Mockito.`when`(smtpMailSender.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage::class.java))
        Mockito.`when`(smtpMailSender.send(Mockito.any(MimeMessage::class.java))).thenThrow(
            MailSendException("")
        )

        val response = emailService.send(email)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("Mail failed to sent to ${email.receiver} with subject ${email.subject}", response.body)
    }
}