package io.github.cumsauce.doorswing;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

/**
 * Renders every animating door as a rotating moving-block submit during COLLECT_SUBMITS.
 * The chunk mesh omits animating doors (RenderSectionRegionMixin); this draws the
 * intermediate pose instead.
 */
public final class DoorSwingRenderer {
    private static ClientLevel lastLevel;

    private DoorSwingRenderer() {
    }

    public static void register() {
        LevelRenderEvents.COLLECT_SUBMITS.register(DoorSwingRenderer::render);
    }

    /** Called on disconnect: drop tracker state before the next world's chunk meshing starts. */
    public static void reset() {
        DoorSwingAnimations.clear();
        lastLevel = null;
    }

    private static void render(LevelRenderContext ctx) {
        if (DoorSwingAnimations.isEmpty()) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            DoorSwingAnimations.clear();
            lastLevel = null;
            return;
        }
        if (level != lastLevel) {
            // New world or dimension: entries from the previous level are stale. On the very
            // first frame lastLevel is still null, so keep the swing that just started instead
            // of dropping it (a dropped entry also never re-dirties its section).
            if (lastLevel != null) {
                DoorSwingAnimations.clear();
            }
            lastLevel = level;
        }

        long now = System.currentTimeMillis();
        LevelRenderState levelState = ctx.levelState();
        Vec3 cam = levelState.cameraRenderState.pos;
        SubmitNodeCollector collector = ctx.submitNodeCollector();
        PoseStack pose = ctx.poseStack();

        for (Map.Entry<Long, DoorAnimation> entry : DoorSwingAnimations.entries()) {
            long key = entry.getKey();
            DoorAnimation anim = entry.getValue();
            if (anim.isDone(now)) {
                BlockPos donePos = BlockPos.of(key);
                DoorSwingIntegration.driver().onDoorFinished(key,
                        donePos.getX() + 0.5, donePos.getY() + 0.5, donePos.getZ() + 0.5, anim.profile);
                DoorSwingAnimations.finish(key);
                continue;
            }
            BlockPos pos = BlockPos.of(key);
            if (!level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
                continue;
            }
            BlockState real = level.getBlockState(pos);
            if (!(real.getBlock() instanceof DoorBlock)
                    || real.getRenderShape() != RenderShape.MODEL) {
                DoorSwingAnimations.remove(key);
                continue;
            }
            float p = anim.progress(now);
            DoorSwingIntegration.driver().onDoorMoving(key,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, anim.profile, p, now);
            boolean hingeLeft = real.getValue(DoorBlock.HINGE) == DoorHingeSide.LEFT;
            boolean useOpenModel = DoorAnimation.openModel(p);
            BlockState model = real.setValue(DoorBlock.OPEN, useOpenModel);
            // Closed model is in the closed pose; open model already carries the full
            // open rotation, so only the *remaining* delta is applied to it.
            float angle = useOpenModel
                    ? DoorAnimation.angleDeg(p - 1f, hingeLeft)
                    : DoorAnimation.angleDeg(p, hingeLeft);

            MovingBlockRenderState moving = new MovingBlockRenderState();
            moving.randomSeedPos = pos;
            moving.blockPos = pos;
            moving.blockState = model;
            moving.biome = level.getBiome(pos);
            moving.cardinalLighting = level.cardinalLighting();
            moving.lightEngine = level.getLightEngine();

            // Submit pose space is camera-relative (vanilla entities use world - camera).
            // Anchor at the block's vertical center axis, rotate, then shift to the model corner.
            pose.pushPose();
            pose.translate(pos.getX() + 0.5 - cam.x, pos.getY() - cam.y, pos.getZ() + 0.5 - cam.z);
            pose.rotateDegrees(Axis.YP, angle);
            pose.translate(-0.5, 0, -0.5);
            collector.submitMovingBlock(pose, moving, EntityRenderState.NO_OUTLINE);
            pose.popPose();
        }
    }
}
