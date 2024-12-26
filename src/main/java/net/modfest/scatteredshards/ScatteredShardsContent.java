package net.modfest.scatteredshards;

import cn.zbx1425.scatteredshards.RegistriesWrapper;
import cn.zbx1425.scatteredshards.Lazy;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.block.ShardBlock;
import net.modfest.scatteredshards.block.ShardBlockEntity;
import net.modfest.scatteredshards.client.render.ShardBlockEntityRenderer;
import net.modfest.scatteredshards.item.ShardTablet;

public class ScatteredShardsContent {
	public static final Identifier SHARD_BLOCK_ID = ScatteredShards.id("shard_block");
	public static final Identifier SHARD_TABLET_ID = ScatteredShards.id("shard_tablet");

	public static final Lazy<Block> SHARD_BLOCK = Lazy.of(ShardBlock::new);
	public static final Lazy<Item> SHARD_BLOCK_ITEM = Lazy.of(() -> new BlockItem(SHARD_BLOCK.get(), new Item.Settings()));

	public static final Lazy<Item> SHARD_TABLET = Lazy.of(() -> new ShardTablet(new Item.Settings()));

	public static final Lazy<BlockEntityType<ShardBlockEntity>> SHARD_BLOCKENTITY = Lazy.of(() -> BlockEntityType.Builder.create(ShardBlockEntity::new, SHARD_BLOCK.get()).build(null));

	public static void register(RegistriesWrapper registries) {
		registries.registerBlock(SHARD_BLOCK_ID.getPath(), SHARD_BLOCK);
		registries.registerItem(SHARD_BLOCK_ID.getPath(), SHARD_BLOCK_ITEM);
		registries.registerBlockEntityType(SHARD_BLOCK_ID.getPath(), SHARD_BLOCKENTITY);
		registries.registerItem(SHARD_TABLET_ID.getPath(), SHARD_TABLET);
	}

	@Environment(EnvType.CLIENT)
	public static void registerClient() {
		BlockEntityRendererFactories.register(ScatteredShardsContent.SHARD_BLOCKENTITY.get(), ShardBlockEntityRenderer::new);
	}
}
