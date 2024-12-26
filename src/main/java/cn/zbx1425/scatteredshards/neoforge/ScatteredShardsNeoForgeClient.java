package cn.zbx1425.scatteredshards.neoforge;

import net.modfest.scatteredshards.ScatteredShardsConfig;
import net.modfest.scatteredshards.client.ScatteredShardsClient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public class ScatteredShardsNeoForgeClient {

	public ScatteredShardsNeoForgeClient(IEventBus eventBus, ModContainer container) {
		new ScatteredShardsClient().onInitializeClient();
		container.registerConfig(ModConfig.Type.CLIENT, ScatteredShardsConfig.CONFIG_SPEC);
		container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
	}
}
