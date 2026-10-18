package io.github.cumsauce.doorswing;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DoorSwingClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("doorswing");

    @Override
    public void onInitializeClient() {
        DoorSwingConfig.load();
        DoorSwingRenderer.register();
        if (FabricLoader.getInstance().isModLoaded("sodium")) {
            LOGGER.info("Sodium detected: animating doors are hidden from Sodium chunk meshes");
        }
        // COLLECT_SUBMITS stops firing once the world unloads, so the renderer's
        // per-frame cleanup never runs. Clear eagerly so stale entries cannot hide
        // doors at the same positions while the next world's chunks are meshing.
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            DoorSwingRenderer.reset();
            DoorSwingIntegration.driver().clear();
        });
    }
}
