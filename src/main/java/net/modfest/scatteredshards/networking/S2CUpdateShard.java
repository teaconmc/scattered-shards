package net.modfest.scatteredshards.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.client.ScatteredShardsClient;

import java.util.Optional;

/**
 * Collects/uncollects/deletes a shard.
 */
public record S2CUpdateShard(Identifier shardId, Mode mode) implements CustomPayload {
	public static final Id<S2CUpdateShard> PACKET_ID = new Id<>(ScatteredShards.id("update_shard"));
	public static final PacketCodec<RegistryByteBuf, S2CUpdateShard> PACKET_CODEC = PacketCodec.tuple(Identifier.PACKET_CODEC, S2CUpdateShard::shardId, Mode.PACKET_CODEC, S2CUpdateShard::mode, S2CUpdateShard::new);

	@Environment(EnvType.CLIENT)
	public static void receive(S2CUpdateShard payload, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			switch (payload.mode()) {
				case COLLECT -> {
					ScatteredShardsClient.triggerShardCollectAnimation(payload.shardId());
					ScatteredShardsAPI.getClientCollection().add(payload.shardId());
				}
				case UNCOLLECT -> ScatteredShardsAPI.getClientCollection().remove(payload.shardId());
				case DELETE -> {
					ShardLibrary library = ScatteredShardsAPI.getClientLibrary();
					Optional<Shard> shard = library.shards().get(payload.shardId());
					library.shards().remove(payload.shardId());
					shard.ifPresent(it -> library.shardSets().remove(it.sourceId(), payload.shardId()));
				}
			}
		});
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}

	public enum Mode {
		COLLECT,
		UNCOLLECT,
		DELETE;

		public static final PacketCodec<RegistryByteBuf, Mode> PACKET_CODEC = PacketCodecs.INTEGER.xmap(val -> Mode.values()[val], Mode::ordinal).cast();
	}
}