package com.chimera.entity;

import com.chimera.item.WebSlingerItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

// Byproduct economy work order Milestone 3b: renders the Web Slinger's hook. Delegates the
// spinning-icon rendering to a held ThrownItemRenderer (the mod's original, simpler renderer
// choice) so the look doesn't change, and additionally draws a string from the firing player's
// hand to the hook - ported directly from vanilla's FishingHookRenderer (there's no shared/
// reusable "line between two points" helper; both the fishing line and the leash line hand-roll
// their own vertex-buffer code, and the leash path is gated behind real Leashable semantics this
// entity doesn't want), swapping its Items.FISHING_ROD arm-side check for WebSlingerItem.
@OnlyIn(Dist.CLIENT)
public class WebHookRenderer extends EntityRenderer<WebHookEntity> {

    private final ThrownItemRenderer<WebHookEntity> itemRenderer;

    public WebHookRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = new ThrownItemRenderer<>(context);
    }

    @Override
    public void render(WebHookEntity hook, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        itemRenderer.render(hook, entityYaw, partialTick, poseStack, buffer, packedLight);

        if (hook.getOwner() instanceof Player player) {
            poseStack.pushPose();
            float attackAnim = player.getAttackAnim(partialTick);
            float armSwing = Mth.sin(Mth.sqrt(attackAnim) * (float) Math.PI);
            Vec3 handPos = getPlayerHandPos(player, armSwing, partialTick);
            Vec3 hookPos = hook.getPosition(partialTick).add(0.0, 0.25, 0.0);
            float dx = (float) (handPos.x - hookPos.x);
            float dy = (float) (handPos.y - hookPos.y);
            float dz = (float) (handPos.z - hookPos.z);
            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.lineStrip());
            PoseStack.Pose pose = poseStack.last();
            int segments = 16;
            for (int i = 0; i <= segments; i++) {
                stringVertex(dx, dy, dz, vertexConsumer, pose, fraction(i, segments), fraction(i + 1, segments));
            }
            poseStack.popPose();
        }
    }

    private Vec3 getPlayerHandPos(Player player, float armSwing, float partialTick) {
        int side = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
        if (!(player.getMainHandItem().getItem() instanceof WebSlingerItem)) {
            side = -side;
        }

        if (this.entityRenderDispatcher.options.getCameraType().isFirstPerson() && player == Minecraft.getInstance().player) {
            double fovScale = 960.0 / this.entityRenderDispatcher.options.fov().get().intValue();
            Vec3 offset = this.entityRenderDispatcher.camera.getNearPlane().getPointOnPlane(side * 0.525F, -0.1F)
                    .scale(fovScale)
                    .yRot(armSwing * 0.5F)
                    .xRot(-armSwing * 0.7F);
            return player.getEyePosition(partialTick).add(offset);
        } else {
            float bodyRot = Mth.lerp(partialTick, player.yBodyRotO, player.yBodyRot) * (float) (Math.PI / 180.0);
            double sin = Mth.sin(bodyRot);
            double cos = Mth.cos(bodyRot);
            float scale = player.getScale();
            double sideOffset = side * 0.35 * scale;
            double forwardOffset = 0.8 * scale;
            float crouchOffset = player.isCrouching() ? -0.1875F : 0.0F;
            return player.getEyePosition(partialTick)
                    .add(-cos * sideOffset - sin * forwardOffset, crouchOffset - 0.45 * scale, -sin * sideOffset + cos * forwardOffset);
        }
    }

    private static float fraction(int numerator, int denominator) {
        return (float) numerator / (float) denominator;
    }

    private static void stringVertex(float x, float y, float z, VertexConsumer vertexConsumer, PoseStack.Pose pose, float frac0, float frac1) {
        float f = x * frac0;
        float f1 = y * (frac0 * frac0 + frac0) * 0.5F + 0.25F;
        float f2 = z * frac0;
        float f3 = x * frac1 - f;
        float f4 = y * (frac1 * frac1 + frac1) * 0.5F + 0.25F - f1;
        float f5 = z * frac1 - f2;
        float length = Mth.sqrt(f3 * f3 + f4 * f4 + f5 * f5);
        f3 /= length;
        f4 /= length;
        f5 /= length;
        vertexConsumer.addVertex(pose, f, f1, f2).setColor(-16777216).setNormal(pose, f3, f4, f5);
    }

    @Override
    public ResourceLocation getTextureLocation(WebHookEntity hook) {
        return itemRenderer.getTextureLocation(hook);
    }
}
