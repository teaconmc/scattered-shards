package cn.zbx1425.scatteredshards.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.ShardCollection;
import net.modfest.scatteredshards.api.impl.ShardCollectionImpl;

import java.util.*;

public class PlayerShardCollections {

    private final Map<UUID, ShardCollection> serverCollections = new HashMap<>();

    public void loadBearer(UUID bearer, String data, boolean fromFile) {
        synchronized (this) {
            serverCollections.put(bearer, deserialize(data));
        }
    }

    public ImmutableSet<Identifier> getCollection(UUID bearer) {
        synchronized (this) {
            if (!serverCollections.containsKey(bearer)) return ImmutableSet.of();
            return ImmutableSet.copyOf(serverCollections.get(bearer));
        }
    }

    public ImmutableMap<UUID, ShardCollection> getCollections() {
        synchronized (this) {
            return ImmutableMap.copyOf(serverCollections);
        }
    }

    public void insert(UUID bearer, ShardCollection newEntry) {
        synchronized (this) {
            serverCollections.put(bearer, newEntry);
        }
    }

    public ShardCollection update(UUID bearer, ShardCollection newEntry) {
        synchronized (this) {
            serverCollections.put(bearer, newEntry);
            return newEntry;
        }
    }

    public static String serialize(ShardCollection entry) {
        StringBuilder sb = new StringBuilder();
        entry.forEach(shard -> sb.append(shard.toString()).append('\n'));
        return sb.toString();
    }

    public static ShardCollection deserialize(String data) {
        ShardCollection entry = new ShardCollectionImpl();
        String[] lines = data.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            entry.add(Identifier.of(line.trim()));
        }
        return entry;
    }

    public void clear() {
        synchronized (this) {
            serverCollections.clear();
        }
    }
}