package cn.zbx1425.scatteredshards.sync;

import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.ShardCollection;

import java.util.Map;
import java.util.UUID;

public interface Synchronizer extends AutoCloseable {

	void writeAllToShare(Map<UUID, ShardCollection> collections);

	void readAllFromShare(Map<UUID, ShardCollection> collections);

	void notifyCollectionChange(UUID bearer, ShardCollection newEntry);

	void notifyCollect(UUID bearer, Identifier shardId);

	void notifyUncollect(UUID bearer, Identifier shardId);

	class NoopSynchronizer implements Synchronizer {
		@Override
		public void writeAllToShare(Map<UUID, ShardCollection> collections) {
		}

		@Override
		public void readAllFromShare(Map<UUID, ShardCollection> collections) {
		}

		@Override
		public void notifyCollectionChange(UUID bearer, ShardCollection newEntry) {
		}

		@Override
		public void notifyCollect(UUID bearer, Identifier shardId) {
		}

		@Override
		public void notifyUncollect(UUID bearer, Identifier shardId) {
		}

		@Override
		public void close() {
		}
	}

	Synchronizer NOOP = new NoopSynchronizer();
}
