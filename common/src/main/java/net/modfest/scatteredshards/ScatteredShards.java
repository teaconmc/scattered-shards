package net.modfest.scatteredshards;

import com.google.common.base.Suppliers;
import dev.architectury.registry.registries.RegistrarManager;
import io.github.cottonmc.cotton.gui.impl.LibGuiCommon;
import net.modfest.scatteredshards.load.ShardTypeLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.command.ShardCommand;
import net.modfest.scatteredshards.networking.ScatteredShardsNetworking;

import java.util.function.Supplier;

public class ScatteredShards {

	public static final String ID = "scattered_shards";

	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	public static Identifier id(String path) {
		return Identifier.of(ID, path);
	}
	
	public static String permission(String path) {
		return ID + "." + path;
	}

	public static final Supplier<RegistrarManager> REGISTRIES = Suppliers.memoize(() -> RegistrarManager.get(ID));

	private final LibGuiCommon libGuiCommon = new LibGuiCommon();

	public void onInitialize() {
		libGuiCommon.onInitialize();
		//ScatteredShardsAPI.init();
		ShardType.register();
		ShardTypeLoader.register();
		ShardCommand.register();
		ScatteredShardsNetworking.register();
		ScatteredShardsContent.register();
	}
}
