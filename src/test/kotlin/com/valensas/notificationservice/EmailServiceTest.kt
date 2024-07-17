package com.valensas.notificationservice

import com.valensas.notificationservice.model.EmailBody
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
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.mail.MailAuthenticationException
import org.springframework.mail.MailSendException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import kotlin.test.assertEquals

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ActiveProfiles("test")
@TestPropertySource(properties = ["notification.sms.service=null"])
class EmailServiceTest {
    @Mock
    private lateinit var mailSender: JavaMailSender

    private val senderAddress = "test@example.com"

    private lateinit var emailModel: EmailModel
    private lateinit var emailModelWithoutHTML: EmailModel
    private lateinit var emailService: EmailService

    @Captor
    private lateinit var mimeMessageCaptor: ArgumentCaptor<MimeMessage>

    @BeforeEach
    fun init() {
        emailService =
            EmailService(
                mailSender = mailSender,
                senderAddress = senderAddress,
            )

        emailModel =
            EmailModel(
                "test@example.com",
                EmailBody(
                    "Plain message",
                    "<h1>HTML message</h1>",
                ),
                "Test subject",
            )

        emailModelWithoutHTML =
            EmailModel(
                "test@example.com",
                EmailBody(
                    "Plain message",
                    null,
                ),
                "Test subject",
            )
    }

    @Test
    fun `sender success`() {
        Mockito.`when`(mailSender.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage::class.java))
        val response = emailService.send(emailModel)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Mail sent successfully to ${emailModel.receiver} with subject ${emailModel.subject}.", response.body)
    }

    @Test
    fun `sender authentication fail`() {
        Mockito.`when`(mailSender.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage::class.java))
        Mockito.`when`(mailSender.send(Mockito.any(MimeMessage::class.java))).thenThrow(
            MailAuthenticationException(""),
        )
        val response = emailService.send(emailModel)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("Authentication of server configurations failed.", response.body)
    }

    @Test
    fun `sender fails to send mail`() {
        Mockito.`when`(mailSender.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage::class.java))
        Mockito.`when`(mailSender.send(Mockito.any(MimeMessage::class.java))).thenThrow(
            MailSendException(""),
        )
        val response = emailService.send(emailModel)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("Mail failed to sent to ${emailModel.receiver} with subject ${emailModel.subject}.", response.body)
    }

    @Test
    fun `send mime message verification`() {
        Mockito.`when`(mailSender.createMimeMessage()).thenReturn(JavaMailSenderImpl().createMimeMessage())
        emailService.send(emailModel)
        verify(mailSender).send(mimeMessageCaptor.capture())
        val mimeMessage = mimeMessageCaptor.value

        assertEquals(senderAddress, mimeMessage.from[0].toString())
        assertEquals(emailModel.subject, mimeMessage.subject)
        assertEquals(emailModel.receiver, mimeMessage.getRecipients(Message.RecipientType.TO)[0].toString())

        val plainTextContent = getContentFromMultipart(mimeMessage.content as MimeMultipart, "text/plain")
        val htmlTextContent = getContentFromMultipart(mimeMessage.content as MimeMultipart, "text/html")
        assertEquals(emailModel.body.plainMessage, plainTextContent)
        assertEquals(emailModel.body.htmlMessage, htmlTextContent)
    }

    @Test
    fun `send mime message verification with null html`() {
        Mockito.`when`(mailSender.createMimeMessage()).thenReturn(JavaMailSenderImpl().createMimeMessage())
        emailService.send(emailModelWithoutHTML)
        verify(mailSender).send(mimeMessageCaptor.capture())
        val mimeMessage = mimeMessageCaptor.value

        assertEquals(senderAddress, mimeMessage.from[0].toString())
        assertEquals(emailModelWithoutHTML.subject, mimeMessage.subject)
        assertEquals(emailModelWithoutHTML.receiver, mimeMessage.getRecipients(Message.RecipientType.TO)[0].toString())

        val plainTextContent = getContentFromMultipart(mimeMessage.content as MimeMultipart, "text/plain")
        val htmlTextContent = getContentFromMultipart(mimeMessage.content as MimeMultipart, "text/html")
        assertEquals(emailModelWithoutHTML.body.plainMessage, plainTextContent)
        assertEquals(emailModelWithoutHTML.body.htmlMessage, htmlTextContent)
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
