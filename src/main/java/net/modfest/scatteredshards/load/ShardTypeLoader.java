package net.modfest.scatteredshards.load;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.networking.S2CSyncLibrary;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ShardTypeLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {

	public static final String TYPE = "shard_type";
	public static final Identifier ID = ScatteredShards.id(TYPE);

	public ShardTypeLoader() {
		super(new Gson(), TYPE);
	}

	@Override
	public @NotNull Identifier getFabricId() {
		return ID;
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
					for (var shardEntry : root.entrySet()) {
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

	public static void register() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ShardTypeLoader());
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
			if (server != null) {
				var syncLibrary = new S2CSyncLibrary(ScatteredShardsAPI.getServerLibrary());
				for (var player : server.getPlayerManager().getPlayerList()) {
					ServerPlayNetworking.send(player, syncLibrary);
				}
			}
		});
	}
}