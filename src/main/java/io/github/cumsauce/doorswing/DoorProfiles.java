package io.github.cumsauce.doorswing;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopperDoorBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Maps a door BlockState to its animation profile. */
public final class DoorProfiles {
    private DoorProfiles() {
    }

    public static DoorProfile of(BlockState state) {
        if (state.getBlock() == Blocks.IRON_DOOR) {
            return DoorProfile.HEAVY;
        }
        if (state.getBlock() instanceof WeatheringCopperDoorBlock) {
            // Covers every oxidation/waxed stage.
            return DoorProfile.FLOURISH;
        }
        if (state.getBlock() == Blocks.CHERRY_DOOR || state.getBlock() == Blocks.BAMBOO_DOOR) {
            return DoorProfile.LIGHT;
        }
        return DoorProfile.NORMAL;
    }
}
