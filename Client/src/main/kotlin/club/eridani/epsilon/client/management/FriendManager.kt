package club.eridani.epsilon.client.management

import club.eridani.epsilon.client.util.text.ChatUtil
import net.minecraft.entity.Entity

object FriendManager {

    val friendList = mutableListOf<String>()
    
    fun isFriend(entity: Entity): Boolean {
        return friendList.contains(entity.uniqueID.toString().replace("-", ""))
    }

    fun isFriend(uuid: String): Boolean {
        return friendList.contains(uuid)
    }

    fun addFriend(uuid: String) : Boolean {
        return if (isFriend(uuid)) {
            false
        } else {
            friendList.add(uuid)
            true
        }
    }

    fun removeFriend(uuid: String) : Boolean {
        return if (isFriend(uuid)) {
            friendList.remove(uuid)
            true
        } else {
            false
        }
    }

    fun addFriend(entity: Entity) {
        if (isFriend(entity)) {
            ChatUtil.sendNoSpamMessage(ChatUtil.SECTION_SIGN + "b" + entity.name + ChatUtil.SECTION_SIGN + "r already is your friend.", 2133)
        } else {
            ChatUtil.sendNoSpamMessage(ChatUtil.SECTION_SIGN + "b" + entity.name + ChatUtil.SECTION_SIGN + "r has been friended.", 2133)
            friendList.add(entity.uniqueID.toString().replace("-", ""))
        }
    }
    
    fun removeFriend(entity: Entity) {
        if (isFriend(entity)) {
            friendList.remove(entity.uniqueID.toString().replace("-", ""))
            ChatUtil.sendNoSpamMessage(ChatUtil.SECTION_SIGN + "b" + entity.name + ChatUtil.SECTION_SIGN + "r has been unfriended.", 2133)
        } else {
            ChatUtil.sendNoSpamMessage(ChatUtil.SECTION_SIGN.toString() + "b" + entity.name + ChatUtil.SECTION_SIGN + "r is not your friend", 2133)
        }
    }

}