package io.github.cumsauce.doorswing.mixin;

import io.github.cumsauce.doorswing.DoorSwingAnimations;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Starts door animations when the client applies a block update that flips DoorBlock.OPEN
 * (server sends both halves as separate updates; each animates independently).
 */
@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @Inject(method = "setBlock", at = @At("HEAD"))
    private void doorswing$onSetBlock(BlockPos pos, BlockState state, int flags, int maxUpdateDepth,
                                      CallbackInfoReturnable<Boolean> cir) {
        ClientLevel self = (ClientLevel) (Object) this;
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
