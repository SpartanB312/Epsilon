package club.eridani.epsilon.client.launch

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.spongepowered.asm.launch.MixinBootstrap
import org.spongepowered.asm.mixin.MixinEnvironment
import org.spongepowered.asm.mixin.Mixins

object MixinLoader {

    private val log: Logger = LogManager.getLogger("MIXIN")

    @Volatile
    private var idk = false

    fun load() {
        if (idk) return
        idk = true

        MixinBootstrap.init()
        log.info("Initializing mixins")
        Mixins.addConfigurations("mixins.epsilonloader.json", "mixins.baritone.json")
        MixinEnvironment.getDefaultEnvironment().obfuscationContext = "searge"
        MixinEnvironment.getDefaultEnvironment().side = MixinEnvironment.Side.CLIENT
        log.info("Epsilon mixins initialized")
        log.info(MixinEnvironment.getDefaultEnvironment().obfuscationContext)
    }

}