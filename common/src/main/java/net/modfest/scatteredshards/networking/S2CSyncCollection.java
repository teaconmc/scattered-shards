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
import net.modfest.scatteredshards.api.ShardCollection;

public record S2CSyncCollection(ShardCollection collection) implements CustomPayload {
	public static final Id<S2CSyncCollection> PACKET_ID = new Id<>(ScatteredShards.id("sync_collection"));
	public static final PacketCodec<RegistryByteBuf, S2CSyncCollection> PACKET_CODEC = ShardCollection.PACKET_CODEC.xmap(S2CSyncCollection::new, S2CSyncCollection::collection);
	
	@Environment(EnvType.CLIENT)
	public static void receive(S2CSyncCollection payload, NetworkManager.PacketContext context) {
		MinecraftClient.getInstance().execute(() -> {
            ScatteredShards.LOGGER.info("Syncing ShardCollection with {} shards collected.", payload.collection().size());
			ScatteredShardsAPI.clientShardCollection = payload.collection(); // TODO: bad, fix
		});
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}