package io.github.cumsauce.doorswing;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public final class DoorSwingClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DoorSwingConfig.load();
        DoorSwingRenderer.register();
        // COLLECT_SUBMITS stops firing once the world unloads, so the renderer's
        // per-frame cleanup never runs. Clear eagerly so stale entries cannot hide
        // doors at the same positions while the next world's chunks are meshing.
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            DoorSwingRenderer.reset();
            DoorSwingIntegration.driver().clear();
        });
    }
}
