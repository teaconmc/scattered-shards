package net.modfest.scatteredshards.api;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.impl.ShardCollectionImpl;

public interface ShardCollection extends Iterable<Identifier> {
	public boolean contains(Identifier identifier);
	
	/**
	 * Adds the identified Shard to this Collection.
	 * @param identifier The Id of the Shard to add. No check is made to see whether the Shard exists.
	 * @return true if the shard was added; false if that ShardId was already in the library or could not be added.
	 */
	public boolean add(Identifier shardId);
	
	/**
	 * Removes the specified Shard from this Collection.
	 * @param shardId the Id of the Shard to remove.
	 */
	public boolean remove(Identifier shardId);
	public int size();
	public void clear();
	
	public Set<Identifier> toImmutableSet();

	void addAll(Collection<Identifier> shardIds);

	public static final PacketCodec<RegistryByteBuf, ShardCollection> PACKET_CODEC = PacketCodecs
			.collection(HashSet::new, Identifier.PACKET_CODEC)
			.xmap(set -> (ShardCollection) new ShardCollectionImpl(set), collection -> new HashSet<>(collection.toImmutableSet()))
			.cast();
}
