package net.modfest.scatteredshards;

import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
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
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.client.render.ShardBlockEntityRenderer;

public class ScatteredShardsContent {
	public static final Identifier SHARD_BLOCK_ID = ScatteredShards.id("shard_block");
	public static final Identifier SHARD_TABLET_ID = ScatteredShards.id("shard_tablet");

	public static final Block SHARD_BLOCK = new ShardBlock();
	public static final Item SHARD_BLOCK_ITEM = new BlockItem(SHARD_BLOCK, new Item.Settings());

	public static final Item SHARD_TABLET = new ShardTablet(new Item.Settings());

	public static final BlockEntityType<ShardBlockEntity> SHARD_BLOCKENTITY = BlockEntityType.Builder.create(ShardBlockEntity::new, SHARD_BLOCK).build(null);
	
	public static void register() {
		Registry.register(Registries.BLOCK, SHARD_BLOCK_ID, SHARD_BLOCK);
		Registry.register(Registries.ITEM, SHARD_BLOCK_ID, SHARD_BLOCK_ITEM);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, SHARD_BLOCK_ID, SHARD_BLOCKENTITY);
		Registry.register(Registries.ITEM, SHARD_TABLET_ID, SHARD_TABLET);
	}

	@Environment(EnvType.CLIENT)
	public static void registerClient() {
		BlockEntityRendererRegistry.register(SHARD_BLOCKENTITY, ShardBlockEntityRenderer::new);
	}
}
