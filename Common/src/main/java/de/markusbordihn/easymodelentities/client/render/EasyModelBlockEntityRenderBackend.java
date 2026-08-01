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
import com.mojang.math.Axis;
import de.markusbordihn.easymodelentities.api.EasyModelRenderable;
import de.markusbordihn.easymodelentities.api.client.EasyModelPartAnimator;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelBlockEntityRenderOptions;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelHeadLook;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartAnimationMode;
import de.markusbordihn.easymodelentities.blockentity.EasyModelHostBlockEntity;
import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.profile.ModelType;
import de.markusbordihn.easymodelentities.data.render.EasyModelRenderState;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.profile.EasyModelProfileService;
import de.markusbordihn.easymodelentities.renderprofile.EasyModelRenderProfileService;
import de.markusbordihn.easymodelentities.runtime.EasyModelAnimationState;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.util.Objects;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class EasyModelBlockEntityRenderBackend {

  private EasyModelBlockEntityRenderBackend() {}

  public static EasyModelRenderState resolveRenderState(EasyModelRuntimeContract contract) {
    return EasyModelRenderStateCache.resolve(contract);
  }

  public static void render(
      BlockEntity blockEntity,
      EasyModelRenderState renderState,
      float partialTick,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight) {
    render(
        blockEntity,
        renderState,
        partialTick,
        EasyModelBlockEntityRenderOptions.DEFAULT,
        poseStack,
        bufferSource,
        packedLight);
  }

  public static void render(
      EasyModelRenderState renderState,
      float ageInTicks,
      float yawDegrees,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      int packedLight) {
    Objects.requireNonNull(renderState, "renderState");
    Objects.requireNonNull(poseStack, "poseStack");
    Objects.requireNonNull(submitNodeCollector, "submitNodeCollector");

    poseStack.pushPose();
    poseStack.translate(0.5f, 1.5f, 0.5f);
    poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - yawDegrees));
    poseStack.scale(-renderState.scale(), -renderState.scale(), renderState.scale());

    EasyModelBakedModelRenderer.render(
        renderState.bakedModel(),
        renderState,
        0.0f,
        0.0f,
        ageInTicks,
        0.0f,
        EasyModelPartAnimator.NONE,
        EasyModelPartAnimationMode.ADD,
        poseStack,
        submitNodeCollector,
        packedLight);
    poseStack.popPose();
  }

  public static void render(
      EasyModelRenderState renderState,
      float ageInTicks,
      float yawDegrees,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight) {
    Objects.requireNonNull(renderState, "renderState");
    Objects.requireNonNull(poseStack, "poseStack");
    Objects.requireNonNull(bufferSource, "bufferSource");

    poseStack.pushPose();
    poseStack.translate(0.5f, 1.5f, 0.5f);
    poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - yawDegrees));
    poseStack.scale(-renderState.scale(), -renderState.scale(), renderState.scale());

    EasyModelBakedModelRenderer.render(
        renderState.bakedModel(),
        renderState,
        0.0f,
        0.0f,
        ageInTicks,
        0.0f,
        EasyModelPartAnimator.NONE,
        EasyModelPartAnimationMode.ADD,
        poseStack,
        bufferSource,
        packedLight);
    poseStack.popPose();
  }

  public static void render(
      BlockEntity blockEntity,
      EasyModelRenderState renderState,
      float partialTick,
      EasyModelBlockEntityRenderOptions options,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight) {
    Objects.requireNonNull(blockEntity, "blockEntity");
    Objects.requireNonNull(renderState, "renderState");
    Objects.requireNonNull(poseStack, "poseStack");
    Objects.requireNonNull(bufferSource, "bufferSource");

    EasyModelBlockEntityRenderOptions safeOptions =
        options == null ? EasyModelBlockEntityRenderOptions.DEFAULT : options;
    float ageInTicks =
        safeOptions.animationTicks() != null
            ? safeOptions.animationTicks()
            : blockEntity instanceof EasyModelHostBlockEntity hostBlockEntity
                ? hostBlockEntity.getEasyModelAnimationTicks(partialTick)
                : 0.0f;
    float yawDegrees = safeOptions.yawDegrees() == null ? 0.0f : safeOptions.yawDegrees();
    float scale =
        safeOptions.scale() == null
            ? renderState.scale()
            : renderState.scale() * safeOptions.scale();

    poseStack.pushPose();
    poseStack.translate(0.5f, 1.5f, 0.5f);
    poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - yawDegrees));
    poseStack.scale(-scale, -scale, scale);

    EasyModelBakedModelRenderer.render(
        renderState.bakedModel(),
        renderState,
        0.0f,
        0.0f,
        ageInTicks,
        0.0f,
        0.0f,
        EasyModelHeadLook.NONE,
        animationState(blockEntity, safeOptions),
        safeOptions.partAnimator(),
        safeOptions.partAnimationMode(),
        safeOptions.partPoseListener(),
        poseStack,
        bufferSource,
        packedLight);
    poseStack.popPose();
  }

  private static EasyModelAnimationState animationState(
      BlockEntity blockEntity, EasyModelBlockEntityRenderOptions options) {
    if (options.animationState() != null) {
      return EasyModelAnimationState.byApiState(options.animationState());
    }

    return blockEntity instanceof EasyModelRenderable renderable
        ? EasyModelAnimationState.byApiState(renderable.getEasyModelAnimationState())
        : EasyModelAnimationState.AUTO;
  }

  public static EasyModelRuntimeContract runtimeContract(
      EasyModelRenderable renderable,
      EasyModelProfileService profileService,
      EasyModelRenderProfileService renderProfileService) {
    Objects.requireNonNull(renderable, "renderable");
    Objects.requireNonNull(profileService, "profileService");
    Objects.requireNonNull(renderProfileService, "renderProfileService");

    Identifier profileId = Objects.requireNonNull(renderable.getEasyModelProfileId(), "profileId");
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
      Identifier profileId,
      EasyModelAnimationState animationState) {
    Identifier renderProfileId =
        Objects.requireNonNullElse(renderable.getEasyModelRenderProfileId(), profileId);
    ModelBodyType bodyType =
        renderProfileService
            .getRenderProfile(renderProfileId)
            .filter(EasyModelRenderProfile::isRenderable)
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
