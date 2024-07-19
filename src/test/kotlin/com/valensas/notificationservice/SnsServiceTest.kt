package com.valensas.notificationservice

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.valensas.notificationservice.model.SmsModel
import com.valensas.notificationservice.service.SnsService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
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
class SnsServiceTest {
    @Mock
    private lateinit var snsClient: SnsClient

    private lateinit var snsService: SnsService

    private lateinit var smsModel: SmsModel
    private lateinit var smsModelFormatted: SmsModel
    private lateinit var smsModelNull: SmsModel
    private lateinit var smsModelInvalidNumbers: SmsModel

    @Captor
    private lateinit var publishRequestCaptor: ArgumentCaptor<PublishRequest>

    @BeforeEach
    fun init() {
        snsService = SnsService(snsClient)

        val phoneNumberUtil = PhoneNumberUtil.getInstance()

        val exampleNumUS =
            phoneNumberUtil.format(
                phoneNumberUtil.getExampleNumber("US"),
                PhoneNumberUtil.PhoneNumberFormat.E164,
            )
        val exampleNumTR =
            phoneNumberUtil.format(
                phoneNumberUtil.getExampleNumber("TR"),
                PhoneNumberUtil.PhoneNumberFormat.E164,
            )

        val invalidExampleNumUS = phoneNumberUtil.format(
            phoneNumberUtil.getInvalidExampleNumber("US"),
            PhoneNumberUtil.PhoneNumberFormat.E164,
        )
        val invalidExampleNumTR = phoneNumberUtil.format(
            phoneNumberUtil.getInvalidExampleNumber("TR"),
            PhoneNumberUtil.PhoneNumberFormat.E164,
        )

        smsModel =
            SmsModel(
                listOf(exampleNumUS, exampleNumTR),
                "Test SMS",
                "Transactional",
            )

        smsModelFormatted =
            SmsModel(
                listOf("+90 532 456 78 90", "0 533 654 32 10", "553 444 33 22"),
                "Test SMS",
                "Transactional",
            )

        smsModelNull =
            SmsModel(
                listOf(exampleNumUS, exampleNumTR),
                "Test SMS",
                null,
            )

        smsModelInvalidNumbers = SmsModel(
            listOf(invalidExampleNumUS, invalidExampleNumTR),
            "Test SMS",
            "Transactional",
        )
    }

    @Test
    fun `sms sns publish success`() {
        val response = snsService.send(smsModel)

        val responseList = smsModel.formattedReceivers.map { "$it: Sent successfully." }

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(responseList.joinToString("\n"), response.body)
    }

    @Test
    fun `sms sns publish fail`() {
        Mockito.`when`(
            snsClient.publish(Mockito.any(PublishRequest::class.java)),
        ).thenThrow(AwsServiceException.builder().message("Error").build())
        val response = snsService.send(smsModel)

        val responseList = smsModel.formattedReceivers.map { "$it: Failed to sent - Error" }

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(responseList.joinToString("\n"), response.body)
    }

    @Test
    fun `sms sns smsType null`() {
        val response = snsService.send(smsModelNull)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("SMS 'type' attribute is required.", response.body)
    }

    @Test
    fun `sms sns invalid phone number fail`() {
        val response = snsService.send(smsModelInvalidNumbers)

        val responseList = smsModelInvalidNumbers.formattedReceivers.map { "$it: Failed to sent - Invalid phone number." }
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(responseList.joinToString("\n"), response.body)
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
            assertEquals(smsModel.type, publishRequests[i].messageAttributes()["AWS.SNS.SMS.SMSType"]?.stringValue())
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
            assertEquals(smsModelFormatted.type, publishRequests[i].messageAttributes()["AWS.SNS.SMS.SMSType"]?.stringValue())
        }
    }
}
