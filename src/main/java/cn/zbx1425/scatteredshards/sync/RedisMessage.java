package cn.zbx1425.scatteredshards.sync;

import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import io.lettuce.core.api.StatefulRedisConnection;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

public class RedisMessage {

    public static final String COMMAND_CHANNEL = "SSHARD_COMMAND_CHANNEL";

    private static final String INSTANCE_ID = Long.toHexString(new Random().nextLong());

    public String initiator;
    public String action;
    public UUID bearer;
    public Identifier shard;

    public RedisMessage(String action, UUID bearer, Identifier shard) {
        this.initiator = INSTANCE_ID;
        this.action = action;
        this.bearer = bearer;
        this.shard = shard;
    }

    public RedisMessage(String redisCommand) {
        String[] parts = redisCommand.split("/");
        this.action = parts[0];
        this.initiator = parts[1];
        this.bearer = UUID.fromString(parts[2]);
        this.shard = Identifier.of(parts[3]);
    }

    public static RedisMessage collect(UUID bearer, Identifier shard) {
        return new RedisMessage("COLLECT", bearer, shard);
    }

    public static RedisMessage uncollect(UUID bearer, Identifier shard) {
        return new RedisMessage("UNCOLLECT", bearer, shard);
    }

    public void publishAsync(StatefulRedisConnection<String, String> connection) {
        connection.async().publish(COMMAND_CHANNEL, String.format("%s/%s/%s/%s",
                action, initiator, bearer.toString(), shard.toString()));
    }

	public void handle(RedisSynchronizer synchronizer) {
		if (isFromSelf()) return;
		try {
			switch (action) {
				case "COLLECT" -> synchronizer.handleCollect(bearer, shard);
				case "UNCOLLECT" -> synchronizer.handleUncollect(bearer, shard);
			}
		} catch (IOException ex) {
			ScatteredShards.LOGGER.error("Redis handler", ex);
		}
	}

    public boolean isFromSelf() {
        return initiator.equals(INSTANCE_ID);
    }
}
