package net.modfest.scatteredshards;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ScatteredShardsConfig {

	public static final ScatteredShardsConfig CONFIG;
	public static final ModConfigSpec CONFIG_SPEC;

	public final ModConfigSpec.ConfigValue<Boolean> replace_advancements;

	private ScatteredShardsConfig(ModConfigSpec.Builder builder) {
		//Define each property
		//One property could be a message to log to the console when the game is initialised
		replace_advancements = builder
			.comment("Whether to replace advancements button with a shard collection button")
			.define("replace_advancements", true);
	}

	static {
		Pair<ScatteredShardsConfig, ModConfigSpec> pair =
			new ModConfigSpec.Builder().configure(ScatteredShardsConfig::new);

		//Store the resulting values
		CONFIG = pair.getLeft();
		CONFIG_SPEC = pair.getRight();
	}
}
