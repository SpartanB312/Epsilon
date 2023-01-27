package club.eridani.epsilon.client.notification

import club.eridani.epsilon.client.event.decentralized.IDecentralizedEvent
import club.eridani.epsilon.client.event.decentralized.Listenable
import club.eridani.epsilon.client.management.SpartanCore.addAsyncUpdateListener
import club.eridani.epsilon.client.module.client.NotificationRender
import club.eridani.epsilon.client.util.Timer
import club.eridani.epsilon.client.util.onRender2D
import java.util.concurrent.LinkedBlockingQueue

object NotificationManager : Listenable {

    override val subscribedListener = ArrayList<Triple<IDecentralizedEvent<*>, (Any) -> Unit, Int>>()

    private var lastRenderTime = System.currentTimeMillis()

    val timer = Timer()

    init {
        onRender2D {
            if (NotificationRender.isEnabled) render()
        }

        addAsyncUpdateListener {
            if (NotificationRender.isEnabled && System.currentTimeMillis() - lastRenderTime <= 10000) {
                if (timer.passed(13)) {
                    timer.reset()
                    notifications.forEach {
                        it.update()
                    }
                }
            }
        }
    }

    internal var notifications = LinkedBlockingQueue<Notification>()

    fun show(notificationIn: Notification) {
        if (NotificationRender.isEnabled) {
            notifications.add(notificationIn)
            lastRenderTime = System.currentTimeMillis()
        }
    }

    fun render() {
        notifications.forEach {
            it.draw()
        }
    }

}