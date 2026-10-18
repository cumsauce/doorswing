package io.github.cumsauce.doorswing;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SodiumCompatConfigTest {

    @Test
    void sodiumMixinConfigIsOptionalAndRegistered() throws IOException {
        String config = read("/doorswing.sodium.mixins.json");
        assertTrue(config.contains("\"required\": false"), "Sodium compat config must not be required");
        assertTrue(config.contains("SodiumLevelSliceMixin"), "Sodium mixin must be listed in its config");

        String modJson = read("/fabric.mod.json");
        assertTrue(modJson.contains("doorswing.sodium.mixins.json"),
                "fabric.mod.json must register the Sodium mixin config");
    }

    @Test
    void sodiumMixinTargetsLevelSliceStateLookup() throws IOException {
        Path source = Path.of("src/main/java/io/github/cumsauce/doorswing/mixin/sodium/SodiumLevelSliceMixin.java");
        assertTrue(Files.isRegularFile(source), "Sodium mixin source must exist at " + source);
        String content = Files.readString(source, StandardCharsets.UTF_8);
        assertTrue(content.contains("net.caffeinemc.mods.sodium.client.world.LevelSlice"),
                "mixin must target Sodium's LevelSlice");
        assertTrue(content.contains("getBlockState(III)"),
                "mixin must target the (int,int,int) state lookup used by Sodium's mesher");
    }

    private static String read(String resource) throws IOException {
        try (InputStream in = SodiumCompatConfigTest.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IOException("Missing resource " + resource);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
