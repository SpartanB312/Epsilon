package club.eridani.epsilon.client.module.client

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.module.Module

internal object NotificationRender : Module(
    name = "Notification",
    alias = arrayOf("Information"),
    category = Category.Client,
    description = "Setting for notification"
) {

    val duration by setting("Duration", 3, 2..10, 1)
    val yDisplacement by setting("Y Displacement", 0, 0..150, 1)

}