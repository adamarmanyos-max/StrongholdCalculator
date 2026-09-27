package com.adamarmanyos.stronghold;

import com.adamarmanyos.stronghold.StrongholdOverlay;
import net.fabricmc.api.ClientModInitializer;

public class StrongholdFinderClient
implements ClientModInitializer {
    public void onInitializeClient() {
        System.out.println("[strongholdfinder] loaded, engine: " + StrongholdOverlay.calculatorName());
    }
}

