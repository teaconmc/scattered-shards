package net.modfest.scatteredshards.client.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.client.screen.ShardCreatorGuiDescription;
import net.modfest.scatteredshards.client.screen.ShardTabletGuiDescription;
import net.modfest.scatteredshards.command.ShardCommand;

import java.util.concurrent.CompletableFuture;

public class ClientShardCommand {

	private static DynamicCommandExceptionType createInvalidException(String item) {
		return new DynamicCommandExceptionType(
				obj -> Text.stringifiedTranslatable("error.scattered_shards.invalid_" + item, obj)
		);
	}

	private static final DynamicCommandExceptionType INVALID_SET_ID = createInvalidException("set_id");
	private static final DynamicCommandExceptionType INVALID_SHARD_ID = createInvalidException("shard_id");

	public static int view(CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> context) throws CommandSyntaxException {
		Identifier id = context.getArgument("set_id", Identifier.class);
		var shards = ScatteredShardsAPI.getClientLibrary().shardSets().get(id);
		if (shards.isEmpty()) {
			throw INVALID_SET_ID.create(id);
		}
		return Command.SINGLE_SUCCESS;
	}

	public static int creatorNew(CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> context) throws CommandSyntaxException {
		String modId = StringArgumentType.getString(context, "mod_id");
		Identifier shardTypeId = context.getArgument("shard_type", Identifier.class);
		ShardType shardType = ScatteredShardsAPI.getClientLibrary().shardTypes().get(shardTypeId)
				.orElseThrow(() -> ShardCommand.INVALID_SHARD_TYPE.create(shardTypeId));
		
		var client = MinecraftClient.getInstance();
		client.send(() -> client.setScreen(ShardCreatorGuiDescription.Screen.newShard(modId, shardType)));
		return Command.SINGLE_SUCCESS;
	}

	public static int creatorEdit(CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> context) throws CommandSyntaxException {
		Identifier shardId = context.getArgument("shard_id", Identifier.class);
		Shard shard = ScatteredShardsAPI.getClientLibrary().shards().get(shardId)
				.orElseThrow(() -> INVALID_SHARD_ID.create(shardId));
		
		var client = MinecraftClient.getInstance();
		client.send(() -> client.setScreen(ShardCreatorGuiDescription.Screen.editShard(shard)));
		return Command.SINGLE_SUCCESS;
	}

	public static int shards(CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> context) throws CommandSyntaxException {
		var client = MinecraftClient.getInstance();
		var library = ScatteredShardsAPI.getClientLibrary();
		var collection = ScatteredShardsAPI.getClientCollection();

		client.send(() -> client.setScreen(new ShardTabletGuiDescription.Screen(collection, library)));

		return Command.SINGLE_SUCCESS;
	}

	public static CompletableFuture<Suggestions> suggestShardSets(CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> context, SuggestionsBuilder builder) {
		for (var id : ScatteredShardsAPI.getClientLibrary().shardSets().keySet()) {
			builder.suggest(id.toString());
		}
		return builder.buildFuture();
	}

	public static CompletableFuture<Suggestions> suggestShards(CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> context, SuggestionsBuilder builder) {
		ScatteredShardsAPI.getClientLibrary().shards().forEach((id, shard) -> {
			builder.suggest(id.toString());
		});
		return builder.buildFuture();
	}

	public static CompletableFuture<Suggestions> suggestShardTypes(CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> context, SuggestionsBuilder builder) {
		ScatteredShardsAPI.getClientLibrary().shardTypes().forEach((id, shardSet) -> {
			if (!id.equals(ShardType.MISSING_ID)) {
				builder.suggest(id.toString());
			}
		});
		return builder.buildFuture();
	}

	public static CompletableFuture<Suggestions> suggestModIds(CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> context, SuggestionsBuilder builder) {
		for (var mod : FabricLoader.getInstance().getAllMods()) {
			builder.suggest(mod.getMetadata().getId());
		}
		return builder.buildFuture();
	}

	private static LiteralCommandNode<ClientCommandRegistrationEvent.ClientCommandSourceStack> literal(String name) {
		return LiteralArgumentBuilder.<ClientCommandRegistrationEvent.ClientCommandSourceStack>literal(name).build();
	}

	private static LiteralCommandNode<ClientCommandRegistrationEvent.ClientCommandSourceStack> literal(String name, Command<ClientCommandRegistrationEvent.ClientCommandSourceStack> command) {
		return LiteralArgumentBuilder.<ClientCommandRegistrationEvent.ClientCommandSourceStack>literal(name).executes(command).build();
	}

	private static RequiredArgumentBuilder<ClientCommandRegistrationEvent.ClientCommandSourceStack, Identifier> identifierArgument(String name) {
		return RequiredArgumentBuilder.<ClientCommandRegistrationEvent.ClientCommandSourceStack, Identifier>argument(name, IdentifierArgumentType.identifier());
	}

	private static RequiredArgumentBuilder<ClientCommandRegistrationEvent.ClientCommandSourceStack, String> stringArgument(String name) {
		return RequiredArgumentBuilder.<ClientCommandRegistrationEvent.ClientCommandSourceStack, String>argument(name, StringArgumentType.string());
	}

	public static void register() {
		ClientCommandRegistrationEvent.EVENT.register((dispatcher, registryAccess) -> {

			var shardcRoot = literal("shardc");
			dispatcher.getRoot().addChild(shardcRoot);

			//Usage: /shardc view <set_id>
			var view = literal("view");
			var setId = identifierArgument("set_id")
					.suggests(ClientShardCommand::suggestShardSets)
					.executes(ClientShardCommand::view);
			view.addChild(setId.build());
			shardcRoot.addChild(view);

			//Usage: /shardc creator
				//-> new <mod_id> <shard_type>
				//-> edit <shard_id>
			var creator = literal("creator");

			var creatorNew = literal("new");
			var modId = stringArgument("mod_id")
					.suggests(ClientShardCommand::suggestModIds);
			var modIdBuild = modId.build();
			var shardType = identifierArgument("shard_type")
					.suggests(ClientShardCommand::suggestShardTypes)
					.executes(ClientShardCommand::creatorNew);
			modIdBuild.addChild(shardType.build());
			creatorNew.addChild(modIdBuild);

			var creatorEdit = literal("edit");
			var shardId = identifierArgument("shard_id")
					.suggests(ClientShardCommand::suggestShards)
					.executes(ClientShardCommand::creatorEdit);
			creatorEdit.addChild(shardId.build());

			creator.addChild(creatorNew);
			creator.addChild(creatorEdit);

			shardcRoot.addChild(creator);

			//Usage: /shards
			var shardsCommand = literal("shards", ClientShardCommand::shards);
			dispatcher.getRoot().addChild(shardsCommand);
		});
	}
}
