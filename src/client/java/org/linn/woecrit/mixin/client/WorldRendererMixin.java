package org.linn.woecrit.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ColorHelper;
import org.linn.woecrit.client.freecam.Freecam;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private BufferBuilderStorage bufferBuilders;
    @Shadow protected abstract boolean canDrawEntityOutlines();
    @Shadow protected abstract void renderEntity(
            Entity entity,
            double cameraX,
            double cameraY,
            double cameraZ,
            float tickProgress,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers
    );

    @Inject(method = "renderEntities", at = @At("TAIL"))
    private void renderClientPlayer(
            MatrixStack matrices,
            VertexConsumerProvider.Immediate vertexConsumers,
            Camera camera,
            RenderTickCounter tickCounter,
            List<Entity> entities,
            CallbackInfo ci
    ) {
        if (!Freecam.isEnabled()) {
            return;
        }

        var position = camera.getPos();
        var tickManager = this.client.world.getTickManager();
        boolean outline = this.canDrawEntityOutlines();

        var entity = Freecam.getOriginalPlayer();

        if (entity.age == 0) {
            entity.lastRenderX = entity.getX();
            entity.lastRenderY = entity.getY();
            entity.lastRenderZ = entity.getZ();
        }

        VertexConsumerProvider vertexConsumerProvider;
        if (outline && this.client.hasOutline(entity)) {
            OutlineVertexConsumerProvider outlineVertexConsumerProvider = this.bufferBuilders.getOutlineVertexConsumers();
            vertexConsumerProvider = outlineVertexConsumerProvider;
            int team_color = entity.getTeamColorValue();
            outlineVertexConsumerProvider.setColor(
                    ColorHelper.getRed(team_color),
                    ColorHelper.getGreen(team_color),
                    ColorHelper.getBlue(team_color),
                    255);
        } else {
            vertexConsumerProvider = vertexConsumers;
        }

        float tick = tickCounter.getTickProgress(!tickManager.shouldSkipTick(entity));
        this.renderEntity(entity, position.getX(), position.getY(), position.getZ(), tick, matrices, vertexConsumerProvider);
    }
}
