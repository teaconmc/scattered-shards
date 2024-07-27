package vendor.cn.zbx1425.scatteredshards.cotton.gui.impl.client;

import dev.architectury.networking.NetworkManager;
import vendor.cn.zbx1425.scatteredshards.cotton.gui.impl.Proxy;
import vendor.cn.zbx1425.scatteredshards.cotton.gui.impl.ScreenNetworkingImpl;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LibGuiClient {
	public static final Logger logger = LogManager.getLogger();
	public static volatile LibGuiConfig config;
//
//	public static final Jankson jankson = JanksonFactory.createJankson();

	public void onInitializeClient() {
		config = new LibGuiConfig();

		System.out.println("ScreenMessageS2C Receiver: " + ScreenNetworkingImpl.ScreenMessageS2C.ID);
		NetworkManager.registerReceiver(NetworkManager.Side.S2C, ScreenNetworkingImpl.ScreenMessageS2C.ID,
			ScreenNetworkingImpl.ScreenMessageS2C.CODEC, (payload, context) -> {
			ScreenNetworkingImpl.handle(MinecraftClient.getInstance(), context.getPlayer(), payload);
		});

		LibGuiShaders.register();
		Proxy.proxy = new ClientProxy();
	}
//
//	public static LibGuiConfig loadConfig() {
//		try {
//			Path file = FabricLoader.getInstance().getConfigDir().resolve("libgui.json5");
//
//			if (Files.notExists(file)) saveConfig(new LibGuiConfig());
//
//			JsonObject json;
//			try (InputStream in = Files.newInputStream(file)) {
//				json = jankson.load(in);
//			}
//
//			config =  jankson.fromJson(json, LibGuiConfig.class);
//
//			/*
//			JsonElement jsonElementNew = jankson.toJson(new LibGuiConfig());
//			if(jsonElementNew instanceof JsonObject) {
//				JsonObject jsonNew = (JsonObject) jsonElementNew;
//				if(json.getDelta(jsonNew).size()>= 0) { //TODO: Insert new keys as defaults into `json` IR object instead of writing the config out, so comments are preserved
//					saveConfig(config);
//				}
//			}*/
//		} catch (Exception e) {
//			logger.error("[LibGui] Error loading config: {}", e.getMessage());
//		}
//		return config;
//	}
//
//	public static void saveConfig(LibGuiConfig config) {
//		try {
//			Path file = FabricLoader.getInstance().getConfigDir().resolve("libgui.json5");
//
//			JsonElement json = jankson.toJson(config);
//			String result = json.toJson(true, true);
//			Files.write(file, result.getBytes(StandardCharsets.UTF_8));
//		} catch (Exception e) {
//			logger.error("[LibGui] Error saving config: {}", e.getMessage());
//		}
//	}
}
