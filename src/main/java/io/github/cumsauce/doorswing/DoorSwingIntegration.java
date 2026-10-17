package io.github.cumsauce.doorswing;

/**
 * Holds the optional sound driver. Kept foley-free so the core mod never loads
 * foley classes; the foley entrypoint installs the real implementation here.
 */
public final class DoorSwingIntegration {
    private static volatile DoorSoundDriver driver = DoorSoundDriver.NONE;

    private DoorSwingIntegration() {
    }

    public static DoorSoundDriver driver() {
        return driver;
    }

    public static void setDriver(DoorSoundDriver newDriver) {
        driver = newDriver == null ? DoorSoundDriver.NONE : newDriver;
    }
}
