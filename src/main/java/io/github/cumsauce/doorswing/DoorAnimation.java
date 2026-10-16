package io.github.cumsauce.doorswing;

/**
 * One in-flight door swing: progress p moves from startP to targetP (both in [0, 1],
 * 0 = fully closed, 1 = fully open) over {@link #DURATION_MS} with a smoothstep ease.
 */
public final class DoorAnimation {
    public static final long DURATION_MS = 300L;

    public final float startP;
    public final float targetP;
    public final long startTimeMs;

    public DoorAnimation(float startP, float targetP, long startTimeMs) {
        this.startP = startP;
        this.targetP = targetP;
        this.startTimeMs = startTimeMs;
    }

    /** Smoothstep ease 3t²-2t³ with t clamped to [0, 1]. */
    public static float ease(float t) {
        float c = t < 0f ? 0f : (t > 1f ? 1f : t);
        return c * c * (3f - 2f * c);
    }

    /** Progress at the given time. */
    public float progress(long nowMs) {
        float t = (float) (nowMs - startTimeMs) / (float) DURATION_MS;
        return startP + (targetP - startP) * ease(t);
    }

    public boolean isDone(long nowMs) {
        return nowMs - startTimeMs >= DURATION_MS;
    }

    /** Model-pose rotation in degrees: hinge left swings -90°, hinge right +90° at full open. */
    public static float angleDeg(float progress, boolean hingeLeft) {
        return hingeLeft ? -90f * progress : 90f * progress;
    }

    /** Which vanilla OPEN model branch to render for a progress value. */
    public static boolean openModel(float progress) {
        return progress >= 0.5f;
    }
}
