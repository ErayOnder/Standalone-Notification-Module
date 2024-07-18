# Standalone Notification Module Project

## Table of Contents
**[Running the Module](#running-the-module)**
- [Mac OS X/Linux](#mac-os-xlinux)

**[Email Service](#email-service)**
- [Requirements](#requirements)
- [Using the Service](#using-the-service)
  - [Endpoint](#endpoint)
  - [Request Body](#request-body)

**[SMS Service](#sms-service)**
- [Requirements](#requirements-1)
- [Using the Service](#using-the-service-1)
  - [Endpoint](#endpoint-1)
  - [Request Body](#request-body-1)
    - [Receiver Format](#receivers-format)
    - [Type Attribute](#type-attribute)

## Running the Module
You can run the application directly on your local machine without needing to install a Servlet container. Execute the following command from the root level of the project:

#### Mac OS X/Linux
```bash
./gradlew bootRun
```

## Email Service
The Email Notification Service allows sending emails to users via SMTP and AWS Simple Email Service (SES).

### Requirements
Before running the project, ensure that the necessary environment variables are set. The `application.yaml` file contains placeholders within _**${}**_ for sensitive information such as access credentials, API tokens, and authentication details. Replace these placeholders with actual values in your local environment or deployment platform. This ensures secure and personalized configuration for services like SMTP and AWS SES.

You can only use one of the channels below.

For AWS SES:
```yaml
cloud:
  aws:
    region:
      auto: true
      static: ${AWS_REGION}
    credentials:
      access-key: ${AWS_ACCESS_KEY}
      secret-key: ${AWS_SECRET_KEY}
```

For SMTP:
```yaml
spring:
  application:
    name: NotificationService

  mail:
    host: ${SMTP_HOST}
    port: ${SMTP_PORT}
    username: ${SMTP_USERNAME}
    password: ${SMTP_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls.enable: true
        transport:
          protocol: smtp
        debug: ${SMTP_DEBUG}
```
After you provided the necessary credentials for your preferred service, you must fill the notification.email.service part in `application.yaml` file accordingly.

For AWS SES:
```yaml
notification:
  email:
    service: 'ses'
    sender: 'example@email.com'
```
For SMTP:
```yaml
notification:
  email:
    service: 'smtp'
    sender: 'example@email.com'
```
By specifying the desired email provider in the channel field, you can choose between SMTP and AWS SES for sending your email notifications.

### Using the Service
To use the service, make requests to the designated endpoint using your preferred HTTP client.
Follow the steps below to send email notifications:

### Endpoint
```http request
POST http://localhost:8080/email
```

### Request Body
```json
{
    "receiver": "example@email.com",
    "body": {
        "plainMessage": "Email Plain Content",
        "htmlMessage": "Email HTML Content"
    },
    "subject": "Email Subject"
}
```


## SMS Service
The SMS Notification Service allows sending emails to users via Twilio and AWS Simple Notification Service (SNS).

### Requirements
Before running the module, ensure that the necessary environment variables are set. The `application.yaml` file contains required attributes.

You can only use one of the channels below.

For AWS SNS:
```yaml
cloud:
  aws:
    region:
      auto: true
      static: ${AWS_REGION}
    credentials:
      access-key: ${AWS_ACCESS_KEY}
      secret-key: ${AWS_SECRET_KEY}
```

For Twilio:
```yaml
twilio:
  account-sid: ${TWILIO_ACCOUNT_SID}
  auth-token: ${TWILIO_AUTH_TOKEN}
  from-phone-number: ${TWILIO_FROM_PHONE_NUMBER}
```
You must fill the _**notification.sms.service**_ part in `application.yaml` file to specify the service you want to use.

For AWS SNS:
```yaml
notification:
  sms:
    service: sns
```
For Twilio:
```yaml
notification:
  sms:
    service: twilio
```

### Using the Service
Use the information below to send email notifications:

### Endpoint
```http request
POST http://localhost:8080/sms
```

### Request Body
```json
{
    "receivers": [
      "+1234567890",
      "+0987654321"
    ],
    "body": "SMS Content",
    "type": "Transactional"
}
```

#### Receivers Format
The **_receivers_** attribute is an array of phone numbers. Both services require the phone numbers to be in the E.164 format. However, the module can handle phone numbers in following formats:
- +1234567890
- +1 234 567 890
- 05xxxxxxxx (for Turkish phone numbers)
- 5xxxxxxxx (for Turkish phone numbers)

#### Type Attribute
The **_type_** attribute is only relevant to AWS SNS. You can choose between _"Promotional"_ and _"Transactional"_ for the type of SMS you want to send.