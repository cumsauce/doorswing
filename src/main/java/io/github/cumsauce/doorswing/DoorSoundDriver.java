package io.github.cumsauce.doorswing;

/**
 * Sink for door movement sounds. The Foley-backed implementation is installed by
 * the optional foley entrypoint; until then {@link #NONE} makes every call a no-op.
 * Implementations must be fast and non-blocking (called from the render loop).
 */
public interface DoorSoundDriver {
    DoorSoundDriver NONE = new DoorSoundDriver() { };

    /** A vanilla door/trapdoor open/close event was intercepted. True = we own it (stays skipped). */
    default boolean onVanillaDoorEvent(double x, double y, double z, boolean iron) {
        return false;
    }

    /** Called every frame while a door animates. progress in [0,1] (may briefly overshoot). */
    default void onDoorMoving(long posKey, double x, double y, double z, DoorProfile profile, float progress, long nowMs) {
    }

    /** Called exactly once when an animation finishes (endpoint reached). */
    default void onDoorFinished(long posKey, double x, double y, double z, DoorProfile profile) {
    }

    /** Drops per-door state (disconnect). */
    default void clear() {
    }
}
