package net.modfest.scatteredshards.networking;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.impl.ShardLibraryPersistentState;

public class ScatteredShardsNetworking {

	@Environment(EnvType.CLIENT)
	public static void registerClient() {
		NetworkManager.registerReceiver(NetworkManager.Side.S2C, S2CSyncShard.PACKET_ID, S2CSyncShard.PACKET_CODEC, S2CSyncShard::receive);
		NetworkManager.registerReceiver(NetworkManager.Side.S2C, S2CSyncLibrary.PACKET_ID, S2CSyncLibrary.PACKET_CODEC, S2CSyncLibrary::receive);
		NetworkManager.registerReceiver(NetworkManager.Side.S2C, S2CSyncCollection.PACKET_ID, S2CSyncCollection.PACKET_CODEC, S2CSyncCollection::receive);
		NetworkManager.registerReceiver(NetworkManager.Side.S2C, S2CSyncGlobalCollection.PACKET_ID, S2CSyncGlobalCollection.PACKET_CODEC, S2CSyncGlobalCollection::receive);
		NetworkManager.registerReceiver(NetworkManager.Side.S2C, S2CModifyShardResult.PACKET_ID, S2CModifyShardResult.PACKET_CODEC, S2CModifyShardResult::receive);
		NetworkManager.registerReceiver(NetworkManager.Side.S2C, S2CUpdateShard.PACKET_ID, S2CUpdateShard.PACKET_CODEC, S2CUpdateShard::receive);
	}

	public static void register() {
		if (Platform.getEnvironment() == Env.SERVER) {
			NetworkManager.registerS2CPayloadType(S2CSyncShard.PACKET_ID, S2CSyncShard.PACKET_CODEC);
			NetworkManager.registerS2CPayloadType(S2CSyncLibrary.PACKET_ID, S2CSyncLibrary.PACKET_CODEC);
			NetworkManager.registerS2CPayloadType(S2CSyncCollection.PACKET_ID, S2CSyncCollection.PACKET_CODEC);
			NetworkManager.registerS2CPayloadType(S2CSyncGlobalCollection.PACKET_ID, S2CSyncGlobalCollection.PACKET_CODEC);
//			PayloadTypeRegistry.playC2S().register(C2SModifyShard.PACKET_ID, C2SModifyShard.PACKET_CODEC);
//			PayloadTypeRegistry.playC2S().register(C2SRequestGlobalCollection.PACKET_ID, C2SRequestGlobalCollection.PACKET_CODEC);
			NetworkManager.registerS2CPayloadType(S2CModifyShardResult.PACKET_ID, S2CModifyShardResult.PACKET_CODEC);
			NetworkManager.registerS2CPayloadType(S2CUpdateShard.PACKET_ID, S2CUpdateShard.PACKET_CODEC);
		}

		NetworkManager.registerReceiver(NetworkManager.Side.C2S, C2SModifyShard.PACKET_ID, C2SModifyShard.PACKET_CODEC, C2SModifyShard::receive);
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, C2SRequestGlobalCollection.PACKET_ID, C2SRequestGlobalCollection.PACKET_CODEC, C2SRequestGlobalCollection::receive);

		PlayerEvent.PLAYER_JOIN.register((player) -> {
			ShardLibraryPersistentState.get(player.server); // Trigger the PersistentState load if it hasn't yet
			NetworkManager.sendToPlayer(player, new S2CSyncLibrary(ScatteredShardsAPI.getServerLibrary()));
			NetworkManager.sendToPlayer(player, new S2CSyncCollection(ScatteredShardsAPI.getServerCollection(player)));
			ScatteredShardsAPI.calculateShardProgress();
			NetworkManager.sendToPlayer(player, new S2CSyncGlobalCollection(ScatteredShardsAPI.getServerGlobalCollection()));
		});
	}
}
