package net.modfest.scatteredshards.api.impl;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.Shard;

public class ShardLibraryPersistentState extends PersistentState {
	public static PersistentState.Type<ShardLibraryPersistentState> TYPE = new PersistentState.Type<>(
		ShardLibraryPersistentState::new,
		ShardLibraryPersistentState::createFromNbt,
		null
	);

	public static final String SHARDS_KEY = "Shards";

	public static ShardLibraryPersistentState get(MinecraftServer server) {
		return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, ScatteredShards.ID + "_library");
	}

	public ShardLibraryPersistentState() {
	}

	public static ShardLibraryPersistentState createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
		ScatteredShards.LOGGER.info("Loading shard library...");
		ShardLibraryPersistentState state = new ShardLibraryPersistentState();
		// This is just a placeholder - all the data lives in the serverLibrary below

		ShardLibrary library = ScatteredShardsAPI.getServerLibrary();
		library.shards().clear();
		library.shardSets().clear();

		NbtCompound shards = tag.getCompound(SHARDS_KEY);
		for (String id : shards.getKeys()) {
			try {
				NbtCompound shardNbt = shards.getCompound(id);
				Identifier shardId = Identifier.of(id);
				Shard shard = Shard.fromNbt(shardNbt);

				library.shards().put(shardId, shard);
				library.shardSets().put(shard.sourceId(), shardId);
			} catch (Throwable t) {
				ScatteredShards.LOGGER.error("Could not load shard \"{}\": {}", id, t.getMessage());
			}
		}

		ScatteredShards.LOGGER.info("Loaded {} shards and {} shardSets.", library.shards().size(), library.shardSets().size());

		return state;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		ShardLibrary library = ScatteredShardsAPI.getServerLibrary();
		ScatteredShards.LOGGER.info("Saving the ShardLibrary with {} shards and {} shardSets...", library.shards().size(), library.shardSets().size());

		tag.put(SHARDS_KEY, library.shards().toNbt());

		ScatteredShards.LOGGER.info("ShardLibrary saved.");

		return tag;
	}
}
