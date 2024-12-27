package net.modfest.scatteredshards.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.client.ScatteredShardsClient;

/**
 * Reports success or failure to a client in response to a request to modify a shard.
 */
public record S2CModifyShardResult(Identifier shardId, boolean success) implements CustomPayload {
	public static final Id<S2CModifyShardResult> PACKET_ID = new Id<>(ScatteredShards.id("modify_shard_result"));
	public static final PacketCodec<RegistryByteBuf, S2CModifyShardResult> PACKET_CODEC = PacketCodec.tuple(Identifier.PACKET_CODEC, S2CModifyShardResult::shardId, PacketCodecs.BOOL, S2CModifyShardResult::success, S2CModifyShardResult::new);

	@Environment(EnvType.CLIENT)
	public static void receive(S2CModifyShardResult payload, NetworkManager.PacketContext context) {
		MinecraftClient.getInstance().execute(() -> {
			ScatteredShardsClient.triggerShardModificationToast(payload.shardId(), payload.success());
			MinecraftClient.getInstance().setScreen(null);
		});
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
