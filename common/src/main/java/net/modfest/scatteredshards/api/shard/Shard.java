package net.modfest.scatteredshards.api.shard;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.modfest.scatteredshards.ScatteredShards;

public class Shard {
	public static final Codec<Either<ItemStack, Identifier>> ICON_CODEC = Codec.either(ItemStack.CODEC, Identifier.CODEC);
	
	public static final Codec<Shard> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("shard_type_id").forGetter(Shard::shardTypeId),
			TextCodecs.CODEC.fieldOf("name").forGetter(Shard::name),
			TextCodecs.CODEC.fieldOf("lore").forGetter(Shard::lore),
			TextCodecs.CODEC.fieldOf("hint").forGetter(Shard::hint),
			TextCodecs.CODEC.fieldOf("source").forGetter(Shard::source),
			Identifier.CODEC.fieldOf("source_id").forGetter(Shard::sourceId),
			ICON_CODEC.fieldOf("icon").forGetter(Shard::icon)
		).apply(instance, Shard::new));

	public static final PacketCodec<RegistryByteBuf, Shard> PACKET_CODEC = PacketCodecs.codec(CODEC).cast();

	public static final Identifier MISSING_ICON_ID = ScatteredShards.id("textures/gui/shards/missing_icon.png");
	public static final Either<ItemStack, Identifier> MISSING_ICON = Either.right(MISSING_ICON_ID);
	public static final Identifier MISSING_SHARD_SOURCE = ScatteredShards.id("missing");
	public static final Identifier LOST_AND_FOUND_SHARD_SOURCE = ScatteredShards.id("lost_and_found");
	public static final Shard MISSING_SHARD = new Shard(ShardType.MISSING_ID, Text.of("Missing"), Text.of(""), Text.of(""), Text.of("None"), MISSING_SHARD_SOURCE, MISSING_ICON);

	protected Identifier shardTypeId;
	protected Text name;
	protected Text lore;
	protected Text hint;
	protected Text source;
	protected Identifier sourceId;
	protected Either<ItemStack, Identifier> icon;
	
	public Shard(Identifier shardTypeId, Text name, Text lore, Text hint, Text source, Identifier sourceId, Either<ItemStack, Identifier> icon) {
		Stream.of(name, lore, hint, source, icon).forEach(Objects::requireNonNull);
		this.shardTypeId = shardTypeId;
		this.name = name;
		this.lore = lore;
		this.hint = hint;
		this.source = source;
		this.sourceId = sourceId;
		this.icon = icon;
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

	public Text source() {
		return source;
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

	public Shard setSource(Text source) {
		this.source = source;
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
		return new Shard(shardTypeId, name.copy(), lore.copy(), hint.copy(), source.copy(), sourceId, icon);
	}

	@Override
	public String toString() {
		return toJson().toString();
	}
	
	public static Shard emptyOfType(Identifier id) {
		return MISSING_SHARD.copy().setShardType(id);
	}

	public static Text getSourceForNamespace(String namespace) {
		return Text.translatable("shard_pack." + namespace + ".name");
	}

	public static Text getSourceForMod(Mod mod) {
		return Text.literal(mod.getName());
	}

	public static Optional<Text> getSourceForModId(String modId) {
		try {
			return Optional.of(getSourceForMod(Platform.getMod(modId)));
		} catch (Exception e) {
			return Optional.empty();
		}
	}
	
	public static Text getSourceForSourceId(Identifier id) {
		if (!id.getPath().equals("shard_pack")) {
			return Text.translatable("shard_pack." + id.getNamespace() + "." + id.getPath() + ".name");
		}
		
		return getSourceForModId(id.getNamespace())
				.orElse(Text.translatable("shard_pack." + id.getNamespace() + ".name"));
	}
	
	public static Identifier getSourceIdForNamespace(String namespace) {
		return Identifier.of(namespace, "shard_pack");
	}
}
