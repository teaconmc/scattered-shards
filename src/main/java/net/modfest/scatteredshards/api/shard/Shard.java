package net.modfest.scatteredshards.api.shard;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

public class Shard {
	public static final Codec<Either<ItemStack, Identifier>> ICON_CODEC = Codec.either(ItemStack.CODEC, Identifier.CODEC);

	public static final Codec<Shard> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Identifier.CODEC.fieldOf("shard_type_id").forGetter(Shard::shardTypeId),
		TextCodecs.CODEC.fieldOf("name").forGetter(Shard::name),
		TextCodecs.CODEC.fieldOf("lore").forGetter(Shard::lore),
		TextCodecs.CODEC.fieldOf("hint").forGetter(Shard::hint),
		Identifier.CODEC.fieldOf("source_id").forGetter(Shard::sourceId),
		ICON_CODEC.fieldOf("icon").forGetter(Shard::icon),
		Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("list_order").forGetter(Shard::listOrder)
	).apply(instance, Shard::new));

	public static final PacketCodec<RegistryByteBuf, Shard> PACKET_CODEC = PacketCodecs.codec(CODEC).cast();

	public static final Identifier MISSING_ICON_ID = ScatteredShards.id("textures/gui/shards/missing_icon.png");
	public static final Either<ItemStack, Identifier> MISSING_ICON = Either.right(MISSING_ICON_ID);
	public static final Identifier MISSING_SHARD_SOURCE = ScatteredShards.id("missing");
	public static final Shard MISSING_SHARD = new Shard(ShardType.MISSING_ID, Text.of("Missing"), Text.of(""), Text.of(""), MISSING_SHARD_SOURCE, MISSING_ICON, Optional.empty());

	protected Identifier shardTypeId;
	protected Text name;
	protected Text lore;
	protected Text hint;
	protected Integer listOrder;
	protected Identifier sourceId;
	protected Either<ItemStack, Identifier> icon;

	public Shard(Identifier shardTypeId, Text name, Text lore, Text hint, Identifier sourceId, Either<ItemStack, Identifier> icon, Optional<Integer> listOrder) {
		Stream.of(name, lore, hint, icon).forEach(Objects::requireNonNull);
		this.shardTypeId = shardTypeId;
		this.name = name;
		this.lore = lore;
		this.hint = hint;
		this.sourceId = sourceId;
		this.icon = icon;
		this.listOrder = listOrder.orElse(null);
	}

	public Identifier shardTypeId() {
		return shardTypeId;
	}

	public Text name() {
		return name;
	}

	public Text lore() {
		return lore;
	}

	public Text hint() {
		return hint;
	}

	public Optional<Integer> listOrder() {
		return listOrder == null ? Optional.empty() : Optional.of(listOrder);
	}

	public Identifier sourceId() {
		return sourceId;
	}

	public Either<ItemStack, Identifier> icon() {
		return icon;
	}

	public Shard setShardType(Identifier shardTypeId) {
		this.shardTypeId = shardTypeId;
		return this;
	}

	public Shard setName(Text value) {
		this.name = value;
		return this;
	}

	public Shard setLore(Text value) {
		this.lore = value;
		return this;
	}

	public Shard setHint(Text value) {
		this.hint = value;
		return this;
	}

	public Shard setListOrder(Integer value) {
		this.listOrder = value;
		return this;
	}

	public Shard setIcon(Either<ItemStack, Identifier> icon) {
		this.icon = icon;
		return this;
	}

	public Shard setIcon(ItemStack itemValue) {
		this.icon = Either.left(itemValue);
		return this;
	}

	public Shard setIcon(Identifier textureValue) {
		this.icon = Either.right(textureValue);
		return this;
	}

	public Shard setSourceId(Identifier id) {
		this.sourceId = id;
		return this;
	}

	public static Shard fromNbt(NbtCompound nbt) {
		return CODEC.parse(NbtOps.INSTANCE, nbt).result().orElseThrow();
	}

	public NbtCompound toNbt() {
		return (NbtCompound) CODEC.encodeStart(NbtOps.INSTANCE, this).result().orElseThrow();
	}

	public JsonObject toJson() {
		return (JsonObject) CODEC.encodeStart(JsonOps.INSTANCE, this).result().orElseThrow();
	}

	public Shard copy() {
		Either<ItemStack, Identifier> icon = icon().mapBoth(stack -> stack, id -> id);
		return new Shard(shardTypeId, name.copy(), lore.copy(), hint.copy(), sourceId, icon, listOrder());
	}

	@Override
	public String toString() {
		return toJson().toString();
	}

	public static Shard emptyOfType(Identifier id) {
		return MISSING_SHARD.copy().setShardType(id).setName(Text.of(""));
	}

	public static Text getSourceForMod(ModContainer mod) {
		return Text.literal(mod.getMetadata().getName());
	}

	public static Optional<Text> getSourceForModId(String modId) {
		return FabricLoader.getInstance().getModContainer(modId).map(Shard::getSourceForMod);
	}

	public static Text getSourceForSourceId(Identifier id) {
		if (!id.getPath().equals("shard_pack")) {
			return Text.translatable("shard_pack." + id.getNamespace() + "." + id.getPath() + ".name");
		}

		return Text.translatableWithFallback("shard_pack." + id.getNamespace() + ".name",
			getSourceForModId(id.getNamespace()).orElse(Text.literal(id.getNamespace())).getLiteralString()
		);
	}
}
