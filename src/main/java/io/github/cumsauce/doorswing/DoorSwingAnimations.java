package io.github.cumsauce.doorswing;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks in-flight door animations keyed by {@link BlockPos#asLong()}.
 * Thread-safe: entries are written from the client thread (setBlock mixin) and read from
 * render and chunk-compile worker threads.
 */
public final class DoorSwingAnimations {
    private static final ConcurrentHashMap<Long, DoorAnimation> ANIMS = new ConcurrentHashMap<>();

    private DoorSwingAnimations() {
    }

    public static void start(long posKey, float fromP, float toP, long nowMs) {
        ANIMS.put(posKey, new DoorAnimation(fromP, toP, nowMs));
    }

    public static boolean isAnimating(long posKey) {
        return ANIMS.containsKey(posKey);
    }

    /** Current progress, or NaN if there is no animation for this position. */
    public static float currentP(long posKey, long nowMs) {
        DoorAnimation anim = ANIMS.get(posKey);
        return anim == null ? Float.NaN : anim.progress(nowMs);
    }

    /** Drops an entry without re-dirtying (door broken/replaced or state reset). */
    public static void remove(long posKey) {
        ANIMS.remove(posKey);
    }

    /**
     * Ends an animation: removes the entry FIRST, then re-dirties the section (and its
     * neighbors) so the chunk mesh is rebuilt with the real door state again.
     */
    public static void finish(long posKey) {
        if (ANIMS.remove(posKey) == null) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        BlockPos pos = BlockPos.of(posKey);
        if (!level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
            return;
        }
        level.setSectionDirtyWithNeighbors(
                SectionPos.blockToSectionCoord(pos.getX()),
                SectionPos.blockToSectionCoord(pos.getY()),
                SectionPos.blockToSectionCoord(pos.getZ()));
    }

    public static boolean isEmpty() {
        return ANIMS.isEmpty();
    }

    public static void clear() {
        ANIMS.clear();
    }

    /** Live (weakly consistent) view for the renderer. */
    public static Iterable<Map.Entry<Long, DoorAnimation>> entries() {
        return ANIMS.entrySet();
    }
}
