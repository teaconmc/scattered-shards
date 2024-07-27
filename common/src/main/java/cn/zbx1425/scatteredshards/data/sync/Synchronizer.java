package cn.zbx1425.scatteredshards.data.sync;

import cn.zbx1425.scatteredshards.data.PlayerShardCollections;
import net.modfest.scatteredshards.api.ShardCollection;
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMap;

import java.io.IOException;
import java.util.UUID;

public interface Synchronizer extends AutoCloseable {

    Synchronizer NOOP = new NoopSynchronizer();

    void kvWriteEntry(UUID bearer, ShardCollection trustedEntry);

    void notifyUpdate(UUID bearer, ShardCollection trustedEntry);

    void notifyInsert(UUID bearer, ShardCollection newEntry);

    void kvReadAllInto(PlayerShardCollections comments) throws IOException;

    void kvWriteAll(PlayerShardCollections comments);
}
