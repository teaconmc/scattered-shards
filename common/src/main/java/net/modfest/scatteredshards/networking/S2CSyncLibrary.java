package net.modfest.scatteredshards.networking;

import dev.architectury.networking.NetworkManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.ShardType;

/**
 * Wipes out the client's recorded shards and shardSets for this session, and replaces them with the supplied information.
 */
public record S2CSyncLibrary(ShardLibrary library) implements CustomPayload {
	public static final Id<S2CSyncLibrary> PACKET_ID = new Id<>(ScatteredShards.id("sync_library"));
	public static final PacketCodec<RegistryByteBuf, S2CSyncLibrary> PACKET_CODEC = ShardLibrary.PACKET_CODEC.xmap(S2CSyncLibrary::new, S2CSyncLibrary::library).cast();
	
	@Environment(EnvType.CLIENT)
	public static void receive(S2CSyncLibrary payload, NetworkManager.PacketContext context) {
		ScatteredShards.LOGGER.info("Syncing ShardLibrary...");
		
		MinecraftClient.getInstance().execute(() -> {
			ScatteredShardsAPI.clientShardLibrary = payload.library(); // bad, TODO: fix
			ShardLibrary library = ScatteredShardsAPI.getClientLibrary();
			
			//Tidy up in case MISSING got dropped
			if (library.shardTypes().get(ShardType.MISSING_ID).isEmpty()) {
				library.shardTypes().put(ShardType.MISSING_ID, ShardType.MISSING);
			}

            ScatteredShards.LOGGER.info("Sync complete. ShardLibrary has {} shard types, {} shards, and {} shardSets.", library.shardTypes().size(), library.shards().size(), library.shardSets().size());
		});
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}