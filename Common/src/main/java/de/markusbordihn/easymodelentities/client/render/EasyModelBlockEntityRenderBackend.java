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
import de.markusbordihn.easymodelentities.blockentity.EasyModelHostBlockEntity;
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
import net.minecraft.world.level.block.entity.BlockEntity;

public final class EasyModelBlockEntityRenderBackend {

  private EasyModelBlockEntityRenderBackend() {}

  public static EasyModelRenderState resolveRenderState(EasyModelRuntimeContract contract) {
    return EasyModelRenderStateResolver.resolve(
        contract,
        EasyModelServices.renderProfileService(),
        EasyModelServices.bakeService(),
        Minecraft.getInstance().getResourceManager());
  }

  public static void render(
      BlockEntity blockEntity,
      EasyModelRenderState renderState,
      float partialTick,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight) {
    Objects.requireNonNull(blockEntity, "blockEntity");
    Objects.requireNonNull(renderState, "renderState");
    Objects.requireNonNull(poseStack, "poseStack");
    Objects.requireNonNull(bufferSource, "bufferSource");

    float ageInTicks =
        blockEntity instanceof EasyModelHostBlockEntity hostBlockEntity
            ? hostBlockEntity.getEasyModelAnimationTicks() + partialTick
            : 0.0f;

    poseStack.pushPose();
    poseStack.translate(0.5f, 1.5f, 0.5f);
    poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
    poseStack.scale(-renderState.scale(), -renderState.scale(), renderState.scale());

    VertexConsumer vertexConsumer =
        bufferSource.getBuffer(RenderType.entityCutoutNoCull(renderState.texture()));
    EasyModelBakedModelRenderer.render(
        renderState.bakedModel(),
        renderState,
        0.0f,
        0.0f,
        ageInTicks,
        poseStack,
        vertexConsumer,
        packedLight);
    poseStack.popPose();
  }

  public static EasyModelRuntimeContract runtimeContract(
      EasyModelRenderable renderable,
      EasyModelProfileService profileService,
      EasyModelRenderProfileService renderProfileService) {
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
        .filter(profile -> profile.modelType() == ModelType.BLOCK_ENTITY)
        .map(profile -> EasyModelRuntimeContract.fromProfile(profile, animationState))
        .orElseGet(
            () ->
                fallbackRuntimeContract(
                    renderable, renderProfileService, profileId, animationState));
  }

  private static EasyModelRuntimeContract fallbackRuntimeContract(
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
        EasyModelHostBlockEntity.FALLBACK_WIDTH,
        EasyModelHostBlockEntity.FALLBACK_HEIGHT,
        EasyModelHostBlockEntity.FALLBACK_EYE_HEIGHT,
        bodyType,
        animationState);
  }
}
