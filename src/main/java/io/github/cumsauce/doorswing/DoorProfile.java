package io.github.cumsauce.doorswing;

/**
 * Per-door-style animation feel: a duration plus an easing curve. The curve maps a
 * clamped t in [0, 1] to eased progress, which may overshoot [0, 1] slightly for
 * springy doors (the linear angle math tolerates progress outside [0, 1]).
 */
public enum DoorProfile {
    /** Default wooden doors: 300 ms smoothstep. */
    NORMAL(300L),
    /** Iron and other heavy redstone doors: long weighty ease, no bounce. */
    HEAVY(500L),
    /** Light wood (cherry, bamboo): snappy with a hint of overshoot. */
    LIGHT(240L),
    /** Copper: medium swing that overshoots and settles like a latch catching. */
    FLOURISH(420L);

    public final long durationMs;

    DoorProfile(long durationMs) {
        this.durationMs = durationMs;
    }

    /** Eased output at t; input clamped to [0, 1]. May return slightly below 0 or above 1. */
    public float apply(float t) {
        float c = t < 0f ? 0f : (t > 1f ? 1f : t);
        return switch (this) {
            case NORMAL -> c * c * (3f - 2f * c);                  // smoothstep
            case HEAVY -> c * c * c * (c * (c * 6f - 15f) + 10f);  // smootherstep
            case LIGHT -> backOut(c, 1.2f);
            case FLOURISH -> backOut(c, 2.0f);
        };
    }

    /** easeOutBack: settles at exactly 1 with a damped overshoot controlled by c1. */
    private static float backOut(float c, float c1) {
        float inv = c - 1f;
        return 1f + (c1 + 1f) * inv * inv * inv + c1 * inv * inv;
    }
}
