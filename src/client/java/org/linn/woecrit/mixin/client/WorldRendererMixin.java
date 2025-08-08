package org.linn.woecrit.mixin.client;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.linn.woecrit.client.freecam.Freecam;
import org.linn.woecrit.client.render.GhostRender;
import org.linn.woecrit.client.world.GhostWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
    @Shadow @Final private ObjectArrayList<ChunkBuilder.BuiltChunk> builtChunks;

    @Shadow
    private @Nullable ClientWorld world;

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

    @Inject(method = "renderBlockLayers", at = @At("TAIL"), cancellable = true)
    private void renderGhostBlockLayers(
            Matrix4fc matrix4fc,
            double d,
            double e,
            double f,
            CallbackInfoReturnable<SectionRenderState> ci
    ) {
        if (Freecam.isEnabled()) {
            var dimension = world.getDimensionEntry();
            GhostRender ghostRender = GhostWorld.worldsTwinMap.get(dimension).render;
            SectionRenderState sectionRenderState = ghostRender.blockRender.renderBlockLayers(matrix4fc, d, e, f);
            sectionRenderState.renderSection(BlockRenderLayerGroup.OPAQUE);
            // TODO Others
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void getCamera(
            ObjectAllocator allocator,
            RenderTickCounter tickCounter,
            boolean renderBlockOutline,
            Camera camera,
            Matrix4f positionMatrix,
            Matrix4f projectionMatrix,
            GpuBufferSlice fog,
            Vector4f fogColor,
            boolean shouldRenderSky,
            CallbackInfo ci) {
        var dimension = world.getDimensionEntry();
        GhostRender ghostRender = GhostWorld.worldsTwinMap.get(dimension).render;
        ghostRender.blockRender.cameraPosition = camera.getCameraPos();
    }
}
