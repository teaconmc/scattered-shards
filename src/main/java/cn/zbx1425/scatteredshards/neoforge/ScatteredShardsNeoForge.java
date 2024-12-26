package cn.zbx1425.scatteredshards.neoforge;

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

@Mod(ScatteredShards.ID)
public class ScatteredShardsNeoForge {

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
	}
}
