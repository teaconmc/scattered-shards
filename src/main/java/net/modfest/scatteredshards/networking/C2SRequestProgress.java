package net.modfest.scatteredshards.networking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;

public class C2SRequestProgress implements CustomPayload {
	public static final CustomPayload.Id<C2SRequestProgress> PACKET_ID = new CustomPayload.Id<>(ScatteredShards.id("request_progress"));
	public static final C2SRequestProgress INSTANCE = new C2SRequestProgress();
	public static final PacketCodec<RegistryByteBuf, C2SRequestProgress> PACKET_CODEC = PacketCodec.unit(INSTANCE);

	public static void receive(C2SRequestProgress payload, ServerPlayNetworking.Context context) {
		System.out.println(ScatteredShardsAPI.exportServerCollections().size());
		ServerPlayNetworking.send(context.player(), new S2CSyncProgress(ScatteredShardsAPI.serverShardProgress));
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
