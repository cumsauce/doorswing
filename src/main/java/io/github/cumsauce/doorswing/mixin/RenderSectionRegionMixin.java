package io.github.cumsauce.doorswing.mixin;

import io.github.cumsauce.doorswing.DoorSwingAnimations;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * While a door is animating, the chunk compiler sees air at its position, so the door is
 * left out of the section mesh (the renderer draws the animated pose instead). Runs on
 * worker threads; the tracker is a ConcurrentHashMap for that reason.
 */
@Mixin(RenderSectionRegion.class)
public class RenderSectionRegionMixin {

    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    private void doorswing$skipAnimatingDoor(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if (DoorSwingAnimations.isAnimating(pos.asLong())) {
            cir.setReturnValue(Blocks.AIR.defaultBlockState());
        }
    }
}
