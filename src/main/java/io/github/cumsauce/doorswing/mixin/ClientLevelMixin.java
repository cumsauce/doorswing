package io.github.cumsauce.doorswing.mixin;

import io.github.cumsauce.doorswing.DoorSwingAnimations;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Starts door animations when the level applies a block update that flips DoorBlock.OPEN.
 * Targets {@link Level#setBlock} (the common superclass) because server-verified updates
 * call it directly via invokespecial, bypassing ClientLevel's override.
 */
@Mixin(Level.class)
public class ClientLevelMixin {

    @Inject(method = "setBlock", at = @At("HEAD"))
    private void doorswing$onSetBlock(BlockPos pos, BlockState state, int flags, int maxUpdateDepth,
                                      CallbackInfoReturnable<Boolean> cir) {
        Level self = (Level) (Object) this;
        if (!(self instanceof ClientLevel)) {
            // Only client-side levels drive the render tracker; server levels (incl. singleplayer
            // integrated server) must not touch it.
            return;
        }
        BlockState old = self.getBlockState(pos);
        if (!(old.getBlock() instanceof DoorBlock)) {
            // Air -> door (placement) or non-door change: nothing to animate.
            return;
        }
        long key = pos.asLong();
        if (!(state.getBlock() instanceof DoorBlock)) {
            // Door broken or replaced: drop any in-flight animation; vanilla re-meshes.
            DoorSwingAnimations.remove(key);
            return;
        }
        boolean wasOpen = old.getValue(DoorBlock.OPEN);
        boolean isOpen = state.getValue(DoorBlock.OPEN);
        if (wasOpen == isOpen) {
            // POWERED-only or other unrelated update.
            return;
        }
        long now = System.currentTimeMillis();
        float current = DoorSwingAnimations.currentP(key, now);
        float fromP = Float.isNaN(current) ? (wasOpen ? 1f : 0f) : current;
        DoorSwingAnimations.start(key, fromP, isOpen ? 1f : 0f, now);
    }
}
