package net.modfest.scatteredshards.api;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

import java.util.HashMap;

public class GlobalCollection {
	int totalPlayers;
	HashMap<Identifier, Integer> collectionTracker;

	public GlobalCollection(int totalPlayers, HashMap<Identifier, Integer> collectionTracker) {
		this.totalPlayers = totalPlayers;
		this.collectionTracker = collectionTracker;
	}

	public int totalPlayers() {
		return totalPlayers;
	}

	public HashMap<Identifier, Integer> collectionTracker() {
		return collectionTracker;
	}

	public static final PacketCodec<RegistryByteBuf, GlobalCollection> PACKET_CODEC = PacketCodec.tuple(
		PacketCodecs.INTEGER, GlobalCollection::totalPlayers,
		PacketCodecs.map(HashMap::new, Identifier.PACKET_CODEC, PacketCodecs.VAR_INT), GlobalCollection::collectionTracker,
		GlobalCollection::new
	);

	public int getCount(Identifier shard) {
		var count = collectionTracker.get(shard);
		return count != null ? count : 0;
	}

	public void update(Identifier shard, int change, int playerCount) {
		collectionTracker.compute(shard, (k, count) -> count != null ? count + change : 1);
		totalPlayers = playerCount;
	}
}
