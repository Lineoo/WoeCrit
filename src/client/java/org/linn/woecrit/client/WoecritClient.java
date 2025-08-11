package org.linn.woecrit.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.linn.woecrit.client.freecam.Freecam;
import org.linn.woecrit.client.gui.screen.WoecritMainScreen;
import org.linn.woecrit.client.render.GhostRender;
import org.lwjgl.glfw.GLFW;

public class WoecritClient implements ClientModInitializer {
    public static final KeyBinding TOGGLE_FREECAM = new KeyBinding(
            "key.woecrit.toggleFreecam",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "category.woecrit.woecrit");

    public static final KeyBinding TOGGLE_GHOST_RENDER = new KeyBinding(
            "key.woecrit.toggleGhostRender",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "category.woecrit.woecrit");

    public static final KeyBinding TOGGLE_MAIN_SCREEN = new KeyBinding(
            "key.woecrit.toggleMainScreen",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "category.woecrit.woecrit");


    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(TOGGLE_FREECAM);
        KeyBindingHelper.registerKeyBinding(TOGGLE_GHOST_RENDER);
        KeyBindingHelper.registerKeyBinding(TOGGLE_MAIN_SCREEN);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TOGGLE_FREECAM.wasPressed()) {
                Freecam.toggle();
            }
            while (TOGGLE_GHOST_RENDER.wasPressed()) {
                GhostRender.toggle();
            }
            while (TOGGLE_MAIN_SCREEN.wasPressed()) {
                client.setScreen(new WoecritMainScreen());
            }
        });
    }

}
