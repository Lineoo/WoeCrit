package org.linn.woecrit.mixin.client;

import net.minecraft.client.Mouse;
import org.linn.woecrit.client.freecam.Freecam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Inject(
            method = "onMouseScroll",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSpectator()Z",
                    ordinal = 0))
    protected void onMouseScrollMixin(long window, double horizontal, double vertical, CallbackInfo callbackInfo) {
        if (Freecam.isEnabled()) {
            var freecamPlayer = Freecam.getFreecamPlayer();
            freecamPlayer.suspendFakeSpectatorOnce();
        }
    }
}
