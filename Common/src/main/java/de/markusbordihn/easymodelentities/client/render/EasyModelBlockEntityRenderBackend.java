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
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationSetting;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSetting;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationPlayback;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationTransition;
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
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class EasyModelBlockEntityRenderBackend {

  private static final EasyModelAnimationPlaybackTracker<BlockEntity> ANIMATION_PLAYBACK_TRACKER =
      new EasyModelAnimationPlaybackTracker<>();
  private static final EasyModelAnimationVariantTracker<BlockEntity> ANIMATION_VARIANT_TRACKER =
      new EasyModelAnimationVariantTracker<>();

  private EasyModelBlockEntityRenderBackend() {}

  static void clearAnimationVariants() {
    ANIMATION_VARIANT_TRACKER.clearAll();
  }

  public static EasyModelRenderState resolveRenderState(EasyModelRuntimeContract contract) {
    return EasyModelRenderStateCache.resolve(contract);
  }

  public static void render(
      BlockEntity blockEntity,
      EasyModelRenderState renderState,
      float partialTick,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      int packedLight) {
    render(
        blockEntity,
        renderState,
        partialTick,
        poseStack,
        submitNodeCollector,
        packedLight,
        OverlayTexture.NO_OVERLAY);
  }

  public static void render(
      BlockEntity blockEntity,
      EasyModelRenderState renderState,
      float partialTick,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      int packedLight,
      int packedOverlay) {
    render(
        blockEntity,
        renderState,
        partialTick,
        EasyModelBlockEntityRenderOptions.DEFAULT,
        poseStack,
        submitNodeCollector,
        packedLight,
        packedOverlay);
  }

  public static void extractRenderState(
      BlockEntity blockEntity,
      EasyModelRuntimeContract contract,
      EasyModelBlockEntityRenderState renderState,
      float partialTick) {
    extractRenderState(
        blockEntity, contract, renderState, partialTick, EasyModelBlockEntityRenderOptions.DEFAULT);
  }

  public static void extractRenderState(
      BlockEntity blockEntity,
      EasyModelRuntimeContract contract,
      EasyModelBlockEntityRenderState renderState,
      float partialTick,
      EasyModelBlockEntityRenderOptions options) {
    Objects.requireNonNull(blockEntity, "blockEntity");
    Objects.requireNonNull(contract, "contract");
    Objects.requireNonNull(renderState, "renderState");

    EasyModelBlockEntityRenderOptions safeOptions =
        options == null ? EasyModelBlockEntityRenderOptions.DEFAULT : options;
    renderState.easyModelRenderState = resolveRenderState(contract);
    AnimationFrames animationFrames =
        animationFrames(blockEntity, renderState.easyModelRenderState, partialTick, safeOptions);
    renderState.playbackFrame = animationFrames.playbackFrame();
    renderState.variantFrame = animationFrames.variantFrame();
    renderState.textureSetting =
        resolveTextureSetting(safeOptions.textureSetting(), textureSetting(blockEntity));
    renderState.ageInTicks = renderState.playbackFrame.animationTicks();
    renderState.partAnimator = safeOptions.partAnimator();
    renderState.partAnimationMode = safeOptions.partAnimationMode();
    renderState.scaleFactor = safeOptions.scale() == null ? 1.0f : safeOptions.scale();
    renderState.yawDegrees = safeOptions.yawDegrees() == null ? 0.0f : safeOptions.yawDegrees();
  }

  public static void render(
      EasyModelBlockEntityRenderState renderState,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      int packedLight) {
    Objects.requireNonNull(renderState, "renderState");
    Objects.requireNonNull(renderState.easyModelRenderState, "easyModelRenderState");
    Objects.requireNonNull(poseStack, "poseStack");
    Objects.requireNonNull(submitNodeCollector, "submitNodeCollector");

    EasyModelRenderState easyModelRenderState = renderState.easyModelRenderState;
    float scale = easyModelRenderState.scale() * renderState.scaleFactor;

    poseStack.pushPose();
    poseStack.translate(0.5f, 1.5f, 0.5f);
    poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - renderState.yawDegrees));
    poseStack.scale(-scale, -scale, scale);

    EasyModelBakedModelRenderer.render(
        easyModelRenderState.bakedModel(),
        easyModelRenderState,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        EasyModelHeadLook.NONE,
        renderState.playbackFrame,
        renderState.variantFrame,
        renderState.textureSetting,
        renderState.partAnimator == null ? EasyModelPartAnimator.NONE : renderState.partAnimator,
        renderState.partAnimationMode == null
            ? EasyModelPartAnimationMode.ADD
            : renderState.partAnimationMode,
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
      BlockEntity blockEntity,
      EasyModelRenderState renderState,
      float partialTick,
      EasyModelBlockEntityRenderOptions options,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      int packedLight) {
    render(
        blockEntity,
        renderState,
        partialTick,
        options,
        poseStack,
        submitNodeCollector,
        packedLight,
        OverlayTexture.NO_OVERLAY);
  }

  public static void render(
      BlockEntity blockEntity,
      EasyModelRenderState renderState,
      float partialTick,
      EasyModelBlockEntityRenderOptions options,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      int packedLight,
      int packedOverlay) {
    Objects.requireNonNull(blockEntity, "blockEntity");
    Objects.requireNonNull(renderState, "renderState");
    Objects.requireNonNull(poseStack, "poseStack");
    Objects.requireNonNull(submitNodeCollector, "submitNodeCollector");

    EasyModelBlockEntityRenderOptions safeOptions =
        options == null ? EasyModelBlockEntityRenderOptions.DEFAULT : options;
    AnimationFrames animationFrames =
        animationFrames(blockEntity, renderState, partialTick, safeOptions);
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
        0.0f,
        0.0f,
        EasyModelHeadLook.NONE,
        animationFrames.playbackFrame(),
        animationFrames.variantFrame(),
        resolveTextureSetting(safeOptions.textureSetting(), textureSetting(blockEntity)),
        safeOptions.partAnimator(),
        safeOptions.partAnimationMode(),
        safeOptions.partPoseListener(),
        poseStack,
        submitNodeCollector,
        packedLight,
        packedOverlay);
    poseStack.popPose();
  }

  public static void playAnimation(
      BlockEntity blockEntity,
      EasyModelAnimation animation,
      EasyModelAnimationTransition transition) {
    playAnimation(blockEntity, animation, EasyModelAnimationPlayback.DEFAULT, transition);
  }

  public static void playAnimation(
      BlockEntity blockEntity,
      EasyModelAnimation animation,
      EasyModelAnimationPlayback playback,
      EasyModelAnimationTransition transition) {
    ANIMATION_PLAYBACK_TRACKER.play(blockEntity, animation, playback, transition);
  }

  public static void restartAnimation(BlockEntity blockEntity) {
    ANIMATION_PLAYBACK_TRACKER.restart(blockEntity);
  }

  public static void stopAnimation(
      BlockEntity blockEntity, EasyModelAnimationTransition transition) {
    ANIMATION_PLAYBACK_TRACKER.stop(blockEntity, transition);
  }

  private static AnimationFrames animationFrames(
      BlockEntity blockEntity,
      EasyModelRenderState renderState,
      float partialTick,
      EasyModelBlockEntityRenderOptions options) {
    EasyModelAnimationSetting setting =
        resolveSetting(options.animation(), animationSetting(blockEntity));
    if (options.animationTicks() != null) {
      ANIMATION_PLAYBACK_TRACKER.clear(blockEntity);
      ANIMATION_VARIANT_TRACKER.clear(blockEntity);
      return new AnimationFrames(
          EasyModelAnimationPlaybackFrame.single(
              setting.animation(), options.animationTicks(), setting.loopOverride(), false),
          EasyModelAnimationVariantFrame.NONE);
    }

    Supplier<String> automaticClipName =
        new EasyModelAutomaticClipName(renderState, 0.0f, 0.0f, 0.0f);
    EasyModelAnimationPlaybackFrame playbackFrame =
        applySetting(
            ANIMATION_PLAYBACK_TRACKER.resolve(
                blockEntity,
                setting.animation(),
                automaticClipName,
                animationClock(blockEntity, partialTick),
                renderState.bakedModel().animations()),
            setting,
            blockEntity,
            partialTick);
    return new AnimationFrames(
        playbackFrame,
        ANIMATION_VARIANT_TRACKER.resolve(
            blockEntity,
            Objects.hashCode(blockEntity.getBlockPos()),
            renderState.bakedModel(),
            renderState.animation().variantMode(),
            playbackFrame,
            automaticClipName,
            playbackFrame.animationTicks(),
            0.0f,
            0.0f));
  }

  private static EasyModelAnimationSetting resolveSetting(
      EasyModelAnimation requestedAnimation, EasyModelAnimationSetting fallback) {
    EasyModelAnimationSetting setting =
        fallback == null ? EasyModelAnimationSetting.AUTO : fallback;
    return requestedAnimation == null ? setting : setting.withAnimation(requestedAnimation);
  }

  private static EasyModelAnimationSetting animationSetting(BlockEntity blockEntity) {
    return blockEntity instanceof EasyModelRenderable renderable
        ? renderable.getEasyModelAnimationSetting()
        : EasyModelAnimationSetting.AUTO;
  }

  private static EasyModelTextureSetting resolveTextureSetting(
      EasyModelTextureSetting requested, EasyModelTextureSetting fallback) {
    if (requested != null && !requested.isEmpty()) {
      return requested;
    }

    return fallback == null ? EasyModelTextureSetting.EMPTY : fallback;
  }

  private static EasyModelTextureSetting textureSetting(BlockEntity blockEntity) {
    return blockEntity instanceof EasyModelRenderable renderable
        ? renderable.getEasyModelTextureSetting()
        : EasyModelTextureSetting.EMPTY;
  }

  private static EasyModelAnimationPlaybackFrame applySetting(
      EasyModelAnimationPlaybackFrame playbackFrame,
      EasyModelAnimationSetting setting,
      BlockEntity blockEntity,
      float partialTick) {
    if (playbackFrame.playbackDriven()) {
      return playbackFrame;
    }

    boolean hostClock =
        !playbackFrame.animation().isNamed() && blockEntity instanceof EasyModelHostBlockEntity;
    float animationTicks =
        hostClock
            ? ((EasyModelHostBlockEntity) blockEntity).getEasyModelAnimationTicks(partialTick)
            : playbackFrame.animationTicks();
    return EasyModelAnimationPlaybackFrame.single(
        playbackFrame.animation(), animationTicks, setting.loopOverride(), false);
  }

  private static double animationClock(BlockEntity blockEntity, float partialTick) {
    if (blockEntity.getLevel() != null) {
      return blockEntity.getLevel().getGameTime() + partialTick;
    }

    return blockEntity instanceof EasyModelHostBlockEntity hostBlockEntity
        ? hostBlockEntity.getEasyModelAnimationTicks(partialTick)
        : 0.0f;
  }

  public static EasyModelRuntimeContract runtimeContract(
      EasyModelRenderable renderable,
      EasyModelProfileService profileService,
      EasyModelRenderProfileService renderProfileService) {
    Objects.requireNonNull(renderable, "renderable");
    Objects.requireNonNull(profileService, "profileService");
    Objects.requireNonNull(renderProfileService, "renderProfileService");

    Identifier profileId = Objects.requireNonNull(renderable.getEasyModelProfileId(), "profileId");
    EasyModelAnimationSetting animation = renderable.getEasyModelAnimationSetting();
    return profileService
        .getProfile(profileId)
        .filter(EasyModelEntityProfile::isActive)
        .filter(profile -> profile.modelType() == ModelType.BLOCK_ENTITY)
        .map(profile -> EasyModelRuntimeContract.fromProfile(profile, animation))
        .orElseGet(
            () -> fallbackRuntimeContract(renderable, renderProfileService, profileId, animation));
  }

  private static EasyModelRuntimeContract fallbackRuntimeContract(
      EasyModelRenderable renderable,
      EasyModelRenderProfileService renderProfileService,
      Identifier profileId,
      EasyModelAnimationSetting animation) {
    Identifier renderProfileId =
        Objects.requireNonNullElse(renderable.getEasyModelRenderProfileId(), profileId);
    ModelBodyType bodyType =
        renderProfileService
            .getRenderProfile(renderProfileId)
            .filter(EasyModelRenderProfile::canResolveRenderState)
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
        animation);
  }

  private record AnimationFrames(
      EasyModelAnimationPlaybackFrame playbackFrame, EasyModelAnimationVariantFrame variantFrame) {}
}
