package net.modfest.scatteredshards.api;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.impl.ShardCollectionImpl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public interface ShardCollection extends Iterable<Identifier> {
	boolean contains(Identifier identifier);

	/**
	 * Adds the identified Shard to this Collection.
	 *
	 * @param shardId The Id of the Shard to add. No check is made to see whether the Shard exists.
	 * @return true if the shard was added; false if that ShardId was already in the library or could not be added.
	 */
	boolean add(Identifier shardId);

	/**
	 * Removes the specified Shard from this Collection.
	 *
	 * @param shardId the Id of the Shard to remove.
	 */
	boolean remove(Identifier shardId);

	int size();

	void clear();

	Set<Identifier> toImmutableSet();

	void addAll(Collection<Identifier> shardIds);

	PacketCodec<RegistryByteBuf, ShardCollection> PACKET_CODEC = PacketCodecs
		.collection(HashSet::new, Identifier.PACKET_CODEC)
		.xmap(set -> (ShardCollection) new ShardCollectionImpl(set), collection -> new HashSet<>(collection.toImmutableSet()))
		.cast();
}
