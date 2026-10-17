package io.github.cumsauce.doorswing;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Minimal runtime config: config/doorswing.properties. */
public final class DoorSwingConfig {
    private static final String KEY_FOLEY_SOUNDS = "foley.sounds";
    private static volatile boolean foleySounds = true;

    private DoorSwingConfig() {
    }

    /** Whether the Foley door-sound takeover is enabled (fallback: vanilla sounds). */
    public static boolean foleySounds() {
        return foleySounds;
    }

    /** Loads config/doorswing.properties, creating it with defaults on first run. */
    public static void load() {
        Path path = Path.of("config", "doorswing.properties");
        try {
            if (Files.exists(path)) {
                Properties props = new Properties();
                try (Reader reader = Files.newBufferedReader(path)) {
                    props.load(reader);
                }
                foleySounds = Boolean.parseBoolean(props.getProperty(KEY_FOLEY_SOUNDS, "true"));
            } else {
                Files.createDirectories(path.getParent());
                Properties props = new Properties();
                props.setProperty(KEY_FOLEY_SOUNDS, "true");
                props.setProperty("comment", "Door Swing config. foley.sounds=false lets vanilla door sounds play even with Foley installed.");
                try (Writer writer = Files.newBufferedWriter(path)) {
                    props.store(writer, "Door Swing");
                }
            }
        } catch (IOException e) {
            // Keep defaults on any I/O problem.
        }
    }
}
