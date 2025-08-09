package org.linn.woecrit.client.freecam;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

public class FreecamEntity extends ClientPlayerEntity {
    private boolean suspendFakeSpectator = false;

    public FreecamEntity(ClientPlayerEntity originPlayer, FreecamNetworkHandler networkHandler) {
        super(
                MinecraftClient.getInstance(),
                originPlayer.clientWorld,
                networkHandler,
                originPlayer.getStatHandler(),
                originPlayer.getRecipeBook(),
                PlayerInput.DEFAULT,
                false);
        setId(-1325);
        setLoaded(true);
        var abilities = getAbilities();
        abilities.flying = true;
    }

    public void suspendFakeSpectatorOnce() {
        suspendFakeSpectator = true;
    }

    @Override
    public void baseTick() {
        super.baseTick();
        var abilities = getAbilities();
        abilities.flying = true;
    }

    @Override
    public boolean isBlockBreakingRestricted(World world, BlockPos pos, GameMode gameMode) {
        return true;
    }

    @Override
    protected boolean shouldTickBlockCollision() {
        return false;
    }

    @Override
    public boolean isSpectator() {
        if (suspendFakeSpectator) {
            suspendFakeSpectator = false;
            return false;
        }
        return true;
    }

    @Override
    public ItemStack getStackInHand(Hand hand) {
        return super.getStackInHand(hand);
    }

    @Override
    public boolean isInCreativeMode() {
        return true;
    }

    @Override
    public boolean canPlaceOn(BlockPos pos, Direction facing, ItemStack stack) {
        return false;
    }
}
