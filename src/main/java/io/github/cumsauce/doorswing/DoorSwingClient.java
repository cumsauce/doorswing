package io.github.cumsauce.doorswing;

import net.fabricmc.api.ClientModInitializer;

public final class DoorSwingClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DoorSwingRenderer.register();
    }
}
