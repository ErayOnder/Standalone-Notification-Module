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
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.springframework.boot.test.context.SpringBootTest
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
    private lateinit var emailModelWithNull: EmailModel
    private lateinit var invalidAddressEmailModel: EmailModel
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
                listOf("testTo1@example.com", "testTo2@example.com"),
                listOf("testCc1@example.com", "testCc2@example.com"),
                listOf("testBcc1@example.com", "testBcc2@example.com"),
                EmailBody(
                    "Plain message",
                    "<h1>HTML message</h1>",
                ),
                "Test subject",
            )

        emailModelWithNull =
            EmailModel(
                listOf("testTo1@example.com", "testTo2@example.com"),
                null,
                null,
                EmailBody(
                    "Plain message",
                    null,
                ),
                "Test subject",
            )

        invalidAddressEmailModel =
            EmailModel(
                listOf("testTo@exaple.com"),
                null,
                listOf("invalid...email@example.com"),
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

        assertEquals("Mail sent successfully to ${emailModel.receiversTo} with subject ${emailModel.subject}.", response)
    }

    @Test
    fun `sender authentication fail`() {
        Mockito.`when`(mailSender.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage::class.java))
        Mockito.`when`(mailSender.send(Mockito.any(MimeMessage::class.java))).thenThrow(
            MailAuthenticationException("Authentication of server configurations failed."),
        )
        val exception =
            assertThrows<MailAuthenticationException> {
                emailService.send(emailModel)
            }

        assertEquals("Authentication of server configurations failed.", exception.message)
    }

    @Test
    fun `sender fails to send mail`() {
        Mockito.`when`(mailSender.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage::class.java))
        Mockito.`when`(mailSender.send(Mockito.any(MimeMessage::class.java))).thenThrow(
            MailSendException("Mail failed to sent to ${emailModel.receiversTo} with subject ${emailModel.subject}."),
        )
        val exception =
            assertThrows<MailSendException> {
                emailService.send(emailModel)
            }

        assertEquals("Mail failed to sent to ${emailModel.receiversTo} with subject ${emailModel.subject}.", exception.message)
    }

    @Test
    fun `service fails with invalid address`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                emailService.send(invalidAddressEmailModel)
            }

        assertEquals("Invalid email address: '${invalidAddressEmailModel.receiversBcc?.get(0)}'", exception.message)
    }

    @Test
    fun `send mime message verification`() {
        Mockito.`when`(mailSender.createMimeMessage()).thenReturn(JavaMailSenderImpl().createMimeMessage())
        emailService.send(emailModel)
        verify(mailSender).send(mimeMessageCaptor.capture())
        val mimeMessage = mimeMessageCaptor.value

        assertEquals(senderAddress, mimeMessage.from[0].toString())
        assertEquals(emailModel.receiversTo, mimeMessage.getRecipients(Message.RecipientType.TO).map { address -> address.toString() })
        assertEquals(emailModel.receiversCc, mimeMessage.getRecipients(Message.RecipientType.CC).map { address -> address.toString() })
        assertEquals(emailModel.receiversBcc, mimeMessage.getRecipients(Message.RecipientType.BCC).map { address -> address.toString() })

        assertEquals(emailModel.subject, mimeMessage.subject)
        val plainTextContent = getContentFromMultipart(mimeMessage.content as MimeMultipart, "text/plain")
        val htmlTextContent = getContentFromMultipart(mimeMessage.content as MimeMultipart, "text/html")
        assertEquals(emailModel.body.plainMessage, plainTextContent)
        assertEquals(emailModel.body.htmlMessage, htmlTextContent)
    }

    @Test
    fun `send mime message verification with null fields`() {
        Mockito.`when`(mailSender.createMimeMessage()).thenReturn(JavaMailSenderImpl().createMimeMessage())
        emailService.send(emailModelWithNull)
        verify(mailSender).send(mimeMessageCaptor.capture())
        val mimeMessage = mimeMessageCaptor.value

        assertEquals(senderAddress, mimeMessage.from[0].toString())
        assertEquals(
            emailModelWithNull.receiversTo,
            mimeMessage.getRecipients(Message.RecipientType.TO).map {
                    address ->
                address.toString()
            },
        )
        assertEquals(
            emailModelWithNull.receiversCc,
            mimeMessage.getRecipients(Message.RecipientType.CC)?.map {
                    address ->
                address.toString()
            },
        )
        assertEquals(
            emailModelWithNull.receiversBcc,
            mimeMessage.getRecipients(Message.RecipientType.BCC)?.map {
                    address ->
                address.toString()
            },
        )

        assertEquals(emailModelWithNull.subject, mimeMessage.subject)
        val plainTextContent = getContentFromMultipart(mimeMessage.content as MimeMultipart, "text/plain")
        val htmlTextContent = getContentFromMultipart(mimeMessage.content as MimeMultipart, "text/html")
        assertEquals(emailModelWithNull.body.plainMessage, plainTextContent)
        assertEquals(emailModelWithNull.body.htmlMessage, htmlTextContent)
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
