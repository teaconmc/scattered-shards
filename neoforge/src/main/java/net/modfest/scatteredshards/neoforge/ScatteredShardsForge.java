package net.modfest.scatteredshards.neoforge;

import net.modfest.scatteredshards.ScatteredShards;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(ScatteredShards.ID)
public class ScatteredShardsForge {

    ScatteredShards scatteredShards = new ScatteredShards();

    public ScatteredShardsForge(IEventBus eventBus) {
        scatteredShards.onInitialize();
    }
}
