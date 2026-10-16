package io.github.cumsauce.doorswing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DoorAnimationTest {
    private static final float EPS = 1e-6f;

    @Test
    void easeBoundsAndMidpoint() {
        assertEquals(0f, DoorAnimation.ease(0f), EPS);
        assertEquals(1f, DoorAnimation.ease(1f), EPS);
        assertEquals(0f, DoorAnimation.ease(-3f), EPS);
        assertEquals(1f, DoorAnimation.ease(2f), EPS);
        assertEquals(0.5f, DoorAnimation.ease(0.5f), EPS);
    }

    @Test
    void easeIsMonotonic() {
        float prev = Float.NEGATIVE_INFINITY;
        for (int i = 0; i <= 100; i++) {
            float v = DoorAnimation.ease(i / 100f);
            assertTrue(v >= prev, "ease must be non-decreasing at t=" + i / 100f);
            prev = v;
        }
    }

    @Test
    void progressHitsEndpointsOnTime() {
        DoorAnimation anim = new DoorAnimation(0f, 1f, 1_000L);
        assertEquals(0f, anim.progress(1_000L), EPS);
        assertEquals(1f, anim.progress(1_000L + DoorAnimation.DURATION_MS), EPS);
        assertEquals(1f, anim.progress(10_000L), EPS);
        assertFalse(anim.isDone(1_000L + DoorAnimation.DURATION_MS - 1));
        assertTrue(anim.isDone(1_000L + DoorAnimation.DURATION_MS));
    }

    @Test
    void closingAnimationStartsAtCurrentProgress() {
        DoorAnimation anim = new DoorAnimation(0.4f, 0f, 5_000L);
        assertEquals(0.4f, anim.progress(5_000L), EPS);
        assertEquals(0f, anim.progress(5_000L + DoorAnimation.DURATION_MS), EPS);
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
