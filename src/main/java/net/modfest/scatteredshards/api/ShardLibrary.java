package net.modfest.scatteredshards.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.modfest.scatteredshards.api.impl.ShardLibraryImpl;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;

/**
 * Represents a shard library, the set of all shards which exist in a particular context.
 */
public interface ShardLibrary {
	
	public MiniRegistry<Shard> shards();
	public MiniRegistry<ShardType> shardTypes();
	public SetMultimap<Identifier, Identifier> shardSets();
	
	/**
	 * Removes all Shards, ShardTypes, and ShardSets in this Library.
	 */
	public void clearAll();
	
	public Stream<Shard> resolveShardSet(Identifier id);

	// this is just the worst
	public static final PacketCodec<RegistryByteBuf, ShardLibrary> PACKET_CODEC = PacketCodec.tuple(
			MiniRegistry.createPacketCodec(Shard.CODEC), ShardLibrary::shards,
			MiniRegistry.createPacketCodec(ShardType.CODEC), ShardLibrary::shardTypes,
			PacketCodecs.map(HashMap::new, Identifier.PACKET_CODEC, PacketCodecs.collection(ArrayList::new, Identifier.PACKET_CODEC)).xmap(
					map -> {
						SetMultimap<Identifier, Identifier> multimap = MultimapBuilder.hashKeys().hashSetValues(3).build();
						for (var entry : map.entrySet()) {
							multimap.putAll(entry.getKey(), entry.getValue());
						}
						return multimap;
					},
					multimap -> {
						HashMap<Identifier, ArrayList<Identifier>> map = new HashMap<>();
						for (var entry : multimap.asMap().entrySet()) {
							map.put(entry.getKey(), new ArrayList<>(entry.getValue()));
						}
						return map;
					}
			).cast(),
			ShardLibrary::shardSets,
			ShardLibraryImpl::new
	);
}
