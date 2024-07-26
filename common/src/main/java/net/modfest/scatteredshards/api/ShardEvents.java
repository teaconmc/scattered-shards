package net.modfest.scatteredshards.api;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.shard.Shard;

public class ShardEvents {

	public static final Event<Collect> COLLECT = EventFactory.createLoop(Collect.class);

	@FunctionalInterface
	public static interface Collect {
		public void handle(ServerPlayerEntity player, Identifier shardId, Shard shard);
	}

}
