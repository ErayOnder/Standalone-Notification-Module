# Standalone Notification Module Project

## Email Service
The Email Notification Service allows sending emails to users via SMTP and AWS Simple Email Service (SES).

### Requirements
Before running the project, ensure that the necessary environment variables are set.
The application.yaml file contains placeholders within ${} for sensitive information such as access credentials, API tokens, and authentication details. 
Replace these placeholders with actual values in your local environment or deployment platform.
This ensures secure and personalized configuration for services like SMTP and AWS SES.

For AWS SES:
```yaml
cloud:
  aws:
    region:
      auto: true
      static: eu-north-1
    credentials:
      access-key: ${AWS_ACCESS_KEY}
      secret-key: ${AWS_SECRET_KEY}
    sender: ${AWS_SENDER_EMAIL}
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
        debug: true
```

### Running the Service
You can run the application directly on your local machine without needing to install a Servlet container.
Execute the following command from the root level of the project:

#### Mac OS X/Linux
```
./gradlew bootRun
```

#### Windows
```
gradlew.bat bootRun
```

### Using the Service
To use the service, make requests to the designated endpoint using your preferred HTTP client.
Follow the steps below to send email notifications:

### Endpoint
```
POST http://localhost:8080/v1/email
```

### Request Body
```json
{
    "receiver": "example@email.com",
    "body": {
        "plainMessage": "Email Plain Content",
        "htmlMessage": "Email HTML Content"
    },
    "subject": "Email Subject",
    "channel": "AWS"
}
```

### Channel Options
- "SMTP" for sending via SMTP.
- "SES" for sending via AWS SES.

By specifying the desired email provider in the channel field, you can choose between SMTP and AWS SES for sending your email notifications.