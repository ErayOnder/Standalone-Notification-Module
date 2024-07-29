package com.valensas.notificationservice

import com.valensas.notificationservice.service.SmsService
import com.valensas.notificationservice.service.SmsService.SmsResponse
import com.valensas.notificationservice.service.SnsService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import software.amazon.awssdk.awscore.exception.AwsServiceException
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.PublishRequest
import kotlin.test.assertEquals

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ActiveProfiles("test")
@TestPropertySource(properties = ["notification.email.service=null", "notification.sms.service=sns"])
class SnsServiceTest : SmsServiceTest() {
    @Mock
    private lateinit var snsClient: SnsClient

    private lateinit var snsService: SnsService

    @Captor
    private lateinit var publishRequestCaptor: ArgumentCaptor<PublishRequest>

    @BeforeEach
    override fun init() {
        super.init()
        snsService = SnsService(snsClient)
    }

    @Test
    fun `sms sns publish success`() {
        val response = snsService.send(smsModel)
        val responseList =
            smsModel.formattedReceivers.map { receiver ->
                SmsService.SmsResponse(receiver, SmsResponse.SmsStatus.SUCCESS, "Sent successfully.")
            }

        assertEquals(responseList.size, response.size)
        responseList.zip(response).forEach { (expected, actual) ->
            assertEquals(expected.receiver, actual.receiver)
            assertEquals(expected.status, actual.status)
            assertEquals(expected.message, actual.message)
        }
    }

    @Test
    fun `sms sns publish fail`() {
        Mockito.`when`(
            snsClient.publish(Mockito.any(PublishRequest::class.java)),
        ).thenThrow(AwsServiceException.builder().message("Error").build())
        val response = snsService.send(smsModel)

        val responseList =
            smsModel.formattedReceivers.map { receiver ->
                SmsService.SmsResponse(receiver, SmsResponse.SmsStatus.FAILED, "Error")
            }

        assertEquals(responseList.size, response.size)
        responseList.zip(response).forEach { (expected, actual) ->
            assertEquals(expected.receiver, actual.receiver)
            assertEquals(expected.status, actual.status)
            assertEquals(expected.message, actual.message)
        }
    }

    @Test
    fun `sms sns smsType null`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                snsService.send(smsModelNull)
            }

        assertEquals("SMS 'type' attribute is required.", exception.message)
    }

    @Test
    fun `sms sns invalid phone number fail`() {
        val response = snsService.send(smsModelInvalidNumbers)

        val responseList =
            smsModelInvalidNumbers.formattedReceivers.map { receiver ->
                SmsService.SmsResponse(receiver, SmsResponse.SmsStatus.FAILED, "Invalid phone number.")
            }

        assertEquals(responseList.size, response.size)
        responseList.zip(response).forEach { (expected, actual) ->
            assertEquals(expected.receiver, actual.receiver)
            assertEquals(expected.status, actual.status)
            assertEquals(expected.message, actual.message)
        }
    }

    @Test
    fun `sms sns publish request verification`() {
        snsService.send(smsModel)

        Mockito.verify(snsClient, Mockito.times(smsModel.receivers.size)).publish(publishRequestCaptor.capture())
        val publishRequests = publishRequestCaptor.allValues

        assertEquals(smsModel.receivers.size, publishRequests.size)
        for (i in 0 until smsModel.formattedReceivers.size) {
            assertEquals(smsModel.formattedReceivers[i], publishRequests[i].phoneNumber())
            assertEquals(smsModel.body, publishRequests[i].message())
            assertEquals(smsModel.type.toString(), publishRequests[i].messageAttributes()["AWS.SNS.SMS.SMSType"]?.stringValue())
        }
    }

    @Test
    fun `sms sns publish request verification formatted`() {
        snsService.send(smsModelFormatted)

        Mockito.verify(snsClient, Mockito.times(smsModelFormatted.receivers.size)).publish(publishRequestCaptor.capture())
        val publishRequests = publishRequestCaptor.allValues

        assertEquals(smsModelFormatted.receivers.size, publishRequests.size)
        for (i in 0 until smsModelFormatted.formattedReceivers.size) {
            assertEquals(smsModelFormatted.formattedReceivers[i], publishRequests[i].phoneNumber())
            assertEquals(smsModelFormatted.body, publishRequests[i].message())
            assertEquals(smsModelFormatted.type.toString(), publishRequests[i].messageAttributes()["AWS.SNS.SMS.SMSType"]?.stringValue())
        }
    }
}
