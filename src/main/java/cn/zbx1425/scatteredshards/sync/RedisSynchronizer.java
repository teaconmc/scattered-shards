package cn.zbx1425.scatteredshards.sync;

import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardCollection;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import net.modfest.scatteredshards.api.impl.ShardCollectionImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RedisSynchronizer implements Synchronizer {

    private final StatefulRedisPubSubConnection<String, String> redisSub;
    private final StatefulRedisConnection<String, String> redisConn;

    public static final String HMAP_ALL_KEY = "SSHARD_DATA_ALL";

    public RedisSynchronizer(String URI) {
        redisConn = RedisClient.create(URI).connect();
        redisSub = RedisClient.create(URI).connectPubSub();
        redisSub.addListener(new Listener());
        redisSub.sync().subscribe(RedisMessage.COMMAND_CHANNEL);
    }

	@Override
    public void writeAllToShare(Map<UUID, ShardCollection> collections) {
        RedisAsyncCommands<String, String> commands = redisConn.async();
        commands.multi();
        commands.del(HMAP_ALL_KEY);
        HashMap<String, String> data = new HashMap<>();
        for (Map.Entry<UUID, ShardCollection> entry : collections.entrySet()) {
			StringBuilder value = new StringBuilder();
			for (Identifier shard : entry.getValue()) value.append(shard.toString()).append('\n');
			data.put(entry.getKey().toString(), value.toString());
        }
        if (!data.isEmpty()) {
            commands.hset(HMAP_ALL_KEY, data);
        }
        commands.exec();
    }

	@Override
	public void readAllFromShareInto(Map<UUID, ShardCollection> collections) {
		Map<String, String> data = redisConn.sync().hgetall(HMAP_ALL_KEY);
		for (Map.Entry<String, String> entry : data.entrySet()) {
			ShardCollection collection = new ShardCollectionImpl();
			for (String shard : entry.getValue().split("\n")) {
				if (!shard.isEmpty()) collection.add(Identifier.tryParse(shard));
			}
			collections.put(UUID.fromString(entry.getKey()), collection);
		}
	}

	@Override
    public void notifyCollectionChange(UUID bearer, ShardCollection newEntry) {
		StringBuilder data = new StringBuilder();
		for (Identifier shard : newEntry) data.append(shard.toString()).append('\n');
        redisConn.async().hset(HMAP_ALL_KEY, bearer.toString(), data.toString());
    }

	@Override
    public void notifyCollect(UUID bearer, Identifier shardId) {
        RedisMessage.collect(bearer, shardId).publishAsync(redisConn);
    }

    protected void handleCollect(UUID bearer, Identifier shardId) throws IOException {
		ScatteredShardsAPI.triggerShardCollection(bearer, shardId);
    }

	@Override
    public void notifyUncollect(UUID bearer, Identifier shardId) {
        RedisMessage.uncollect(bearer, shardId).publishAsync(redisConn);
    }

    protected void handleUncollect(UUID bearer, Identifier shardId) throws IOException {
		ScatteredShardsAPI.triggerShardUncollection(bearer, shardId);
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
			message.handle(RedisSynchronizer.this);
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
