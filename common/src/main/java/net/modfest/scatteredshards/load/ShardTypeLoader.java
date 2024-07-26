package net.modfest.scatteredshards.load;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.resource.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.networking.S2CSyncLibrary;

import java.util.Map;

public class ShardTypeLoader extends JsonDataLoader implements ResourceReloader {

	public static final String TYPE = "shard_type";
	public static final Identifier ID = ScatteredShards.id(TYPE);

	public ShardTypeLoader() {
		super(new Gson(), TYPE);
	}

	@Override
	protected void apply(Map<Identifier, JsonElement> cache, ResourceManager manager, Profiler profiler) {
		var library = ScatteredShardsAPI.getServerLibrary();
		
		library.shardTypes().clear();
		library.shardTypes().put(ShardType.MISSING_ID, ShardType.MISSING);
		
		int successes = 0;
		for (var entry : cache.entrySet()) {
			try {
				JsonObject root = JsonHelper.asObject(entry.getValue(), "root element");
				
				if (root.has("text_color")) {
					library.shardTypes().put(entry.getKey(), ShardType.fromJson(root));
					successes++;
				} else {
					for(var shardEntry : root.entrySet()) {
						JsonObject shardTypeObj = JsonHelper.asObject(shardEntry.getValue(), "shard-type object");
						library.shardTypes().put(Identifier.of(shardEntry.getKey()), ShardType.fromJson(shardTypeObj));
						successes++;
					}
				}
			} catch (Exception ex) {
                ScatteredShards.LOGGER.error("Failed to load shard type '{}':", entry.getKey(), ex);
			}
		}
        ScatteredShards.LOGGER.info("Loaded {} shard type{}", successes, successes == 1 ? "" : "s");
	}

	public static final Event<EndDataPackReload> END_DATA_PACK_RELOAD = EventFactory.createLoop(EndDataPackReload.class);

	@FunctionalInterface
	public interface EndDataPackReload {
		/**
		 * Called after data packs on a Minecraft server have been reloaded.
		 *
		 * <p>If the reload was not successful, the old data packs will be kept.
		 *
		 * @param server the server
		 * @param resourceManager the resource manager
		 * @param success if the reload was successful
		 */
		void endDataPackReload(MinecraftServer server, LifecycledResourceManager resourceManager, boolean success);
	}

	public static void register() {
		ReloadListenerRegistry.register(ResourceType.SERVER_DATA, new ShardTypeLoader());

		END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
			if (server != null) {
				var syncLibrary = new S2CSyncLibrary(ScatteredShardsAPI.getServerLibrary());
				for (var player : server.getPlayerManager().getPlayerList()) {
					NetworkManager.sendToPlayer(player, syncLibrary);
				}
			}
		});
	}
}
