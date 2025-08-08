package org.linn.woecrit.client;

import net.fabricmc.api.ClientModInitializer;
import org.linn.woecrit.client.freecam.Freecam;

public class WoecritClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Freecam.register();
    }

}
