package cn.zbx1425.scatteredshards.neoforge;

import cn.zbx1425.scatteredshards.ServerConfig;
import cn.zbx1425.scatteredshards.sync.RedisSynchronizer;
import cn.zbx1425.scatteredshards.sync.SyncDispatcher;
import io.github.cottonmc.cotton.gui.impl.LibGuiCommon;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.load.ShardTypeLoader;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.io.IOException;

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
				SyncDispatcher.INSTANCE = new SyncDispatcher(event.getServer(),
					SERVER_CONFIG.syncRole.value.equalsIgnoreCase("host"));
				if (!SERVER_CONFIG.redisUrl.value.isEmpty()) {
					SyncDispatcher.INSTANCE.peerChannel = new RedisSynchronizer(SERVER_CONFIG.redisUrl.value);
				}
			} catch (IOException e) {
				ScatteredShards.LOGGER.error("Failed to load server config", e);
			}
		}

		@SubscribeEvent
		public static void onServerStopping(ServerStoppingEvent event) {
			try {
				if (SyncDispatcher.INSTANCE != null) {
					SyncDispatcher.INSTANCE.close();
					SyncDispatcher.INSTANCE = null;
				}
			} catch (Exception e) {
				ScatteredShards.LOGGER.error("Failed to close sync dispatcher", e);
			}
		}
	}
}
