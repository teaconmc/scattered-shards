package cn.zbx1425.scatteredshards.data.sync;

import cn.zbx1425.scatteredshards.data.PlayerShardCollections;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ShardCollection;
import cn.zbx1425.scatteredshards.data.ServerWorldData;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RedisSynchronizer implements Synchronizer {

    private final StatefulRedisPubSubConnection<String, String> redisSub;
    private final StatefulRedisConnection<String, String> redisConn;

    public static final String HMAP_ALL_KEY = "SCATSHARD_DATA_ALL";

    private final ServerWorldData serverWorldData;

    public RedisSynchronizer(String URI, ServerWorldData serverWorldData) {
        redisConn = RedisClient.create(URI).connect();
        redisSub = RedisClient.create(URI).connectPubSub();
        redisSub.addListener(new Listener());
        redisSub.sync().subscribe(RedisMessage.COMMAND_CHANNEL);

        this.serverWorldData = serverWorldData;
    }

    @Override
    public void kvWriteAll(PlayerShardCollections comments) {
        RedisAsyncCommands<String, String> commands = redisConn.async();
        commands.multi();
        commands.del(HMAP_ALL_KEY);
        HashMap<String, String> data = new HashMap<>();
        for (Map.Entry<UUID, ShardCollection> entry : comments.getCollections().entrySet()) {
            data.put(entry.getKey().toString(), PlayerShardCollections.serialize(entry.getValue()));
        }
        if (!data.isEmpty()) {
            commands.hset(HMAP_ALL_KEY, data);
        }
        commands.exec();
    }

    @Override
    public void kvWriteEntry(UUID bearer, ShardCollection newEntry) {
        redisConn.async().hset(HMAP_ALL_KEY, bearer.toString(), PlayerShardCollections.serialize(newEntry));
    }

    @Override
    public void notifyInsert(UUID bearer, ShardCollection newEntry) {
        RedisMessage.insert(bearer, newEntry).publishAsync(redisConn);
    }

    private void handleInsert(UUID bearer, ShardCollection peerEntry) throws IOException {
        serverWorldData.insert(bearer, peerEntry, true);
    }

    @Override
    public void notifyUpdate(UUID bearer, ShardCollection newEntry) {
        RedisMessage.update(bearer, newEntry).publishAsync(redisConn);
    }

    private void handleUpdate(UUID bearer, ShardCollection peerEntry) throws IOException {
        serverWorldData.update(bearer, peerEntry, true);
    }

    @Override
    public void kvReadAllInto(PlayerShardCollections comments) throws IOException {
        Map<String, String> data = redisConn.sync().hgetall(HMAP_ALL_KEY);
        for (Map.Entry<String, String> entry : data.entrySet()) {
            comments.insert(UUID.fromString(entry.getKey()), PlayerShardCollections.deserialize(entry.getValue()));
        }
    }

    @Override
    public void close() {
        redisSub.close();
        redisConn.close();
    }

    public class Listener implements RedisPubSubListener<String, String> {
        @Override
        public void message(String channel, String rawMessage) {
            RedisMessage message = new RedisMessage(rawMessage);
            if (message.isFromSelf()) return;
            try {
                switch (message.action) {
                    case "INSERT" -> handleInsert(message.bearer, message.content);
                    case "UPDATE" -> handleUpdate(message.bearer, message.content);
                }
            } catch (IOException ex) {
                ScatteredShards.LOGGER.error("Redis handler", ex);
            }
        }

        @Override
        public void message(String pattern, String channel, String message) { }

        @Override
        public void subscribed(String channel, long count) { }

        @Override
        public void psubscribed(String pattern, long count) { }

        @Override
        public void unsubscribed(String channel, long count) { }

        @Override
        public void punsubscribed(String pattern, long count) { }
    }

}
