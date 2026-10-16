package io.github.cumsauce.doorswing;

/**
 * One in-flight door swing: progress p moves from startP to targetP (0 = fully closed,
 * 1 = fully open) over the bound {@link DoorProfile}'s duration with its easing curve.
 * Progress may briefly overshoot the endpoints for springy profiles.
 */
public final class DoorAnimation {
    public final float startP;
    public final float targetP;
    public final long startTimeMs;
    public final DoorProfile profile;

    public DoorAnimation(float startP, float targetP, long startTimeMs, DoorProfile profile) {
        this.startP = startP;
        this.targetP = targetP;
        this.startTimeMs = startTimeMs;
        this.profile = profile;
    }

    /** Progress at the given time. */
    public float progress(long nowMs) {
        float t = (float) (nowMs - startTimeMs) / (float) profile.durationMs;
        return startP + (targetP - startP) * profile.apply(t);
    }

    public boolean isDone(long nowMs) {
        return nowMs - startTimeMs >= profile.durationMs;
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
