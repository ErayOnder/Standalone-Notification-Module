package com.valensas.notificationservice.model

data class APNSModel(
    val aps: ApsModel,
) {
    constructor(pushModel: PushModel) : this(
        ApsModel(
            AlertModel(
                title = pushModel.title,
                body = pushModel.body,
            ),
        ),
    )
}

data class ApsModel(
    val alert: AlertModel,
)

data class AlertModel(
    val title: String,
    val body: String,
)
