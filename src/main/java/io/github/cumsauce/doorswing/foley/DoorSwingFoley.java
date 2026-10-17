package io.github.cumsauce.doorswing.foley;

import io.github.cumsauce.doorswing.DoorSwingIntegration;
import io.github.cumsauce.foley.api.FoleyApi;
import io.github.cumsauce.foley.api.FoleyEntrypoint;
import io.github.cumsauce.foley.api.MaterialDescriptor;
import io.github.cumsauce.foley.api.SoundModel;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.SoundType;

/**
 * Foley addon entrypoint: registers synthesized door sounds + materials and
 * installs the handler that takes over vanilla door/trapdoor events.
 * Only ever loaded when Foley is present (fabric.mod.json "foley" entrypoint).
 */
public final class DoorSwingFoley implements FoleyEntrypoint {
    @Override
    public void onFoleyReady(FoleyApi api) {
        registerSounds(api);
        registerMaterials(api);
        FoleyDoorHandler handler = new FoleyDoorHandler(api);
        api.doorHandler(handler);
        DoorSwingIntegration.setDriver(handler);
        ClientPlayConnectionEvents.DISCONNECT.register((networkHandler, client) -> {
            handler.clear();
            DoorSwingIntegration.setDriver(null);
        });
    }

    static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath("doorswing", name);
    }

    private static void registerSounds(FoleyApi api) {
        // Wood family (oak, spruce, ... default profile)
        api.sounds().register(id("creak_wood"), new SoundModel.Tone(110f, 170f, 1.4f, 0.55f), 8);
        api.sounds().register(id("scrape_wood"), new SoundModel.NoiseBurst(1600f, 2.2f, 0.22f, 0.35f), 8);
        api.sounds().register(id("squeak_wood"), new SoundModel.Bubble(0.6f), 4);
        api.sounds().register(id("clunk_wood"), new SoundModel.Contact(SoundModel.ContactKind.PLACE, SoundModel.MaterialKind.WOOD, 2.5f), 6);
        // Metal family (iron doors, heavy profile)
        api.sounds().register(id("creak_metal"), new SoundModel.Tone(80f, 130f, 1.6f, 0.6f), 8);
        api.sounds().register(id("scrape_metal"), new SoundModel.NoiseBurst(2400f, 1.6f, 0.18f, 0.45f), 8);
        api.sounds().register(id("clunk_metal"), new SoundModel.Contact(SoundModel.ContactKind.PLACE, SoundModel.MaterialKind.METAL, 3.0f), 6);
        // Light family (cherry/bamboo, light profile)
        api.sounds().register(id("creak_light"), new SoundModel.Tone(160f, 240f, 1.1f, 0.5f), 8);
        api.sounds().register(id("scrape_light"), new SoundModel.NoiseBurst(2000f, 2.0f, 0.16f, 0.3f), 8);
        api.sounds().register(id("clunk_light"), new SoundModel.Contact(SoundModel.ContactKind.PLACE, SoundModel.MaterialKind.WOOD, 1.8f), 6);
        // Copper family (copper doors, flourish profile)
        api.sounds().register(id("creak_copper"), new SoundModel.Tone(90f, 140f, 1.5f, 0.55f), 8);
        api.sounds().register(id("scrape_copper"), new SoundModel.NoiseBurst(2200f, 1.8f, 0.2f, 0.4f), 8);
        api.sounds().register(id("clunk_copper"), new SoundModel.Contact(SoundModel.ContactKind.PLACE, SoundModel.MaterialKind.METAL, 2.2f), 6);
    }

    private static void registerMaterials(FoleyApi api) {
        Identifier wood = id("door_wood");
        api.materials().register(wood, MaterialDescriptor.wooden());
        api.materials().mapSoundType(SoundType.WOOD, wood);
        Identifier metal = id("door_metal");
        api.materials().register(metal, MaterialDescriptor.metallic());
        api.materials().mapSoundType(SoundType.METAL, metal);
        // Copper: stiffer, denser than default metal, slight internal loss.
        Identifier copper = id("door_copper");
        api.materials().register(copper, new MaterialDescriptor(
                SoundModel.MaterialKind.METAL, 110e9, 8900.0, 0.34, 0.01, 0.03, 0, 0.5, 0.0));
        api.materials().mapSoundType(SoundType.COPPER, copper);
    }
}
