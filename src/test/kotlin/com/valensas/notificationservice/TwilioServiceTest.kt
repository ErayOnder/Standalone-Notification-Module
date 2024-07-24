package com.valensas.notificationservice

import com.twilio.exception.ApiException
import com.twilio.rest.api.v2010.account.Message
import com.twilio.rest.api.v2010.account.MessageCreator
import com.twilio.type.PhoneNumber
import com.valensas.notificationservice.service.SmsService
import com.valensas.notificationservice.service.TwilioService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import kotlin.test.assertEquals

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ActiveProfiles("test")
@TestPropertySource(properties = ["notification.email.service=null", "notification.sms.service=twilio"])
class TwilioServiceTest : SmsServiceTest() {
    private var sender = "+12345678900"
    private lateinit var twilioService: TwilioService

    @Captor
    private lateinit var phoneNumberCaptor: ArgumentCaptor<PhoneNumber>

    @Captor
    private lateinit var stringCaptor: ArgumentCaptor<String>

    @BeforeEach
    override fun init() {
        super.init()
        twilioService = TwilioService(sender)
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
        val responseList = smsModel.formattedReceivers.map { receiver ->
            SmsService.SmsResponse(receiver, SmsService.SmsStatus.SUCCESS, "Sent successfully.") }

        assertEquals(responseList.size, response.size)
        responseList.zip(response).forEach { (expected, actual) ->
            assertEquals(expected.receiver, actual.receiver)
            assertEquals(expected.status, actual.status)
            assertEquals(expected.message, actual.message)
        }

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
        val responseList = smsModel.formattedReceivers.map { receiver ->
            SmsService.SmsResponse(receiver, SmsService.SmsStatus.FAILED, "Error") }

        assertEquals(responseList.size, response.size)
        responseList.zip(response).forEach { (expected, actual) ->
            assertEquals(expected.receiver, actual.receiver)
            assertEquals(expected.status, actual.status)
            assertEquals(expected.message, actual.message)
        }

        messageStatic.close()
    }

    @Test
    fun `sms twilio invalid phone number fail`() {
        val response = twilioService.send(smsModelInvalidNumbers)
        val responseList = smsModelInvalidNumbers.formattedReceivers.map { receiver ->
            SmsService.SmsResponse(receiver, SmsService.SmsStatus.FAILED, "Invalid phone number.") }

        assertEquals(responseList.size, response.size)
        responseList.zip(response).forEach { (expected, actual) ->
            assertEquals(expected.receiver, actual.receiver)
            assertEquals(expected.status, actual.status)
            assertEquals(expected.message, actual.message)
        }
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
        for (i in 0 until smsModel.receivers.size) {
            assertEquals(smsModel.formattedReceivers[i], phoneNumberCaptor.allValues[2 * i].toString())
            assertEquals(sender, phoneNumberCaptor.allValues[2 * i + 1].toString())
        }
        messageStatic.close()
    }

    @Test
    fun `sms sns publish request verification formatted`() {
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
        twilioService.send(smsModelFormatted)
        messageStatic.verify(
            {
                Message.creator(
                    phoneNumberCaptor.capture(),
                    phoneNumberCaptor.capture(),
                    stringCaptor.capture(),
                )
            },
            Mockito.times(smsModelFormatted.receivers.size),
        )

        assertEquals(smsModelFormatted.body, stringCaptor.value)
        for (i in 0 until smsModelFormatted.receivers.size) {
            assertEquals(smsModelFormatted.formattedReceivers[i], phoneNumberCaptor.allValues[2 * i].toString())
            assertEquals(sender, phoneNumberCaptor.allValues[2 * i + 1].toString())
        }
        messageStatic.close()
    }
}
