package net.modfest.scatteredshards.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardProgress;

public record S2CSyncProgress(ShardProgress progress) implements CustomPayload {
	public static final Id<S2CSyncProgress> PACKET_ID = new Id<>(ScatteredShards.id("sync_progress"));
	public static final PacketCodec<RegistryByteBuf, S2CSyncProgress> PACKET_CODEC = ShardProgress.PACKET_CODEC.xmap(S2CSyncProgress::new, S2CSyncProgress::progress).cast();

	@Environment(EnvType.CLIENT)
	public static void receive(S2CSyncProgress payload, ClientPlayNetworking.Context context) {
		ScatteredShards.LOGGER.info("Syncing ShardProgress...");

		context.client().execute(() -> {
			ScatteredShardsAPI.clientShardProgress = payload.progress(); // bad, TODO: fix

			ScatteredShards.LOGGER.info("Sync complete.");
		});
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
