package club.eridani.epsilon.client.module.misc

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.module.Module
import java.awt.Desktop
import java.net.URI

object AutoPorn : Module(name = "AutoPorn", category = Category.Misc, description = "Auto go to porn website") {
    private val site by setting("Site", Site.nhentai)

    override fun onEnable() {
        if (site == Site.nhentai) {
            Desktop.getDesktop().browse(URI("https://nhentai.net/random/"))
        } else {
            Desktop.getDesktop().browse(URI("www." + site.name + ".com"))
        }
        disable(notification = false, silent = true)
    }

    enum class Site {
        nhentai, Pornhub, XVideos, Redtube, XHamster, YouPorn, Tube8, ThisAV
    }
}