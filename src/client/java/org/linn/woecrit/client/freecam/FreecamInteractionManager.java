package org.linn.woecrit.client.freecam;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.linn.woecrit.client.render.GhostRender;
import org.linn.woecrit.mixin.client.ClientPlayerInteractionManagerAccessor;

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
        if (!GhostRender.isEnabled()) {
            // Stop interaction when renderer is down
            return ActionResult.FAIL;
        }
        return super.interactBlock(player, hand, hitResult);
    }

    @Override
    public boolean breakBlock(BlockPos pos) {
        return super.breakBlock(pos);
    }

    @Override
    public boolean attackBlock(BlockPos pos, Direction direction) {
        if (!this.client.world.getWorldBorder().contains(pos)) {
            return false;
        }

        this.breakBlock(pos);
        ((ClientPlayerInteractionManagerAccessor) this).setBlockBreakingCooldown(5);

        return true;
    }
}
