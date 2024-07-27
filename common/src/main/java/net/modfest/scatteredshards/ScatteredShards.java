package net.modfest.scatteredshards;

import cn.zbx1425.scatteredshards.ServerConfig;
import cn.zbx1425.scatteredshards.data.ServerWorldData;
import cn.zbx1425.scatteredshards.data.sync.RedisSynchronizer;
import com.google.common.base.Suppliers;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.registry.registries.RegistrarManager;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import vendor.cn.zbx1425.scatteredshards.cotton.gui.impl.LibGuiCommon;
import net.modfest.scatteredshards.load.ShardTypeLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.command.ShardCommand;
import net.modfest.scatteredshards.networking.ScatteredShardsNetworking;

import java.io.IOException;
import java.util.function.Supplier;

public class ScatteredShards {

	public static final String ID = "scattered_shards";

	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	public static Identifier id(String path) {
		return Identifier.of(ID, path);
	}
	
	public static String permission(String path) {
		return ID + "." + path;
	}

	public static final Supplier<RegistrarManager> REGISTRIES = Suppliers.memoize(() -> RegistrarManager.get(ID));

	private final LibGuiCommon libGuiCommon = new LibGuiCommon();

	public static ServerConfig SERVER_CONFIG = new ServerConfig();

	public void onInitialize() {
		libGuiCommon.onInitialize();
		//ScatteredShardsAPI.init();
		ShardType.register();
		ShardTypeLoader.register();
		ShardCommand.register();
		ScatteredShardsNetworking.register();
		ScatteredShardsContent.register();

		LifecycleEvent.SERVER_STARTING.register(server -> {
			try {
				SERVER_CONFIG.load(server.getRunDirectory()
						.resolve("config").resolve("scattered_shards.json"));

				ScatteredShardsAPI.DATABASE = new ServerWorldData(server, SERVER_CONFIG.syncRole.value.equalsIgnoreCase("host"));
				if (!SERVER_CONFIG.redisUrl.value.isEmpty()) {
					ScatteredShardsAPI.DATABASE.peerChannel = new RedisSynchronizer(SERVER_CONFIG.redisUrl.value, ScatteredShardsAPI.DATABASE);
				}
				ScatteredShardsAPI.DATABASE.load();
			} catch (IOException e) {
				LOGGER.error("Failed to open data storage", e);
				throw new RuntimeException(e);
			}
		});
		LifecycleEvent.SERVER_STOPPING.register(server -> {
			try {
				ScatteredShardsAPI.DATABASE.peerChannel.close();
			} catch (Exception ex) {
				LOGGER.error("Failed to close database peer channel", ex);
			}
		});
	}
}
