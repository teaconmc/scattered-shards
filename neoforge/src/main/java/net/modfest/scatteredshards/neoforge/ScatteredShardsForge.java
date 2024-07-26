package net.modfest.scatteredshards.neoforge;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.client.ScatteredShardsClient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(ScatteredShards.ID)
public class ScatteredShardsForge {

    ScatteredShards scatteredShards = new ScatteredShards();

    public ScatteredShardsForge(IEventBus eventBus) {
        scatteredShards.onInitialize();
        if (Platform.getEnvironment() == Env.CLIENT) {
            eventBus.register(ClientLogic.ModEventBusListener.class);
//            NeoForge.EVENT_BUS.register(ClientLogic.ForgeEventBusListener.class);
        }
    }

    private static class ClientLogic {

        private static final ScatteredShardsClient scatteredShardsClient = new ScatteredShardsClient();

        private static class ModEventBusListener {

            @SubscribeEvent
            public static void onClientSetupEvent(FMLClientSetupEvent event) {
                scatteredShardsClient.onInitializeClient();
            }
        }

        private static class ForgeEventBusListener {

        }
    }
}
