package io.github.cottonmc.cotton.gui.impl.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;

import io.github.cottonmc.cotton.gui.impl.Proxy;
import io.github.cottonmc.cotton.gui.impl.ScreenNetworkingImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LibGuiClient implements ClientModInitializer {
	public static final Logger logger = LogManager.getLogger();
	public static volatile LibGuiConfig config;

	@Override
	public void onInitializeClient() {
		config = loadConfig();

		ClientPlayNetworking.registerGlobalReceiver(ScreenNetworkingImpl.ScreenMessage.ID, (payload, context) -> {
			ScreenNetworkingImpl.handle(context.client(), context.player(), payload);
		});

		LibGuiShaders.register();
		Proxy.proxy = new ClientProxy();
	}

	public static LibGuiConfig loadConfig() {
		return new LibGuiConfig();
	}

	public static void saveConfig(LibGuiConfig config) {

	}
}
