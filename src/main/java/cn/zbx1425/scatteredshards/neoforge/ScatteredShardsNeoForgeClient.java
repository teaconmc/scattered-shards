package cn.zbx1425.scatteredshards.neoforge;

import io.github.cottonmc.cotton.gui.impl.client.LibGuiClient;
import net.modfest.scatteredshards.ScatteredShardsConfig;
import net.modfest.scatteredshards.client.ScatteredShardsClient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public class ScatteredShardsNeoForgeClient {

	public ScatteredShardsNeoForgeClient(IEventBus eventBus, ModContainer container) {
		container.registerConfig(ModConfig.Type.CLIENT, ScatteredShardsConfig.CONFIG_SPEC);
		container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
		eventBus.register(ModEventBusListener.class);
	}

	public static class ModEventBusListener {

		@SubscribeEvent
		public static void onClientSetupEvent(FMLClientSetupEvent event) {
			new LibGuiClient().onInitializeClient();
			new ScatteredShardsClient().onInitializeClient();
		}

		@SubscribeEvent
		public static void onRegisterKeyBinding(RegisterKeyMappingsEvent event) {
			event.register(ScatteredShardsClient.VIEW_COLLECTION);
		}
	}
}
