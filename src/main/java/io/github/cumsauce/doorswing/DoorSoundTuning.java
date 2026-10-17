package io.github.cumsauce.doorswing;

/** Velocity-to-perception scaling for door movement sounds. Pure math, unit tested. */
public final class DoorSoundTuning {
    private DoorSoundTuning() {
    }

    /** Swing speed (progress/s) to creak volume. */
    public static float creakVolume(float speed) {
        return clamp01(0.25f + speed * 0.18f);
    }

    /** Swing speed to creak playback pitch. */
    public static float creakPitch(float speed) {
        return 0.85f + clamp01(speed * 0.12f) * 0.5f;
    }

    /** Milliseconds between creak one-shots; faster swings play more often. */
    public static long creakCooldownMs(float speed) {
        return (long) (80.0 + 320.0 / Math.max(0.5f, speed));
    }

    /** Milliseconds between scrape one-shots. */
    public static long scrapeCooldownMs(float speed) {
        return (long) (50.0 + 200.0 / Math.max(0.5f, speed));
    }

    /** Per-call probability scale for hinge squeaks while moving. */
    public static float squeakChance(float speed) {
        return clamp01(speed * 0.015f);
    }

    /** Peak swing speed to endpoint clunk volume. */
    public static float clunkVolume(float peakSpeed) {
        return clamp01(0.2f + peakSpeed * 0.12f);
    }

    static float clamp01(float v) {
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }
}
