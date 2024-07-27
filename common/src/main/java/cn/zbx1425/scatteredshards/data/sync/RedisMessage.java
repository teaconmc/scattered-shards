package cn.zbx1425.scatteredshards.data.sync;

import cn.zbx1425.scatteredshards.data.PlayerShardCollections;
import net.modfest.scatteredshards.api.ShardCollection;
import cn.zbx1425.scatteredshards.data.ServerWorldData;
import io.lettuce.core.api.StatefulRedisConnection;

import java.util.UUID;

public class RedisMessage {

    public static final String COMMAND_CHANNEL = "WORLD_COMMENT_COMMAND_CHANNEL";

    private static final String INSTANCE_ID = Long.toHexString(ServerWorldData.SNOWFLAKE.nextId());

    public String initiator;
    public String action;
    public UUID bearer;
    public ShardCollection content;

    public RedisMessage(String action, UUID bearer, ShardCollection content) {
        this.initiator = INSTANCE_ID;
        this.action = action;
        this.bearer = bearer;
        this.content = content;
    }

    public RedisMessage(String redisCommand) {
        String[] parts = redisCommand.split(":");
        this.action = parts[0];
        this.initiator = parts[1];
        this.bearer = UUID.fromString(parts[2]);
        this.content = PlayerShardCollections.deserialize(parts[3]);
    }

    public static RedisMessage insert(UUID bearer, ShardCollection entry) {
        return new RedisMessage("INSERT", bearer, entry);
    }

    public static RedisMessage update(UUID bearer, ShardCollection entry) {
        return new RedisMessage("UPDATE", bearer, entry);
    }

    public void publishAsync(StatefulRedisConnection<String, String> connection) {
        connection.async().publish(COMMAND_CHANNEL, String.format("%s:%s:%s:%s",
                action, initiator, bearer.toString(), PlayerShardCollections.serialize(content)));
    }

    public boolean isFromSelf() {
        return initiator.equals(INSTANCE_ID);
    }
}
