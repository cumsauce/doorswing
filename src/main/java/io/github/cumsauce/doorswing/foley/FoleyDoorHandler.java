package io.github.cumsauce.doorswing.foley;

import io.github.cumsauce.doorswing.DoorProfiles;
import io.github.cumsauce.doorswing.DoorSoundDriver;
import io.github.cumsauce.doorswing.DoorSoundTuning;
import io.github.cumsauce.doorswing.DoorSwingConfig;
import io.github.cumsauce.doorswing.DoorProfile;
import io.github.cumsauce.foley.api.DoorSoundHandler;
import io.github.cumsauce.foley.api.Foley;
import io.github.cumsauce.foley.api.FoleyApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Bridges doorswing's animation tracker to Foley: takes over vanilla door/trapdoor
 * events and plays creak/scrape/squeak/clunk one-shots scaled by swing speed.
 */
public final class FoleyDoorHandler implements DoorSoundHandler, DoorSoundDriver {
    private final FoleyApi api;
    private final ConcurrentHashMap<Long, DoorState> states = new ConcurrentHashMap<>();

    public FoleyDoorHandler(FoleyApi api) {
        this.api = api;
    }

    private static String family(DoorProfile profile) {
        return switch (profile) {
            case NORMAL -> "wood";
            case HEAVY -> "metal";
            case LIGHT -> "light";
            case FLOURISH -> "copper";
        };
    }

    /** Family from the actual block at the event position, or null when it is not a door. */
    private static String blockFamily(double x, double y, double z) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }
        BlockPos pos = BlockPos.containing(x, y, z);
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof DoorBlock)) {
            return null;
        }
        return family(DoorProfiles.of(state));
    }

    @Override
    public boolean onVanillaDoorEvent(Identifier event, double x, double y, double z, boolean iron) {
        if (!DoorSwingConfig.foleySounds()) {
            return false;
        }
        boolean trapdoor = event.getPath().contains("trapdoor");
        String family = blockFamily(x, y, z);
        if (family == null) {
            family = iron ? "metal" : "wood";
        }
        if (trapdoor) {
            // Doorswing does not animate trapdoors, so keep Foley's old tap: one clunk.
            api.sounds().play(DoorSwingFoley.id("clunk_" + family), x, y, z, SoundSource.BLOCKS, 0.5f, 1f);
            return true;
        }
        // Doors also animate, so seed a short movement burst here.
        api.sounds().play(DoorSwingFoley.id("creak_" + family), x, y, z, SoundSource.BLOCKS, 0.35f, 1f);
        api.sounds().play(DoorSwingFoley.id("scrape_" + family), x, y, z, SoundSource.BLOCKS, 0.3f, 1f);
        return true;
    }

    @Override
    public void onDoorMoving(long posKey, double x, double y, double z, DoorProfile profile, float progress, long nowMs) {
        if (!DoorSwingConfig.foleySounds() || !Foley.available()) {
            return;
        }
        DoorState s = states.computeIfAbsent(posKey, k -> new DoorState());
        if (s.lastMs == 0) {
            s.lastP = progress;
            s.lastMs = nowMs;
            return;
        }
        float dt = Math.max(0.001f, (nowMs - s.lastMs) / 1000f);
        float raw = Math.abs(progress - s.lastP) / dt;
        s.speed = s.speed * 0.6f + raw * 0.4f;
        s.peakSpeed = Math.max(s.peakSpeed, s.speed);
        s.lastP = progress;
        s.lastMs = nowMs;
        if (s.speed < 0.05f) {
            return;
        }

        String family = family(profile);
        if (nowMs >= s.nextCreakMs) {
            api.sounds().play(DoorSwingFoley.id("creak_" + family), x, y, z, SoundSource.BLOCKS,
                    DoorSoundTuning.creakVolume(s.speed), DoorSoundTuning.creakPitch(s.speed));
            s.nextCreakMs = nowMs + DoorSoundTuning.creakCooldownMs(s.speed);
        }
        if (nowMs >= s.nextScrapeMs) {
            api.sounds().play(DoorSwingFoley.id("scrape_" + family), x, y, z, SoundSource.BLOCKS,
                    Math.min(1f, 0.2f + s.speed * 0.15f), DoorSoundTuning.creakPitch(s.speed));
            s.nextScrapeMs = nowMs + DoorSoundTuning.scrapeCooldownMs(s.speed);
        }
        if (!family.equals("metal")
                && ThreadLocalRandom.current().nextFloat() < DoorSoundTuning.squeakChance(s.speed)) {
            api.sounds().play(DoorSwingFoley.id("squeak_wood"), x, y, z, SoundSource.BLOCKS, 0.4f,
                    0.9f + ThreadLocalRandom.current().nextFloat() * 0.3f);
        }
    }

    @Override
    public void onDoorFinished(long posKey, double x, double y, double z, DoorProfile profile) {
        DoorState s = states.remove(posKey);
        float peak = s == null ? 1f : Math.max(0.5f, s.peakSpeed);
        if (!DoorSwingConfig.foleySounds() || !Foley.available()) {
            return;
        }
        api.sounds().play(DoorSwingFoley.id("clunk_" + family(profile)), x, y, z, SoundSource.BLOCKS,
                DoorSoundTuning.clunkVolume(peak), 1f);
    }

    @Override
    public void clear() {
        states.clear();
    }

    private static final class DoorState {
        float lastP;
        long lastMs;
        float speed;
        float peakSpeed;
        long nextCreakMs;
        long nextScrapeMs;
    }
}
