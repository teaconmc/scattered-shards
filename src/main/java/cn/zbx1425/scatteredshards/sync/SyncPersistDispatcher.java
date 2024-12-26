package cn.zbx1425.scatteredshards.sync;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ShardCollection;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

public class SyncPersistDispatcher implements Synchronizer {

    public final MinecraftServer server;
	public final Path basePath;

	public boolean isHost;
    public final Synchronizer peerChannel;
	public final FileSerializer persistAccess;

	public static SyncPersistDispatcher CURRENT;

    public SyncPersistDispatcher(MinecraftServer server, boolean isHost, Synchronizer peerChannel) {
        this.server = server;
		this.basePath = Path.of(server.getSavePath(WorldSavePath.ROOT).toString(), "scattered_shards");
        this.isHost = isHost;
        this.peerChannel = peerChannel;
		this.persistAccess = new FileSerializer(basePath);
	}

	public void loadFromToShareOrDiskAndInto(Map<UUID, ShardCollection> playerShardCollections) {
		if (isHost) {
			try {
				persistAccess.loadInto(playerShardCollections);
			} catch (IOException e) {
				ScatteredShards.LOGGER.error("Failed to load shard collections from disk", e);
			}
			peerChannel.writeAllToShare(playerShardCollections);
		} else {
			peerChannel.readAllFromShareInto(playerShardCollections);
		}
	}

	@Override
	public void notifyCollectionChange(UUID bearer, ShardCollection newEntry) {
		if (!isHost) return;
		peerChannel.notifyCollectionChange(bearer, newEntry);
		try {
			persistAccess.write(bearer, newEntry);
		} catch (IOException e) {
			ScatteredShards.LOGGER.error("Failed to persist shard collection for player " + bearer, e);
		}
	}

	@Override
	public void notifyCollect(UUID bearer, Identifier shardId) {
		peerChannel.notifyCollect(bearer, shardId);
	}

	@Override
	public void notifyUncollect(UUID bearer, Identifier shardId) {
		peerChannel.notifyUncollect(bearer, shardId);
	}

	@Override
	public void writeAllToShare(Map<UUID, ShardCollection> collections) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void readAllFromShareInto(Map<UUID, ShardCollection> collections) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() throws Exception {
		peerChannel.close();
	}
}
