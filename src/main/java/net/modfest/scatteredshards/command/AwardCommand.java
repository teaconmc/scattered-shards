package net.modfest.scatteredshards.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.Shard;

import java.util.List;

public class AwardCommand {

	public static int award(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		EntitySelector target = ctx.getArgument("players", EntitySelector.class);
		Identifier shardId = ctx.getArgument("shard_id", Identifier.class);

		ShardLibrary library = ScatteredShardsAPI.getServerLibrary();
		Shard shard = library.shards().get(shardId).orElseThrow(() -> ShardCommand.INVALID_SHARD.create(shardId)); // Validate shardId
		List<ServerPlayerEntity> targets = target.getPlayers(ctx.getSource());

		int i = 0;
		for (ServerPlayerEntity player : targets) {
			if (ScatteredShardsAPI.triggerShardCollection(player, shardId)) {
				i++;
			}
		}
		final int collected = i;

		Text shardName = shard.name().getString().isBlank() ? Text.of(shardId.toString()) : shard.name();

		if (collected == 0) {
			if (targets.size() != 1) {
				ctx.getSource().sendFeedback(() -> Text.stringifiedTranslatable("commands.scattered_shards.shard.award.none", shardName), false);
			} else {
				ctx.getSource().sendFeedback(() -> Text.stringifiedTranslatable("commands.scattered_shards.shard.collect.already", shardName, targets.size()), false);
			}
		} else if (collected == 1) {
			ctx.getSource().sendFeedback(() -> Text.stringifiedTranslatable("commands.scattered_shards.shard.collect", shardName), false);
		} else {
			ctx.getSource().sendFeedback(() -> Text.stringifiedTranslatable("commands.scattered_shards.shard.award", shardName, collected), false);
		}

		return collected;
	}

	public static void register(CommandNode<ServerCommandSource> parent) {
		CommandNode<ServerCommandSource> awardCommand = ShardCommandNodeHelper.literal("award")
			.requires(Permissions.require(ScatteredShards.permission("command.award"), 2))
			.build();
		CommandNode<ServerCommandSource> awardPlayerArgument = ShardCommandNodeHelper.players("players").build();
		CommandNode<ServerCommandSource> awardIdArgument = ShardCommandNodeHelper.shardId("shard_id")
			.executes(AwardCommand::award)
			.build();
		parent.addChild(awardCommand);
		awardCommand.addChild(awardPlayerArgument);
		awardPlayerArgument.addChild(awardIdArgument);
	}
}
