package net.modfest.scatteredshards.api.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;

import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.MiniRegistry;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;

public class ShardLibraryImpl implements ShardLibrary {
	private final MiniRegistry<Shard> shards;
	private final MiniRegistry<ShardType> shardTypes;
	private final SetMultimap<Identifier, Identifier> shardSets;
	
	@Override
	public void clearAll() {
		shards.clear();
		shardSets.clear();
		shardTypes.clear();
	}

	public ShardLibraryImpl() {
		this(new MiniRegistry<>(Shard.CODEC), new MiniRegistry<>(ShardType.CODEC), MultimapBuilder.hashKeys().hashSetValues(3).build());
	}

	public ShardLibraryImpl(MiniRegistry<Shard> shards, MiniRegistry<ShardType> shardTypes, SetMultimap<Identifier, Identifier> shardSets) {
		this.shards = shards;
		this.shardTypes = shardTypes;
		this.shardSets = shardSets;
	}
	
	@Override
	public MiniRegistry<Shard> shards() {
		return shards;
	}
	
	@Override
	public MiniRegistry<ShardType> shardTypes() {
		return shardTypes;
	}
	
	@Override
	public SetMultimap<Identifier, Identifier> shardSets() {
		return shardSets;
	}
	
	@Override
	public Stream<Shard> resolveShardSet(Identifier id) {
		return shardSets.get(id).stream()
			.map(shards::get)
			.flatMap(Optional::stream);
	}
}
