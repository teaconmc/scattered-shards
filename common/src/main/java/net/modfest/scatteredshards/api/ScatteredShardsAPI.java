package net.modfest.scatteredshards.api;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import cn.zbx1425.scatteredshards.data.ServerWorldData;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.architectury.networking.NetworkManager;
import org.jetbrains.annotations.ApiStatus;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.impl.ShardCollectionImpl;
import net.modfest.scatteredshards.api.impl.ShardLibraryImpl;
import net.modfest.scatteredshards.api.impl.ShardLibraryPersistentState;
import net.modfest.scatteredshards.networking.S2CUpdateShard;

public class ScatteredShardsAPI {
	
	public static final String MODIFY_SHARD_PERMISSION = ScatteredShards.permission("modify_shard");
	
	private static ShardLibraryPersistentState libraryPersistentState;
	private static final ShardLibrary serverShardLibrary = new ShardLibraryImpl();
	public static ShardLibrary clientShardLibrary = null;
	public static ShardCollection clientShardCollection = null;
	private static Thread serverThread = null;

	public static ServerWorldData DATABASE;
	
	public static ShardLibrary getServerLibrary() {
		if (serverThread != null && !Thread.currentThread().equals(serverThread)) {
			throw new IllegalStateException("getServerLibrary called from thread '"+Thread.currentThread().getName()+"'. This method can only be accessed from the server thread.");
		}
		
		return serverShardLibrary;
	}
	
	@Environment(EnvType.CLIENT)
	public static ShardLibrary getClientLibrary() {
		if (!RenderSystem.isOnRenderThread()) {
			throw new IllegalStateException("getClientLibrary called from thread '"+Thread.currentThread().getName()+"'. This method can only be accessed from the client thread.");
		}
		
		return clientShardLibrary;
	}

	@ApiStatus.Internal
	public static Map<UUID, ShardCollection> exportServerCollections() {
		throw new UnsupportedOperationException("Not implemented");
	}
	
	@Environment(EnvType.CLIENT)
	public static ShardCollection getClientCollection() {
		if (!RenderSystem.isOnRenderThread()) {
			throw new IllegalStateException("getClientCollection called from thread '"+Thread.currentThread().getName()+"'. This method can only be accessed from the client thread.");
		}
		
		return clientShardCollection;
	}

	public static ImmutableSet<Identifier> getServerCollection(PlayerEntity player) {
		return DATABASE.comments.getCollection(player.getUuid());
	}
	
	public static boolean triggerShardCollection(ServerPlayerEntity player, Identifier shardId) {
		var collection = getServerCollection(player);
		if (!collection.contains(shardId)) {
			ShardCollection newEntry = new ShardCollectionImpl(collection);
			newEntry.add(shardId);
            try {
                DATABASE.insert(player.getUuid(), newEntry, false);
				NetworkManager.sendToPlayer(player, new S2CUpdateShard(shardId, S2CUpdateShard.Mode.COLLECT));
				return true;
            } catch (IOException ex) {
				ScatteredShards.LOGGER.error("Failed to save player shard collection", ex);
				return false;
            }
		} else {
			return false;
		}
	}
	
	@ApiStatus.Internal
	public static void init() {
		serverThread = Thread.currentThread();
	}
	
	@ApiStatus.Internal
	@Environment(EnvType.CLIENT)
	public static void initClient() {
		clientShardLibrary = new ShardLibraryImpl();
		clientShardCollection = new ShardCollectionImpl();
	}

//	public static void register(ShardCollectionPersistentState persistentState) {
//		ScatteredShardsAPI.collectionPersistentState = persistentState;
//	}
	
	public static void register(ShardLibraryPersistentState persistentState) {
		ScatteredShardsAPI.libraryPersistentState = persistentState;
	}
}
