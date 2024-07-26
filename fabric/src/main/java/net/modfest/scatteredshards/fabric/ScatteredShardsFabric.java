package net.modfest.scatteredshards.fabric;

import net.fabricmc.api.ModInitializer;
import net.modfest.scatteredshards.ScatteredShards;

public class ScatteredShardsFabric implements ModInitializer {

    ScatteredShards scatteredShards = new ScatteredShards();

    @Override
    public void onInitialize() {
        scatteredShards.onInitialize();
    }
}
