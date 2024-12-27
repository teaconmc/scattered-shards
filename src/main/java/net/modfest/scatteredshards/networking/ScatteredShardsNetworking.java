package net.modfest.scatteredshards.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.impl.ShardLibraryPersistentState;

public class ScatteredShardsNetworking {

	@Environment(EnvType.CLIENT)
	public static void registerClient() {
		ClientPlayNetworking.registerGlobalReceiver(S2CSyncShard.PACKET_ID, S2CSyncShard::receive);
		ClientPlayNetworking.registerGlobalReceiver(S2CSyncLibrary.PACKET_ID, S2CSyncLibrary::receive);
		ClientPlayNetworking.registerGlobalReceiver(S2CSyncCollection.PACKET_ID, S2CSyncCollection::receive);
		ClientPlayNetworking.registerGlobalReceiver(S2CSyncGlobalCollection.PACKET_ID, S2CSyncGlobalCollection::receive);
		ClientPlayNetworking.registerGlobalReceiver(S2CModifyShardResult.PACKET_ID, S2CModifyShardResult::receive);
		ClientPlayNetworking.registerGlobalReceiver(S2CUpdateShard.PACKET_ID, S2CUpdateShard::receive);
	}

	public static void register() {
		PayloadTypeRegistry.playS2C().register(S2CSyncShard.PACKET_ID, S2CSyncShard.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(S2CSyncLibrary.PACKET_ID, S2CSyncLibrary.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(S2CSyncCollection.PACKET_ID, S2CSyncCollection.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(S2CSyncGlobalCollection.PACKET_ID, S2CSyncGlobalCollection.PACKET_CODEC);
		PayloadTypeRegistry.playC2S().register(C2SModifyShard.PACKET_ID, C2SModifyShard.PACKET_CODEC);
		PayloadTypeRegistry.playC2S().register(C2SRequestGlobalCollection.PACKET_ID, C2SRequestGlobalCollection.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(S2CModifyShardResult.PACKET_ID, S2CModifyShardResult.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(S2CUpdateShard.PACKET_ID, S2CUpdateShard.PACKET_CODEC);

		ServerPlayNetworking.registerGlobalReceiver(C2SModifyShard.PACKET_ID, C2SModifyShard::receive);
		ServerPlayNetworking.registerGlobalReceiver(C2SRequestGlobalCollection.PACKET_ID, C2SRequestGlobalCollection::receive);
	}

	// Registration moved to ScatteredShardsNeoForge
	public static void onPlayerJoinServer(MinecraftServer server, ServerPlayerEntity player) {
		ShardLibraryPersistentState.get(server); // Trigger the PersistentState load if it hasn't yet
		ServerPlayNetworking.send(player, new S2CSyncLibrary(ScatteredShardsAPI.getServerLibrary()));
		ServerPlayNetworking.send(player, new S2CSyncCollection(ScatteredShardsAPI.getServerCollection(player)));
		ScatteredShardsAPI.calculateShardProgress();
		ServerPlayNetworking.send(player, new S2CSyncGlobalCollection(ScatteredShardsAPI.getServerGlobalCollection()));
		ScatteredShards.LOGGER.info("S2C sync packets sent to {}", player.getPlayerListName().getString());
	}
}
