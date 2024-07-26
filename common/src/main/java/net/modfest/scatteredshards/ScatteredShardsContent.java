package net.modfest.scatteredshards;

import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.modfest.scatteredshards.block.ShardBlock;
import net.modfest.scatteredshards.block.ShardBlockEntity;
import net.modfest.scatteredshards.item.ShardTablet;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.client.render.ShardBlockEntityRenderer;

public class ScatteredShardsContent {
	public static final Identifier SHARD_BLOCK_ID = ScatteredShards.id("shard_block");
	public static final Identifier SHARD_TABLET_ID = ScatteredShards.id("shard_tablet");

	public static RegistrySupplier<Block> SHARD_BLOCK;
	public static RegistrySupplier<Item> SHARD_BLOCK_ITEM;

	public static RegistrySupplier<Item> SHARD_TABLET;

	public static RegistrySupplier<BlockEntityType<ShardBlockEntity>> SHARD_BLOCKENTITY;
	
	public static void register() {
		Registrar<Item> ITEMS = ScatteredShards.REGISTRIES.get().get(Registries.ITEM);
		Registrar<Block> BLOCKS = ScatteredShards.REGISTRIES.get().get(Registries.BLOCK);
		Registrar<BlockEntityType<?>> BLOCK_ENTITY_TYPES = ScatteredShards.REGISTRIES.get().get(Registries.BLOCK_ENTITY_TYPE);

		SHARD_BLOCK = BLOCKS.register(SHARD_BLOCK_ID, ShardBlock::new);
		SHARD_BLOCK_ITEM = ITEMS.register(SHARD_BLOCK_ID, () -> new BlockItem(SHARD_BLOCK.get(), new Item.Settings()));
		SHARD_BLOCKENTITY = BLOCK_ENTITY_TYPES.register(SHARD_BLOCK_ID, () ->
				BlockEntityType.Builder.create(ShardBlockEntity::new, SHARD_BLOCK.get()).build(null));
		SHARD_TABLET = ITEMS.register(SHARD_TABLET_ID, () -> new ShardTablet(new Item.Settings()));
	}

	@Environment(EnvType.CLIENT)
	public static void registerClient() {
		BlockEntityRendererRegistry.register(SHARD_BLOCKENTITY.get(), ShardBlockEntityRenderer::new);
	}
}
