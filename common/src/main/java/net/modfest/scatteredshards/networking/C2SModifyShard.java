package net.modfest.scatteredshards.networking;

import dev.architectury.networking.NetworkManager;
import vendor.cn.zbx1425.scatteredshards.permissions.v0.Permissions;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.impl.ShardLibraryPersistentState;
import net.modfest.scatteredshards.api.shard.Shard;

/**
 * Requests that a shard be created or modified. Requires permissions!
 */
public record C2SModifyShard(Identifier shardId, Shard shard) implements CustomPayload {
	public static final Id<C2SModifyShard> PACKET_ID = new Id<>(ScatteredShards.id("modify_shard"));
	public static final PacketCodec<RegistryByteBuf, C2SModifyShard> PACKET_CODEC = PacketCodec.tuple(Identifier.PACKET_CODEC, C2SModifyShard::shardId, Shard.PACKET_CODEC, C2SModifyShard::shard, C2SModifyShard::new);
	
	public static void receive(C2SModifyShard payload, NetworkManager.PacketContext context) {
		MinecraftServer server = context.getPlayer().getServer();
		server.execute(() -> {
			boolean success = server.isSingleplayer() || Permissions.check(context.getPlayer(), ScatteredShardsAPI.MODIFY_SHARD_PERMISSION, 1);
			
			//Let the sender know of success or failure before a shard update comes through
			NetworkManager.sendToPlayer((ServerPlayerEntity)context.getPlayer(), new S2CModifyShardResult(payload.shardId(), success));
			
			if (!success) {
				return;
			}

			//Update our serverside library
			ScatteredShardsAPI.getServerLibrary().shards().put(payload.shardId(), payload.shard());
			ScatteredShardsAPI.getServerLibrary().shardSets().put(payload.shard().sourceId(), payload.shardId());

			//Make sure the NBT gets written on next world-save
			ShardLibraryPersistentState.get(server).markDirty();

			//Update everyone's client libraries with the new shard
			var syncShard = new S2CSyncShard(payload.shardId(), payload.shard());
			for(ServerPlayerEntity otherPlayer : server.getPlayerManager().getPlayerList()) {
				NetworkManager.sendToPlayer(otherPlayer, syncShard);
			}
		});
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}