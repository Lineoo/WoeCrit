package org.linn.woecrit.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.world.ClientWorld;
import org.linn.woecrit.client.freecam.Freecam;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(
            method = "handleInputEvents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSpectator()Z",
                    ordinal = 0))
    protected void allowSlotSelection(CallbackInfo ci) {
        if (Freecam.isEnabled()) {
            var player = Freecam.getFreecamPlayer();
            player.suspendFakeSpectatorOnce();
        }
    }

    @Inject(method = "setWorld", at = @At("HEAD"))
    private void resetBeforeChangineWorld(ClientWorld world, CallbackInfo ci) {
        if (Freecam.isEnabled()) {
            Freecam.toggle();
        }
    }

    @Inject(method = "disconnect", at = @At("HEAD"))
    private void resetBeforeDisconnection(Screen disconnectionScreen, boolean transferring, CallbackInfo ci) {
        if (Freecam.isEnabled()) {
            Freecam.toggle();
        }
    }
}
