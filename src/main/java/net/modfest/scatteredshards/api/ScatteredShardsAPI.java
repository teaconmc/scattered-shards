package net.modfest.scatteredshards.api;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.jetbrains.annotations.ApiStatus;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.impl.ShardCollectionImpl;
import net.modfest.scatteredshards.api.impl.ShardCollectionPersistentState;
import net.modfest.scatteredshards.api.impl.ShardLibraryImpl;
import net.modfest.scatteredshards.api.impl.ShardLibraryPersistentState;
import net.modfest.scatteredshards.networking.S2CUpdateShard;

public class ScatteredShardsAPI {

	public static final String MODIFY_SHARD_PERMISSION = ScatteredShards.permission("modify_shard");

	private static ShardLibraryPersistentState libraryPersistentState;
	private static ShardCollectionPersistentState collectionPersistentState;
	private static final ShardLibrary serverShardLibrary = new ShardLibraryImpl();
	private static Map<UUID, ShardCollection> serverCollections = new HashMap<>();
	public static ShardLibrary clientShardLibrary = null;
	public static ShardCollection clientShardCollection = null;
	public static ShardProgress clientShardProgress = null;
	private static Thread serverThread = null;
	private static Thread clientThread = null;
	public static ShardProgress serverShardProgress = null;


	public static ShardLibrary getServerLibrary() {
		if (serverThread != null && !Thread.currentThread().equals(serverThread)) {
			throw new IllegalStateException("getServerLibrary called from thread '" + Thread.currentThread().getName() + "'. This method can only be accessed from the server thread.");
		}

		return serverShardLibrary;
	}

	@Environment(EnvType.CLIENT)
	public static ShardLibrary getClientLibrary() {
		if (clientThread != null && !Thread.currentThread().equals(clientThread)) {
			throw new IllegalStateException("getClientLibrary called from thread '" + Thread.currentThread().getName() + "'. This method can only be accessed from the client thread.");
		}

		return clientShardLibrary;
	}

	public static ShardCollection getServerCollection(UUID uuid) {
		var collection = serverCollections.get(uuid);
		if (collection == null) {
			collection = new ShardCollectionImpl();
			serverCollections.put(uuid, collection);
			if (collectionPersistentState != null) collectionPersistentState.markDirty();
		}
		return collection;
	}

	public static void calculateShardProgress() {
		var shardCountMap = new HashMap<Identifier, Integer>();
		var totalCount = serverCollections.size();

		serverCollections.forEach(((uuid, identifiers) -> {
			identifiers.forEach(identifier -> {
				shardCountMap.compute(identifier, (k, count) -> count != null ? 1 + count : 1);
			});
		}));

		serverShardProgress = new ShardProgress(totalCount, shardCountMap);
	}

	public static ShardCollection getServerCollection(PlayerEntity player) {
		return getServerCollection(player.getUuid());
	}

	@ApiStatus.Internal
	public static Map<UUID, ShardCollection> exportServerCollections() {
		return serverCollections;
	}

	@Environment(EnvType.CLIENT)
	public static ShardCollection getClientCollection() {
		if (clientThread != null && !Thread.currentThread().equals(clientThread)) {
			throw new IllegalStateException("getClientCollection called from thread '" + Thread.currentThread().getName() + "'. This method can only be accessed from the client thread.");
		}

		return clientShardCollection;
	}


	public static boolean triggerShardCollection(ServerPlayerEntity player, Identifier shardId) {
		var collection = getServerCollection(player);
		if (collection.add(shardId)) {
			if (player.getServer() != null) collectionPersistentState.markDirty();

			serverShardProgress.update(shardId, 1, serverCollections.size());
			ServerPlayNetworking.send(player, new S2CUpdateShard(shardId, S2CUpdateShard.Mode.COLLECT));
			return true;
		} else {
			return false;
		}
	}

	public static boolean triggerShardUncollection(ServerPlayerEntity player, Identifier shardId) {
		var collection = getServerCollection(player);
		if (collection.remove(shardId)) {
			if (player.getServer() != null) collectionPersistentState.markDirty();


			serverShardProgress.update(shardId, -1, serverCollections.size());
			ServerPlayNetworking.send(player, new S2CUpdateShard(shardId, S2CUpdateShard.Mode.UNCOLLECT));
			return true;
		} else {
			return false;
		}
	}

	/* TODO: This does not get called currently */
	@ApiStatus.Internal
	public static void init() {
		serverThread = Thread.currentThread();
	}

	@ApiStatus.Internal
	@Environment(EnvType.CLIENT)
	public static void initClient() {
		clientThread = Thread.currentThread();
		clientShardLibrary = new ShardLibraryImpl();
		clientShardCollection = new ShardCollectionImpl();
	}

	public static void register(ShardCollectionPersistentState persistentState) {
		ScatteredShardsAPI.collectionPersistentState = persistentState;
	}

	public static void register(ShardLibraryPersistentState persistentState) {
		ScatteredShardsAPI.libraryPersistentState = persistentState;
	}
}
