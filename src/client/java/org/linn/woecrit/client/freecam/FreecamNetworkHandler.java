package org.linn.woecrit.client.freecam;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.ServerLinks;

import java.util.Collections;
import java.util.UUID;

public class FreecamNetworkHandler extends ClientPlayNetworkHandler {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    public FreecamNetworkHandler(ClientPlayNetworkHandler originNetworkHandler) {
        super(
                MinecraftClient.getInstance(),
                originNetworkHandler.getConnection(),
                new ClientConnectionState(
                        new GameProfile(UUID.randomUUID(), "Freecam"),
                        CLIENT.getTelemetryManager().createWorldSession(false, null, null),
                        CLIENT.player.getRegistryManager().toImmutable(),
                        originNetworkHandler.getEnabledFeatures(),
                        null,
                        CLIENT.getCurrentServerEntry(),
                        CLIENT.currentScreen,
                        Collections.emptyMap(),
                        CLIENT.inGameHud.getChatHud().toChatState(),
                        Collections.emptyMap(),
                        ServerLinks.EMPTY
                ));
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        // :p Captured
    }

    @Override
    public FeatureSet getEnabledFeatures() {
        return super.getEnabledFeatures();
    }
}
