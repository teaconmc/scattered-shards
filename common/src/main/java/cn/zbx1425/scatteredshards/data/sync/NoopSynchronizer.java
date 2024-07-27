package cn.zbx1425.scatteredshards.data.sync;

import cn.zbx1425.scatteredshards.data.PlayerShardCollections;
import net.modfest.scatteredshards.api.ShardCollection;
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMap;

import java.io.IOException;
import java.util.UUID;

public class NoopSynchronizer implements Synchronizer {

    public NoopSynchronizer() {

    }

    @Override
    public void close() {

    }

    @Override
    public void kvWriteEntry(UUID bearer, ShardCollection trustedEntry) {

    }

    @Override
    public void notifyUpdate(UUID bearer, ShardCollection trustedEntry) {

    }

    @Override
    public void notifyInsert(UUID bearer, ShardCollection newEntry) {

    }

    @Override
    public void kvReadAllInto(PlayerShardCollections comments) throws IOException {

    }

    @Override
    public void kvWriteAll(PlayerShardCollections comments) {

    }
}
