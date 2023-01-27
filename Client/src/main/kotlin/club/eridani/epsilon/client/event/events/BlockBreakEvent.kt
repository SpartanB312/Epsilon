package club.eridani.epsilon.client.event.events

import club.eridani.epsilon.client.event.Event
import net.minecraft.util.math.BlockPos

class BlockBreakEvent(val breakerID: Int, val position: BlockPos, val progress: Int) : Event()