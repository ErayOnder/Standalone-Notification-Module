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
                receivers = listOf("+98765432100", "+98765432101"),
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
        val responseList = smsModel.formattedReceiver.map { "$it: Sent successfully." }

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(responseList.joinToString("\n"), response.body)
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
        Mockito.`when`(messageCreator.create()).thenThrow(ApiException("Error"))

        val response = twilioService.send(smsModel)
        val responseList = smsModel.formattedReceiver.map { "$it: Failed to sent - Error" }

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(responseList.joinToString("\n"), response.body)

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
        messageStatic.verify(
            {
                Message.creator(
                    phoneNumberCaptor.capture(),
                    phoneNumberCaptor.capture(),
                    stringCaptor.capture(),
                )
            },
            Mockito.times(smsModel.receivers.size),
        )

        assertEquals(smsModel.body, stringCaptor.value)
        assertEquals(sender, phoneNumberCaptor.allValues[1].toString())
        for (i in 0 until smsModel.receivers.size) {
            assertEquals(smsModel.formattedReceiver[i], phoneNumberCaptor.allValues[2 * i].toString())
        }
        messageStatic.close()
    }
}
