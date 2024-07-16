package net.modfest.scatteredshards.api;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

import java.util.HashMap;

public class ShardProgress {
    int totalPlayers;
    HashMap<Identifier, Integer> progressTracker;

    public ShardProgress(int totalPlayers, HashMap<Identifier, Integer> progressTracker) {
        this.totalPlayers = totalPlayers;
        this.progressTracker = progressTracker;
    }

    public int totalPlayers() {
        return totalPlayers;
    }

    public HashMap<Identifier, Integer> progressTracker() {
        return progressTracker;
    }

    public static final PacketCodec<RegistryByteBuf, ShardProgress> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER, ShardProgress::totalPlayers,
        PacketCodecs.map(HashMap::new, Identifier.PACKET_CODEC, PacketCodecs.VAR_INT), ShardProgress::progressTracker,
        ShardProgress::new
    );

    public int getCount(Identifier shard) {
        var count = progressTracker.get(shard);
        return count != null ? count : 0;
    }

    public void update(Identifier shard, int change, int playerCount) {
        progressTracker.compute(shard, (k, count) -> count != null ? count + change : 1);
        totalPlayers = playerCount;
    }
}
