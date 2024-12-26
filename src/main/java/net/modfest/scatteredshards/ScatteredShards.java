package net.modfest.scatteredshards;

import cn.zbx1425.scatteredshards.RegistriesWrapper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.command.ShardCommand;
import net.modfest.scatteredshards.load.ShardTypeLoader;
import net.modfest.scatteredshards.networking.ScatteredShardsNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScatteredShards {

	public static final String ID = "scattered_shards";

	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	public static final ScatteredShardsConfig CONFIG = ScatteredShardsConfig.CONFIG;

	public static Identifier id(String path) {
		return Identifier.of(ID, path);
	}

	public static String permission(String path) {
		return ID + "." + path;
	}

	public void onInitialize(RegistriesWrapper registries) {
		ShardType.register(registries);
		ShardTypeLoader.register();
		ShardCommand.register();
		ScatteredShardsNetworking.register();
		ScatteredShardsContent.register(registries);
		ServerLifecycleEvents.SERVER_STOPPED.register(ScatteredShardsAPI::serverStopped);
	}
}
