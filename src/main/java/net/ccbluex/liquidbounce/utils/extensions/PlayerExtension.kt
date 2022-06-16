/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.utils.extensions

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.monster.EntityGhast
import net.minecraft.entity.monster.EntityGolem
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.monster.EntitySlime
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.passive.EntityBat
import net.minecraft.entity.passive.EntitySquid
import net.minecraft.entity.passive.EntityVillager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Allows to get the distance between the current entity and [entity] from the nearest corner of the bounding box
 */
fun Entity.getDistanceToEntityBox(entity: Entity): Double {
    val eyes = this.getPositionEyes(1F)
    val size = entity.collisionBorderSize.toDouble()
    val pos = getNearestPointBB(eyes, entity.entityBoundingBox.expand(size, size, size))
    val dist = pos.subtract(eyes)
    return sqrt(dist.xCoord.pow(2) + dist.yCoord.pow(2) + dist.zCoord.pow(2))
}

fun getNearestPointBB(eye: Vec3, box: AxisAlignedBB): Vec3 {
    val origin = doubleArrayOf(eye.xCoord, eye.yCoord, eye.zCoord)
    val destMins = doubleArrayOf(box.minX, box.minY, box.minZ)
    val destMaxs = doubleArrayOf(box.maxX, box.maxY, box.maxZ)
    for (i in 0..2) {
        origin[i] = origin[i].coerceIn(destMins[i], destMaxs[i])
    }
    return Vec3(origin[0], origin[1], origin[2])
}

fun EntityPlayer.getPing(): Int {
    return MinecraftInstance.mc.netHandler.getPlayerInfo(uniqueID).responseTime
}

fun Entity.isAnimal(): Boolean {
    return this is EntityAnimal || this is EntitySquid || this is EntityGolem || this is EntityBat
}

fun Entity.isMob(): Boolean {
    return this is EntityMob || this is EntityVillager || this is EntitySlime || this is EntityGhast || this is EntityDragon
}

fun EntityPlayer.isClientFriend(): Boolean {
    val entityName = name ?: return false

    return LiquidBounce.fileManager.friendsConfig.isFriend(stripColor(entityName))
}

val Entity.rotation: Rotation
    get() = Rotation(rotationYaw, rotationPitch)