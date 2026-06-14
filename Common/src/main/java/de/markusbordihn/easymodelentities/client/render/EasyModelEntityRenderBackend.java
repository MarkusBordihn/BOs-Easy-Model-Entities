/*
 * Copyright 2026 Markus Bordihn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.easymodelentities.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.markusbordihn.easymodelentities.api.EasyModelRenderable;
import de.markusbordihn.easymodelentities.entity.EasyModelHostEntity;
import de.markusbordihn.easymodelentities.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.profile.EasyModelProfileService;
import de.markusbordihn.easymodelentities.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.profile.ModelType;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.render.EasyModelRenderState;
import de.markusbordihn.easymodelentities.render.EasyModelRenderStateResolver;
import de.markusbordihn.easymodelentities.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.renderprofile.EasyModelRenderProfileService;
import de.markusbordihn.easymodelentities.runtime.EasyModelAnimationState;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class EasyModelEntityRenderBackend {

  private EasyModelEntityRenderBackend() {}

  public static EasyModelRenderState resolveRenderState(EasyModelRuntimeContract contract) {
    return EasyModelRenderStateResolver.resolve(
        contract,
        EasyModelServices.renderProfileService(),
        EasyModelServices.bakeService(),
        Minecraft.getInstance().getResourceManager());
  }

  public static void render(
      Entity entity,
      EasyModelRenderState renderState,
      float entityYaw,
      float partialTick,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight) {
    Objects.requireNonNull(entity, "entity");
    Objects.requireNonNull(renderState, "renderState");
    Objects.requireNonNull(poseStack, "poseStack");
    Objects.requireNonNull(bufferSource, "bufferSource");

    poseStack.pushPose();
    poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - entityYaw));
    poseStack.scale(-renderState.scale(), -renderState.scale(), renderState.scale());
    poseStack.translate(0.0f, -1.501f, 0.0f);

    VertexConsumer vertexConsumer =
        bufferSource.getBuffer(RenderType.entityCutoutNoCull(renderState.texture()));
    EasyModelBakedModelRenderer.render(
        renderState.bakedModel(),
        renderState,
        limbSwing(entity, partialTick),
        limbSwingAmount(entity, partialTick),
        entity.tickCount + partialTick,
        poseStack,
        vertexConsumer,
        packedLight);
    poseStack.popPose();
  }

  public static EasyModelRuntimeContract runtimeContract(
      Entity entity,
      EasyModelRenderable renderable,
      EasyModelProfileService profileService,
      EasyModelRenderProfileService renderProfileService) {
    Objects.requireNonNull(entity, "entity");
    Objects.requireNonNull(renderable, "renderable");
    Objects.requireNonNull(profileService, "profileService");
    Objects.requireNonNull(renderProfileService, "renderProfileService");

    ResourceLocation profileId =
        Objects.requireNonNull(renderable.getEasyModelProfileId(), "profileId");
    EasyModelAnimationState animationState =
        EasyModelAnimationState.byApiState(renderable.getEasyModelAnimationState());
    return profileService
        .getProfile(profileId)
        .filter(EasyModelEntityProfile::isActive)
        .filter(profile -> profile.modelType() == ModelType.ENTITY)
        .map(profile -> EasyModelRuntimeContract.fromProfile(profile, animationState))
        .orElseGet(
            () ->
                fallbackRuntimeContract(
                    entity, renderable, renderProfileService, profileId, animationState));
  }

  private static EasyModelRuntimeContract fallbackRuntimeContract(
      Entity entity,
      EasyModelRenderable renderable,
      EasyModelRenderProfileService renderProfileService,
      ResourceLocation profileId,
      EasyModelAnimationState animationState) {
    ResourceLocation renderProfileId =
        Objects.requireNonNullElse(renderable.getEasyModelRenderProfileId(), profileId);
    ModelBodyType bodyType =
        renderProfileService
            .getRenderProfile(renderProfileId)
            .filter(EasyModelRenderProfile::isActive)
            .map(EasyModelRenderProfile::bodyType)
            .orElse(ModelBodyType.STATIC);
    String renderableVersion = Objects.requireNonNullElse(renderable.getEasyModelVersion(), "");
    String version = renderableVersion.isBlank() ? "" : renderableVersion;
    return new EasyModelRuntimeContract(
        profileId,
        renderProfileId,
        version,
        width(entity),
        height(entity),
        eyeHeight(entity),
        bodyType,
        animationState);
  }

  private static float limbSwing(Entity entity, float partialTick) {
    return entity instanceof LivingEntity livingEntity
        ? livingEntity.walkAnimation.position(partialTick)
        : 0.0f;
  }

  private static float limbSwingAmount(Entity entity, float partialTick) {
    return entity instanceof LivingEntity livingEntity
        ? Math.min(livingEntity.walkAnimation.speed(partialTick), 1.0f)
        : 0.0f;
  }

  private static float width(Entity entity) {
    return entity.getBbWidth() > 0.0f ? entity.getBbWidth() : EasyModelHostEntity.FALLBACK_WIDTH;
  }

  private static float height(Entity entity) {
    return entity.getBbHeight() > 0.0f ? entity.getBbHeight() : EasyModelHostEntity.FALLBACK_HEIGHT;
  }

  private static float eyeHeight(Entity entity) {
    return entity.getEyeHeight() > 0.0f
        ? entity.getEyeHeight()
        : EasyModelHostEntity.FALLBACK_EYE_HEIGHT;
  }
}
