package net.modfest.scatteredshards.networking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;

public class C2SRequestGlobalCollection implements CustomPayload {
	public static final CustomPayload.Id<C2SRequestGlobalCollection> PACKET_ID = new CustomPayload.Id<>(ScatteredShards.id("request_global_collection"));
	public static final C2SRequestGlobalCollection INSTANCE = new C2SRequestGlobalCollection();
	public static final PacketCodec<RegistryByteBuf, C2SRequestGlobalCollection> PACKET_CODEC = PacketCodec.unit(INSTANCE);

	public static void receive(C2SRequestGlobalCollection payload, ServerPlayNetworking.Context context) {
		ServerPlayNetworking.send(context.player(), new S2CSyncGlobalCollection(ScatteredShardsAPI.getServerGlobalCollection()));
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
