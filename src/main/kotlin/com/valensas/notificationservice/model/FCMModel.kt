package com.valensas.notificationservice.model

data class FCMModel(
    val message: FCMMessage,
) {
    constructor(pushModel: PushModel) : this(
        FCMMessage(
            token = pushModel.token,
            notification =
                FCMNotification(
                    title = pushModel.title,
                    body = pushModel.body,
                ),
        ),
    )
}

data class FCMMessage(
    val token: String,
    val notification: FCMNotification,
)

data class FCMNotification(
    val title: String,
    val body: String,
)
