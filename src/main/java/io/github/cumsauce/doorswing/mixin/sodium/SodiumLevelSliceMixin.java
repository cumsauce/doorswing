package io.github.cumsauce.doorswing.mixin.sodium;

import io.github.cumsauce.doorswing.DoorSwingAnimations;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Sodium variant of the vanilla section-mesh suppression.
 *
 * <p>Sodium's chunk mesher does not use {@code RenderSectionRegion}; it reads every block
 * state from {@code net.caffeinemc.mods.sodium.client.world.LevelSlice}. Without this hook
 * the real door is baked into the Sodium section mesh in its final open pose while the
 * animated copy is still swinging, so the door appears twice until the swing ends.
 *
 * <p>Targeted by name because Sodium is an optional runtime dependency: the class only
 * exists when Sodium is installed. Coordinates are absolute world coordinates.
 */
@Mixin(targets = "net.caffeinemc.mods.sodium.client.world.LevelSlice")
public class SodiumLevelSliceMixin {

    @Inject(
            method = "getBlockState(III)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void doorswing$skipAnimatingDoor(int x, int y, int z, CallbackInfoReturnable<BlockState> cir) {
        if (DoorSwingAnimations.isEmpty()) {
            return;
        }
        if (DoorSwingAnimations.isAnimating(BlockPos.asLong(x, y, z))) {
            cir.setReturnValue(Blocks.AIR.defaultBlockState());
        }
    }
}
