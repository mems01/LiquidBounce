/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.injection.fabric.mixins.render;

import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura;
import net.ccbluex.liquidbounce.features.module.modules.movement.NoSlow;
import net.minecraft.block.Block;
import net.minecraft.client.entity.ClientPlayerEntity;
import net.minecraft.client.render.entity.model.BiPedModel;
import net.minecraft.client.render.block.model.ItemCameraTransforms;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.HeldHeldItemRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

import static net.ccbluex.liquidbounce.utils.MinecraftInstance.mc;
import static com.mojang.blaze3d.platform.GlStateManager.*;

@Mixin(HeldHeldItemRenderer.class)
@SideOnly(Side.CLIENT)
public class MixinHeldHeldItemRenderer {

    @Shadow
    @Final
    private LivingEntityRenderer<?> livingEntityRenderer;

    /**
     * @author CCBlueX
     */
    @Overwrite
    public void doRenderLayer(LivingEntity LivingEntityIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {
        ItemStack itemstack = LivingEntityIn.getHeldItem();

        if (itemstack != null) {
            pushMatrix();

            if (livingEntityRenderer.getMainModel().isChild) {
                float f = 0.5F;
                translate(0f, 0.625F, 0f);
                rotate(-20f, -1f, 0f, 0f);
                scale(f, f, f);
            }

            final UUID uuid = LivingEntityIn.getUniqueID();
            final PlayerEntity PlayerEntity = mc.world.getPlayerEntityByUUID(uuid);

            if (PlayerEntity != null && (PlayerEntity.isBlocking() || PlayerEntity instanceof ClientPlayerEntity && ((itemstack.getItem() instanceof SwordItem && KillAura.INSTANCE.getRenderBlocking()) || NoSlow.INSTANCE.isUNCPBlocking()))) {
                if (LivingEntityIn.isSneaking()) {
                    ((BiPedModel) livingEntityRenderer.getMainModel()).postRenderArm(0.0325F);
                    translate(-0.58F, 0.3F, -0.2F);
                    rotate(-24390f, 137290f, -2009900f, -2054900f);
                } else {
                    ((BiPedModel) livingEntityRenderer.getMainModel()).postRenderArm(0.0325F);
                    translate(-0.48F, 0.2F, -0.2F);
                    rotate(-24390f, 137290f, -2009900f, -2054900f);
                }
            } else {
                ((BiPedModel) livingEntityRenderer.getMainModel()).postRenderArm(0.0625F);
            }

            translate(-0.0625F, 0.4375F, 0.0625F);

            if (LivingEntityIn instanceof PlayerEntity && ((PlayerEntity) LivingEntityIn).fishEntity != null) {
                itemstack = new ItemStack(Items.fishing_rod, 0);
            }

            Item item = itemstack.getItem();

            if (item instanceof BlockItem && Block.getBlockFromItem(item).getRenderType() == 2) {
                translate(0f, 0.1875F, -0.3125F);
                rotate(20f, 1f, 0f, 0f);
                rotate(45f, 0f, 1f, 0f);
                float f1 = 0.375F;
                scale(-f1, -f1, f1);
            }

            if (LivingEntityIn.isSneaking()) {
                translate(0f, 0.203125F, 0f);
            }

            mc.getHeldItemRenderer().renderItem(LivingEntityIn, itemstack, ItemCameraTransforms.TransformType.THIRD_PERSON);
            popMatrix();
        }
    }
}
