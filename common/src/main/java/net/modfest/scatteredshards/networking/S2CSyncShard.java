package net.modfest.scatteredshards.networking;

import dev.architectury.networking.NetworkManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.Shard;

/**
 * Syncs or adds one shard to the client, leaving all others untouched
 */
public record S2CSyncShard(Identifier shardId, Shard shard) implements CustomPayload {
	public static final Id<S2CSyncShard> PACKET_ID = new Id<>(ScatteredShards.id("sync_shard"));
	public static final PacketCodec<RegistryByteBuf, S2CSyncShard> PACKET_CODEC = PacketCodec.tuple(Identifier.PACKET_CODEC, S2CSyncShard::shardId, Shard.PACKET_CODEC, S2CSyncShard::shard, S2CSyncShard::new);
	
	@Environment(EnvType.CLIENT)
	public static void receive(S2CSyncShard payload, NetworkManager.PacketContext context) {
		MinecraftClient.getInstance().execute(() -> {
			ShardLibrary library = ScatteredShardsAPI.getClientLibrary();
			library.shards().put(payload.shardId(), payload.shard());
			library.shardSets().put(payload.shard().sourceId(), payload.shardId());
			//ScatteredShards.LOGGER.info("Updated data for shard \"" + shardId + "\"");
		});
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}