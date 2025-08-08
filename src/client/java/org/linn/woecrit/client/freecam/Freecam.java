package org.linn.woecrit.client.freecam;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.util.PlayerInput;
import org.lwjgl.glfw.GLFW;

public class Freecam {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final KeyBinding TOGGLE_FREECAM = new KeyBinding(
            "key.woecrit.toggleFreecam",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "category.woecrit.woecrit");

    private static boolean enabled = false;

    private static FreecamEntity freecamPlayer;
    private static FreecamInteractionManager freecamInteractionManager;
    private static FreecamNetworkHandler freecamNetworkHandler;

    private static ClientPlayerEntity originPlayer;
    private static ClientPlayerInteractionManager originInteractionManager;
    private static ClientPlayNetworkHandler originNetworkHandler;

    public static boolean isEnabled() {
        return enabled;
    }

    public static ClientPlayerEntity getOriginalPlayer() {
        return originPlayer;
    }
    public static FreecamEntity getFreecamPlayer() {
        return freecamPlayer;
    }

    public static void register() {
        KeyBindingHelper.registerKeyBinding(TOGGLE_FREECAM);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TOGGLE_FREECAM.wasPressed()) {
                Freecam.toggle();
            }
        });
    }

    public static void toggle() {
        if(!enabled) {
            // Enable
            enabled = true;

            originPlayer = CLIENT.player;
            originInteractionManager = CLIENT.interactionManager;
            originNetworkHandler = CLIENT.getNetworkHandler();

            freecamNetworkHandler = new FreecamNetworkHandler(originNetworkHandler);
            freecamPlayer = new FreecamEntity(originPlayer, freecamNetworkHandler);
            freecamInteractionManager = new FreecamInteractionManager(freecamNetworkHandler);

            CLIENT.player = freecamPlayer;
            CLIENT.setCameraEntity(freecamPlayer);
            CLIENT.interactionManager = freecamInteractionManager;

            freecamPlayer.copyPositionAndRotation(originPlayer);
            freecamPlayer.clientWorld.addEntity(freecamPlayer);
            froze_control();
        } else {
            // Disable
            enabled = false;

            CLIENT.player = originPlayer;
            CLIENT.setCameraEntity(originPlayer);
            CLIENT.interactionManager = originInteractionManager;

            defrost_control();
            freecamPlayer.discard();

            freecamPlayer = null;
            freecamNetworkHandler = null;
            freecamInteractionManager = null;
        }
    }

    private static void froze_control() {
        // TODO an option which controls whether to keeps the status
        boolean keep_shift = true;

        var playerInput = originPlayer.input.playerInput;
        var input = new Input();
        input.playerInput = new PlayerInput(
                false,
                false,
                false,
                false,
                false,
                playerInput.sneak() && keep_shift,
                false
        );
        originNetworkHandler.sendPacket(new PlayerInputC2SPacket(input.playerInput));
        originPlayer.input = input;
        freecamPlayer.input = new KeyboardInput(CLIENT.options);
    }

    private static void defrost_control() {
        freecamPlayer.input = new Input();
        originPlayer.input = new KeyboardInput(CLIENT.options);
    }
}
