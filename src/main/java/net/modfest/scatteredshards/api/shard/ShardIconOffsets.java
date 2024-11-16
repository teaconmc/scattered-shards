package net.modfest.scatteredshards.api.shard;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record ShardIconOffsets(Offset normal, Offset mini) {

	public static final Codec<ShardIconOffsets> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Offset.CODEC.fieldOf("normal").forGetter(ShardIconOffsets::normal),
		Offset.CODEC.fieldOf("mini").forGetter(ShardIconOffsets::mini)
	).apply(instance, ShardIconOffsets::new));

	public static final PacketCodec<RegistryByteBuf, ShardIconOffsets> PACKET_CODEC = PacketCodec.tuple(
		Offset.PACKET_CODEC, ShardIconOffsets::normal,
		Offset.PACKET_CODEC, ShardIconOffsets::mini,
		ShardIconOffsets::new
	);

	public record Offset(int up, int left) {

		public static final Codec<Offset> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("top").forGetter(Offset::up),
			Codec.INT.fieldOf("left").forGetter(Offset::left)
		).apply(instance, Offset::new));

		public static final PacketCodec<RegistryByteBuf, Offset> PACKET_CODEC = PacketCodec.tuple(
			PacketCodecs.INTEGER, Offset::up,
			PacketCodecs.INTEGER, Offset::left,
			Offset::new
		);

		public int down() {
			// (32 (card) - 16 (item)) - up
			return 16 - up;
		}

		public int right() {
			// (24 (card) - 16 (item)) - left
			return 8 - left;
		}

		public int miniDown() {
			// (16 (card) - 6 (item)) - up
			return 10 - up;
		}

		public int miniRight() {
			// (12 (card) - 6 (item)) - left
			return 6 - left;
		}
	}

	public ShardIconOffsets(int normalLeft, int normalUp, int miniLeft, int miniUp) {
		this(new Offset(normalUp, normalLeft), new Offset(miniUp, miniLeft));
	}
}
