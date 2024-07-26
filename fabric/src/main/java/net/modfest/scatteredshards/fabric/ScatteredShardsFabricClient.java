package net.modfest.scatteredshards.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.modfest.scatteredshards.client.ScatteredShardsClient;

public class ScatteredShardsFabricClient implements ClientModInitializer {

    private final ScatteredShardsClient scatteredShardsClient = new ScatteredShardsClient();

    @Override
    public void onInitializeClient() {
        scatteredShardsClient.onInitializeClient();
    }
}
