package net.modfest.scatteredshards;

import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.load.ShardTypeLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.command.ShardCommand;
import net.modfest.scatteredshards.networking.ScatteredShardsNetworking;

public class ScatteredShards {

	public static final String ID = "scattered_shards";

	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	public static Identifier id(String path) {
		return Identifier.of(ID, path);
	}
	
	public static String permission(String path) {
		return ID + "." + path;
	}

	public void onInitialize() {
		//ScatteredShardsAPI.init();
		ShardType.register();
		ShardTypeLoader.register();
		ShardCommand.register();
		ScatteredShardsNetworking.register();
		ScatteredShardsContent.register();
	}
}
