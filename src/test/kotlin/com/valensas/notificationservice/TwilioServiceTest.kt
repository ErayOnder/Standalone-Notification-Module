package com.valensas.notificationservice

import com.twilio.exception.ApiException
import com.twilio.rest.api.v2010.account.Message
import com.twilio.rest.api.v2010.account.MessageCreator
import com.twilio.type.PhoneNumber
import com.valensas.notificationservice.model.SmsModel
import com.valensas.notificationservice.service.TwilioService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import kotlin.test.assertEquals

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ActiveProfiles("test")
@TestPropertySource(properties = ["notification.email.service=null", "notification.sms.service=twilio"])
class TwilioServiceTest {
    private var sender = "+12345678900"
    private lateinit var twilioService: TwilioService
    private lateinit var smsModel: SmsModel

    @Captor
    private lateinit var phoneNumberCaptor: ArgumentCaptor<PhoneNumber>

    @Captor
    private lateinit var stringCaptor: ArgumentCaptor<String>

    @BeforeEach
    fun init() {
        twilioService =
            TwilioService(
                sender = sender,
            )
        smsModel =
            SmsModel(
                receiver = "+98765432100",
                body = "This is a sms.",
                type = null,
            )
    }

    @Test
    fun `sms twilio publish success`() {
        val messageCreator = Mockito.mock(MessageCreator::class.java)
        val messageStatic = Mockito.mockStatic(Message::class.java)
        messageStatic.`when`<Any> {
            Message.creator(
                Mockito.any(PhoneNumber::class.java),
                Mockito.any(PhoneNumber::class.java),
                Mockito.anyString(),
            )
        }.thenReturn(messageCreator)
        Mockito.`when`(messageCreator.create()).thenReturn(Mockito.mock(Message::class.java))

        val response = twilioService.send(smsModel)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("SMS sent successfully to ${smsModel.receiver}.", response.body)

        messageStatic.close()
    }

    @Test
    fun `sms twilio publish fail`() {
        val messageCreator = Mockito.mock(MessageCreator::class.java)
        val messageStatic = Mockito.mockStatic(Message::class.java)
        messageStatic.`when`<Any> {
            Message.creator(
                Mockito.any(PhoneNumber::class.java),
                Mockito.any(PhoneNumber::class.java),
                Mockito.anyString(),
            )
        }.thenReturn(messageCreator)
        Mockito.`when`(messageCreator.create()).thenThrow(ApiException(""))

        val response = twilioService.send(smsModel)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)

        messageStatic.close()
    }

    @Test
    fun `sms twilio publish request verification`() {
        val messageCreator = Mockito.mock(MessageCreator::class.java)
        val messageStatic = Mockito.mockStatic(Message::class.java)
        messageStatic.`when`<Any> {
            Message.creator(
                Mockito.any(PhoneNumber::class.java),
                Mockito.any(PhoneNumber::class.java),
                Mockito.anyString(),
            )
        }.thenReturn(messageCreator)
        Mockito.`when`(messageCreator.create()).thenReturn(Mockito.mock(Message::class.java))
        twilioService.send(smsModel)
        messageStatic.verify {
            Message.creator(
                phoneNumberCaptor.capture(),
                phoneNumberCaptor.capture(),
                stringCaptor.capture(),
            )
        }

        assertEquals(smsModel.formattedReceiver, phoneNumberCaptor.allValues[0].toString())
        assertEquals(sender, phoneNumberCaptor.allValues[1].toString())
        assertEquals(smsModel.body, stringCaptor.value)

        messageStatic.close()
    }
}
