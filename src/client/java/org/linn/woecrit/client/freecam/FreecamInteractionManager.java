package org.linn.woecrit.client.freecam;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.GameMode;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

public class FreecamInteractionManager extends ClientPlayerInteractionManager {
    private final MinecraftClient client = MinecraftClient.getInstance();

    public FreecamInteractionManager(FreecamNetworkHandler networkHandler) {
        super(MinecraftClient.getInstance(), networkHandler);
        super.setGameMode(GameMode.CREATIVE);
    }

    @Override
    public void setGameMode(GameMode gameMode) {
        // Captured
    }

    @Override
    public void setGameModes(GameMode gameMode, @Nullable GameMode previousGameMode) {
        // Captured
    }

    @Override
    public ActionResult interactBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult) {
        if (true) {
            return ActionResult.CONSUME;
        }
        return super.interactBlock(player, hand, hitResult);
    }
}
