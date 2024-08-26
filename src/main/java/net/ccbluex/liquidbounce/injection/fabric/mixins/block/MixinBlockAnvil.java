package net.ccbluex.liquidbounce.injection.fabric.mixins.block;

import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilBlock.class)
public abstract class MixinAnvilBlock extends MixinBlock {

    @Inject(method = "onBlockPlaced", cancellable = true, at = @At("HEAD"))
    private void injectAnvilCrashFix(World worldIn, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer, CallbackInfoReturnable<BlockState> cir) {
        if (((meta >> 2) & ~0x3) != 0) {
            cir.setReturnValue(super.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(AnvilBlock.FACING, placer.getHorizontalFacing().rotateY()).withProperty(AnvilBlock.DAMAGE, 2));
            cir.cancel();
        }
    }
}
