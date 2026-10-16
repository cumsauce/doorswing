package io.github.cumsauce.doorswing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DoorAnimationTest {
    private static final float EPS = 1e-6f;

    @Test
    void normalEaseBoundsAndMidpoint() {
        assertEquals(0f, DoorProfile.NORMAL.apply(0f), EPS);
        assertEquals(1f, DoorProfile.NORMAL.apply(1f), EPS);
        assertEquals(0f, DoorProfile.NORMAL.apply(-3f), EPS);
        assertEquals(1f, DoorProfile.NORMAL.apply(2f), EPS);
        assertEquals(0.5f, DoorProfile.NORMAL.apply(0.5f), EPS);
    }

    @Test
    void normalAndHeavyAreMonotonic() {
        for (DoorProfile profile : new DoorProfile[]{DoorProfile.NORMAL, DoorProfile.HEAVY}) {
            float prev = Float.NEGATIVE_INFINITY;
            for (int i = 0; i <= 100; i++) {
                float v = profile.apply(i / 100f);
                assertTrue(v >= prev, profile + " must be non-decreasing at t=" + i / 100f);
                prev = v;
            }
        }
    }

    @Test
    void heavyIsSlowerOffTheLine() {
        // Smootherstep lags smoothstep early on: the door takes more "effort" to get moving.
        assertTrue(DoorProfile.HEAVY.apply(0.25f) < DoorProfile.NORMAL.apply(0.25f));
        assertEquals(0.5f, DoorProfile.HEAVY.apply(0.5f), EPS);
        assertEquals(500L, DoorProfile.HEAVY.durationMs);
    }

    @Test
    void overshootProfilesSettleExactly() {
        for (DoorProfile profile : new DoorProfile[]{DoorProfile.LIGHT, DoorProfile.FLOURISH}) {
            assertEquals(0f, profile.apply(0f), EPS, profile + " must start at 0");
            assertEquals(1f, profile.apply(1f), EPS, profile + " must settle at exactly 1");
            float peak = 0f;
            for (int i = 0; i <= 200; i++) {
                peak = Math.max(peak, profile.apply(i / 200f));
            }
            assertTrue(peak > 1.01f, profile + " must overshoot, peak=" + peak);
            assertTrue(peak < 1.25f, profile + " overshoot must stay subtle, peak=" + peak);
        }
        // Copper flourishes more than light wood.
        float lightPeak = 0f;
        float flourishPeak = 0f;
        for (int i = 0; i <= 200; i++) {
            lightPeak = Math.max(lightPeak, DoorProfile.LIGHT.apply(i / 200f));
            flourishPeak = Math.max(flourishPeak, DoorProfile.FLOURISH.apply(i / 200f));
        }
        assertTrue(flourishPeak > lightPeak);
    }

    @Test
    void durationsPerProfile() {
        assertEquals(300L, DoorProfile.NORMAL.durationMs);
        assertEquals(500L, DoorProfile.HEAVY.durationMs);
        assertEquals(240L, DoorProfile.LIGHT.durationMs);
        assertEquals(420L, DoorProfile.FLOURISH.durationMs);
    }

    @Test
    void progressHitsEndpointsOnTime() {
        DoorAnimation anim = new DoorAnimation(0f, 1f, 1_000L, DoorProfile.NORMAL);
        assertEquals(0f, anim.progress(1_000L), EPS);
        assertEquals(1f, anim.progress(1_000L + anim.profile.durationMs), EPS);
        assertEquals(1f, anim.progress(10_000L), EPS);
        assertFalse(anim.isDone(1_000L + anim.profile.durationMs - 1));
        assertTrue(anim.isDone(1_000L + anim.profile.durationMs));
    }

    @Test
    void overshootAnimationReachesTargetExactlyAtEnd() {
        DoorAnimation anim = new DoorAnimation(0f, 1f, 0L, DoorProfile.FLOURISH);
        // Mid-swing overshoot is allowed...
        assertTrue(anim.progress(anim.profile.durationMs * 3L / 4L) > 1f);
        // ...but the endpoint is exact.
        assertEquals(1f, anim.progress(anim.profile.durationMs), EPS);
    }

    @Test
    void closingAnimationStartsAtCurrentProgress() {
        DoorAnimation anim = new DoorAnimation(0.4f, 0f, 5_000L, DoorProfile.NORMAL);
        assertEquals(0.4f, anim.progress(5_000L), EPS);
        assertEquals(0f, anim.progress(5_000L + anim.profile.durationMs), EPS);
    }

    @Test
    void angleFormulaPerHinge() {
        assertEquals(-90f, DoorAnimation.angleDeg(1f, true), EPS);
        assertEquals(90f, DoorAnimation.angleDeg(1f, false), EPS);
        assertEquals(0f, DoorAnimation.angleDeg(0f, true), EPS);
        assertEquals(-45f, DoorAnimation.angleDeg(0.5f, true), EPS);
    }

    @Test
    void openModelBranchesAtHalf() {
        assertFalse(DoorAnimation.openModel(0f));
        assertFalse(DoorAnimation.openModel(0.49f));
        assertTrue(DoorAnimation.openModel(0.5f));
        assertTrue(DoorAnimation.openModel(1f));
    }
}
