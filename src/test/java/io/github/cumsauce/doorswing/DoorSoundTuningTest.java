package io.github.cumsauce.doorswing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DoorSoundTuningTest {
    private static final float EPS = 1e-6f;

    @Test
    void volumesStayUnitAndMonotonic() {
        float prevCreak = -1f;
        float prevClunk = -1f;
        for (float speed = 0f; speed <= 10f; speed += 0.25f) {
            float creak = DoorSoundTuning.creakVolume(speed);
            float clunk = DoorSoundTuning.clunkVolume(speed);
            assertTrue(creak >= 0f && creak <= 1f, "creak volume out of range at " + speed);
            assertTrue(clunk >= 0f && clunk <= 1f, "clunk volume out of range at " + speed);
            assertTrue(creak >= prevCreak - EPS, "creak volume decreased at " + speed);
            assertTrue(clunk >= prevClunk - EPS, "clunk volume decreased at " + speed);
            prevCreak = creak;
            prevClunk = clunk;
        }
    }

    @Test
    void creakPitchRisesAndStaysBounded() {
        float prev = -1f;
        for (float speed = 0f; speed <= 10f; speed += 0.25f) {
            float pitch = DoorSoundTuning.creakPitch(speed);
            assertTrue(pitch >= 0.85f - EPS, "pitch below floor at " + speed);
            assertTrue(pitch <= 1.35f + EPS, "pitch above ceiling at " + speed);
            assertTrue(pitch >= prev - EPS, "pitch decreased at " + speed);
            prev = pitch;
        }
    }

    @Test
    void cooldownsShrinkWithSpeed() {
        assertTrue(DoorSoundTuning.creakCooldownMs(1f) > DoorSoundTuning.creakCooldownMs(4f));
        assertTrue(DoorSoundTuning.creakCooldownMs(4f) > DoorSoundTuning.creakCooldownMs(10f));
        assertTrue(DoorSoundTuning.creakCooldownMs(10f) > 0);

        assertTrue(DoorSoundTuning.scrapeCooldownMs(1f) > DoorSoundTuning.scrapeCooldownMs(4f));
        assertTrue(DoorSoundTuning.scrapeCooldownMs(4f) > DoorSoundTuning.scrapeCooldownMs(10f));
        assertTrue(DoorSoundTuning.scrapeCooldownMs(10f) > 0);
    }

    @Test
    void squeakChanceClamped() {
        assertEquals(0f, DoorSoundTuning.squeakChance(0f), EPS);
        assertEquals(1f, DoorSoundTuning.squeakChance(1000f), EPS);
    }

    @Test
    void clunkVolumeCaps() {
        assertEquals(1f, DoorSoundTuning.clunkVolume(1000f), EPS);
    }
}
