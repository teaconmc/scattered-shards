package net.modfest.scatteredshards.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.GlobalCollection;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;

public record S2CSyncGlobalCollection(GlobalCollection globalCollection) implements CustomPayload {
	public static final Id<S2CSyncGlobalCollection> PACKET_ID = new Id<>(ScatteredShards.id("sync_global_collection"));
	public static final PacketCodec<RegistryByteBuf, S2CSyncGlobalCollection> PACKET_CODEC = GlobalCollection.PACKET_CODEC.xmap(S2CSyncGlobalCollection::new, S2CSyncGlobalCollection::globalCollection).cast();

	@Environment(EnvType.CLIENT)
	public static void receive(S2CSyncGlobalCollection payload, ClientPlayNetworking.Context context) {
		ScatteredShards.LOGGER.info("Syncing GlobalShardCollection...");

		context.client().execute(() -> {
			ScatteredShardsAPI.updateClientGlobalCollection(payload.globalCollection());
			ScatteredShards.LOGGER.info("Sync complete. Received data for {} players.", payload.globalCollection.totalPlayers());
		});
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
