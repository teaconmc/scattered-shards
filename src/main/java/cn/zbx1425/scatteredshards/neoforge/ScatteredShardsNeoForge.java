package cn.zbx1425.scatteredshards.neoforge;

import cn.zbx1425.scatteredshards.ServerConfig;
import cn.zbx1425.scatteredshards.sync.RedisSynchronizer;
import cn.zbx1425.scatteredshards.sync.SyncPersistDispatcher;
import cn.zbx1425.scatteredshards.sync.Synchronizer;
import io.github.cottonmc.cotton.gui.impl.LibGuiCommon;
import net.minecraft.server.network.ServerPlayerEntity;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.load.ShardTypeLoader;
import net.modfest.scatteredshards.networking.ScatteredShardsNetworking;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@Mod(ScatteredShards.ID)
public class ScatteredShardsNeoForge {

	public static ServerConfig SERVER_CONFIG = new ServerConfig();

	public ScatteredShardsNeoForge(IEventBus eventBus, ModContainer container) {
		RegistriesWrapperImpl registries = new RegistriesWrapperImpl();
		new LibGuiCommon().onInitialize();
		new ScatteredShards().onInitialize(registries);
		registries.registerAllDeferred(eventBus);
		NeoForge.EVENT_BUS.register(NeoForgeEventBusListener.class);
		if (FMLEnvironment.dist.isClient()) {
			new ScatteredShardsNeoForgeClient(eventBus, container);
		}
	}

	public static class NeoForgeEventBusListener {

		@SubscribeEvent
		public static void onRegisterClientReloadListeners(AddReloadListenerEvent event) {
			event.addListener(new ShardTypeLoader());
		}

		@SubscribeEvent
		public static void onServerStarting(ServerStartingEvent event) {
			try {
				SERVER_CONFIG.load(event.getServer().getRunDirectory()
					.resolve("config").resolve("scattered_shards.json"));
				Synchronizer peerChannel;
				if (!SERVER_CONFIG.redisUrl.value.isEmpty()) {
					peerChannel = new RedisSynchronizer(SERVER_CONFIG.redisUrl.value);
				} else {
					peerChannel = null;
				}
				SyncPersistDispatcher.CURRENT = new SyncPersistDispatcher(
					event.getServer(),
					SERVER_CONFIG.syncRole.value.equalsIgnoreCase("host"),
					peerChannel
				);
				SyncPersistDispatcher.CURRENT.loadFromToShareOrDiskAndInto(ScatteredShardsAPI.exportServerCollections());
			} catch (Exception e) {
				ScatteredShards.LOGGER.error("Failed to use server config", e);
			}
		}

		@SubscribeEvent
		public static void onServerStopping(ServerStoppingEvent event) {
			try {
				if (SyncPersistDispatcher.CURRENT != null) {
					SyncPersistDispatcher.CURRENT.close();
					SyncPersistDispatcher.CURRENT = null;
				}
			} catch (Exception e) {
				ScatteredShards.LOGGER.error("Failed to close sync dispatcher", e);
			}
		}

		@SubscribeEvent
		public static void onPlayerJoinServer(PlayerEvent.PlayerLoggedInEvent event) {
			if (event.getEntity() instanceof ServerPlayerEntity serverPlayer) {
				ScatteredShardsNetworking.onPlayerJoinServer(serverPlayer.getServer(), serverPlayer);
			}
		}
	}
}
