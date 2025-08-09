package org.linn.woecrit.mixin.client.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import org.linn.woecrit.client.freecam.Freecam;
import org.linn.woecrit.client.world.GhostWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
    @Shadow
    public abstract Block getBlock();

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void useOnBlockAsGhost(ItemUsageContext usageContext, CallbackInfoReturnable<ActionResult> cir) {
        if (!Freecam.isEnabled()) {
            return;
        } else {
            cir.cancel();
        }

        var context = new ItemPlacementContext(usageContext);

        if (!this.getBlock().isEnabled(context.getWorld().getEnabledFeatures())) {
            cir.setReturnValue(ActionResult.FAIL);
        }

        if (!context.canPlace()) {
            cir.setReturnValue(ActionResult.FAIL);
        }

        BlockState blockState = this.getBlock().getPlacementState(context);

        if (blockState == null) {
            cir.setReturnValue(ActionResult.FAIL);
        }

        var world = context.getWorld();
        var ghostWorld = GhostWorld.worldsTwinMap.get(world.getDimensionEntry());
        var success = ghostWorld.setBlockState(context.getBlockPos(), blockState);

        cir.setReturnValue(success ? ActionResult.SUCCESS : ActionResult.FAIL);
    }
}
