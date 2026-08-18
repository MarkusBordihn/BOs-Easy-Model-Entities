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
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationSetting;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSetting;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationPlayback;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationTransition;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelEntityRenderOptions;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelHeadLook;
import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.profile.ModelType;
import de.markusbordihn.easymodelentities.data.render.EasyModelRenderState;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.entity.EasyModelEntityHost;
import de.markusbordihn.easymodelentities.entity.EasyModelHostEntity;
import de.markusbordihn.easymodelentities.profile.EasyModelProfileService;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.renderprofile.EasyModelRenderProfileService;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class EasyModelEntityRenderBackend {

  private static final float AIRBORNE_BASE = 0.35f;
  private static final float AIRBORNE_MOTION_RANGE = 0.4f;
  private static final EasyModelAnimationPlaybackTracker<Entity> ANIMATION_PLAYBACK_TRACKER =
      new EasyModelAnimationPlaybackTracker<>();
  private static final EasyModelAnimationVariantTracker<Entity> ANIMATION_VARIANT_TRACKER =
      new EasyModelAnimationVariantTracker<>();

  private EasyModelEntityRenderBackend() {}

  static void clearAnimationVariants() {
    ANIMATION_VARIANT_TRACKER.clearAll();
  }

  public static EasyModelRenderState resolveRenderState(EasyModelRuntimeContract contract) {
    return EasyModelRenderStateCache.resolve(contract);
  }

  public static Optional<EasyModelRuntimeContract> resolveContract(
      ResourceLocation profileId, EasyModelAnimationSetting animation) {
    Objects.requireNonNull(profileId, "profileId");
    Objects.requireNonNull(animation, "animationState");
    Optional<EasyModelRuntimeContract> contract =
        EasyModelServices.profileService()
            .getProfile(profileId)
            .filter(EasyModelEntityProfile::isActive)
            .map(profile -> EasyModelRuntimeContract.fromProfile(profile, animation));
    if (contract.isPresent()) {
      return contract;
    }
    return EasyModelServices.renderProfileService()
        .getRenderProfile(profileId)
        .filter(EasyModelRenderProfile::canResolveRenderState)
        .map(
            renderProfile ->
                EasyModelRuntimeContract.fromRenderProfile(profileId, renderProfile, animation));
  }

  public static void render(
      Entity entity,
      EasyModelRenderState renderState,
      float entityYaw,
      float partialTick,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight) {
    render(
        entity,
        renderState,
        entityYaw,
        partialTick,
        poseStack,
        bufferSource,
        packedLight,
        OverlayTexture.NO_OVERLAY);
  }

  public static void render(
      Entity entity,
      EasyModelRenderState renderState,
      float entityYaw,
      float partialTick,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      int packedOverlay) {
    render(
        entity,
        renderState,
        entityYaw,
        partialTick,
        EasyModelEntityRenderOptions.DEFAULT,
        poseStack,
        bufferSource,
        packedLight,
        packedOverlay);
  }

  public static void render(
      Entity entity,
      EasyModelRenderState renderState,
      float entityYaw,
      float partialTick,
      EasyModelEntityRenderOptions options,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight) {
    render(
        entity,
        renderState,
        entityYaw,
        partialTick,
        options,
        poseStack,
        bufferSource,
        packedLight,
        OverlayTexture.NO_OVERLAY);
  }

  public static void render(
      Entity entity,
      EasyModelRenderState renderState,
      float entityYaw,
      float partialTick,
      EasyModelEntityRenderOptions options,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      int packedOverlay) {
    Objects.requireNonNull(entity, "entity");
    Objects.requireNonNull(renderState, "renderState");
    Objects.requireNonNull(poseStack, "poseStack");
    Objects.requireNonNull(bufferSource, "bufferSource");

    EasyModelEntityRenderOptions safeOptions =
        options == null ? EasyModelEntityRenderOptions.DEFAULT : options;
    EasyModelAnimationSetting setting =
        resolveSetting(safeOptions.animation(), animationSetting(entity));
    float limbSwing = limbSwing(entity, partialTick);
    float limbSwingAmount = limbSwingAmount(entity, partialTick);
    float airborneAmount = airborneAmount(entity);
    float attackAmount = attackAmount(entity, partialTick);
    EasyModelAnimationPlaybackFrame playbackFrame;
    EasyModelAnimationVariantFrame variantFrame = EasyModelAnimationVariantFrame.NONE;
    if (safeOptions.animationTicks() != null) {
      ANIMATION_PLAYBACK_TRACKER.clear(entity);
      ANIMATION_VARIANT_TRACKER.clear(entity);
      playbackFrame =
          EasyModelAnimationPlaybackFrame.single(
              setting.animation(), safeOptions.animationTicks(), setting.loopOverride(), false);
    } else {
      double animationClock = entity.tickCount + partialTick;
      Supplier<String> automaticClipName =
          new EasyModelAutomaticClipName(
              renderState, limbSwingAmount, airborneAmount, attackAmount);
      playbackFrame =
          applySetting(
              ANIMATION_PLAYBACK_TRACKER.resolve(
                  entity,
                  setting.animation(),
                  automaticClipName,
                  animationClock,
                  renderState.bakedModel().animations()),
              setting);
      variantFrame =
          ANIMATION_VARIANT_TRACKER.resolve(
              entity,
              entity.getId(),
              renderState.bakedModel(),
              renderState.animation().variantMode(),
              playbackFrame,
              automaticClipName,
              animationClock,
              EasyModelBakedModelRenderer.walkCycles(renderState, limbSwing),
              attackAmount);
    }

    render(
        renderState,
        entityYaw,
        limbSwing,
        limbSwingAmount,
        playbackFrame,
        variantFrame,
        airborneAmount,
        attackAmount,
        headLook(entity, entityYaw, partialTick, safeOptions),
        safeOptions,
        resolveTextureSetting(safeOptions.textureSetting(), textureSetting(entity)),
        poseStack,
        bufferSource,
        packedLight,
        packedOverlay);
  }

  public static void render(
      EasyModelRenderState renderState,
      float yaw,
      EasyModelEntityRenderOptions options,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight) {
    Objects.requireNonNull(renderState, "renderState");
    Objects.requireNonNull(poseStack, "poseStack");
    Objects.requireNonNull(bufferSource, "bufferSource");

    EasyModelEntityRenderOptions safeOptions =
        options == null ? EasyModelEntityRenderOptions.DEFAULT : options;
    float ageInTicks = safeOptions.animationTicks() == null ? 0.0f : safeOptions.animationTicks();
    EasyModelAnimationSetting setting =
        resolveSetting(safeOptions.animation(), EasyModelAnimationSetting.AUTO);

    render(
        renderState,
        yaw,
        0.0f,
        0.0f,
        EasyModelAnimationPlaybackFrame.single(
            setting.animation(), ageInTicks, setting.loopOverride(), false),
        0.0f,
        0.0f,
        safeOptions.headLook() == null ? EasyModelHeadLook.NONE : safeOptions.headLook(),
        safeOptions,
        safeOptions.textureSetting(),
        poseStack,
        bufferSource,
        packedLight,
        OverlayTexture.NO_OVERLAY);
  }

  private static void render(
      EasyModelRenderState renderState,
      float yaw,
      float limbSwing,
      float limbSwingAmount,
      EasyModelAnimationPlaybackFrame playbackFrame,
      float airborneAmount,
      float attackAmount,
      EasyModelHeadLook headLook,
      EasyModelEntityRenderOptions options,
      EasyModelTextureSetting textureSetting,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      int packedOverlay) {
    render(
        renderState,
        yaw,
        limbSwing,
        limbSwingAmount,
        playbackFrame,
        EasyModelAnimationVariantFrame.NONE,
        airborneAmount,
        attackAmount,
        headLook,
        options,
        textureSetting,
        poseStack,
        bufferSource,
        packedLight,
        packedOverlay);
  }

  private static void render(
      EasyModelRenderState renderState,
      float yaw,
      float limbSwing,
      float limbSwingAmount,
      EasyModelAnimationPlaybackFrame playbackFrame,
      EasyModelAnimationVariantFrame variantFrame,
      float airborneAmount,
      float attackAmount,
      EasyModelHeadLook headLook,
      EasyModelEntityRenderOptions options,
      EasyModelTextureSetting textureSetting,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      int packedOverlay) {
    float scale = renderState.scale() * (options.scale() == null ? 1.0f : options.scale());

    poseStack.pushPose();
    poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - yaw));
    poseStack.scale(-scale, -scale, scale);
    poseStack.translate(0.0f, -1.501f, 0.0f);

    EasyModelBakedModelRenderer.render(
        renderState.bakedModel(),
        renderState,
        limbSwing,
        limbSwingAmount,
        airborneAmount,
        attackAmount,
        headLook,
        playbackFrame,
        variantFrame,
        textureSetting,
        options.partAnimator(),
        options.partAnimationMode(),
        options.partPoseListener(),
        poseStack,
        bufferSource,
        packedLight,
        packedOverlay);
    poseStack.popPose();
  }

  public static void playAnimation(
      Entity entity, EasyModelAnimation animation, EasyModelAnimationTransition transition) {
    playAnimation(entity, animation, EasyModelAnimationPlayback.DEFAULT, transition);
  }

  public static void playAnimation(
      Entity entity,
      EasyModelAnimation animation,
      EasyModelAnimationPlayback playback,
      EasyModelAnimationTransition transition) {
    ANIMATION_PLAYBACK_TRACKER.play(entity, animation, playback, transition);
  }

  public static void restartAnimation(Entity entity) {
    ANIMATION_PLAYBACK_TRACKER.restart(entity);
  }

  public static void stopAnimation(Entity entity, EasyModelAnimationTransition transition) {
    ANIMATION_PLAYBACK_TRACKER.stop(entity, transition);
  }

  private static EasyModelHeadLook headLook(
      Entity entity, float bodyYaw, float partialTick, EasyModelEntityRenderOptions options) {
    if (options.headLook() != null) {
      return options.headLook();
    }
    if (!(entity instanceof LivingEntity livingEntity)) {
      return EasyModelHeadLook.NONE;
    }
    return EasyModelHeadLook.of(
        Mth.rotLerp(partialTick, livingEntity.yHeadRotO, livingEntity.yHeadRot) - bodyYaw,
        Mth.lerp(partialTick, livingEntity.xRotO, livingEntity.getXRot()));
  }

  private static EasyModelAnimationSetting resolveSetting(
      EasyModelAnimation requestedAnimation, EasyModelAnimationSetting fallback) {
    EasyModelAnimationSetting setting =
        fallback == null ? EasyModelAnimationSetting.AUTO : fallback;
    return requestedAnimation == null ? setting : setting.withAnimation(requestedAnimation);
  }

  private static EasyModelAnimationSetting animationSetting(Entity entity) {
    if (entity instanceof EasyModelEntityHost hostEntity) {
      return hostEntity.getEasyModelAnimationSetting();
    }
    if (entity instanceof EasyModelRenderable renderable) {
      return renderable.getEasyModelAnimationSetting();
    }

    return EasyModelAnimationSetting.AUTO;
  }

  private static EasyModelTextureSetting resolveTextureSetting(
      EasyModelTextureSetting requested, EasyModelTextureSetting fallback) {
    if (requested != null && !requested.isEmpty()) {
      return requested;
    }

    return fallback == null ? EasyModelTextureSetting.EMPTY : fallback;
  }

  private static EasyModelTextureSetting textureSetting(Entity entity) {
    if (entity instanceof EasyModelEntityHost hostEntity) {
      return hostEntity.getEasyModelTextureSetting();
    }
    if (entity instanceof EasyModelRenderable renderable) {
      return renderable.getEasyModelTextureSetting();
    }

    return EasyModelTextureSetting.EMPTY;
  }

  private static EasyModelAnimationPlaybackFrame applySetting(
      EasyModelAnimationPlaybackFrame playbackFrame, EasyModelAnimationSetting setting) {
    return playbackFrame.playbackDriven()
        ? playbackFrame
        : EasyModelAnimationPlaybackFrame.single(
            playbackFrame.animation(),
            playbackFrame.animationTicks(),
            setting.loopOverride(),
            false);
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
    EasyModelAnimationSetting animation = renderable.getEasyModelAnimationSetting();
    return profileService
        .getProfile(profileId)
        .filter(EasyModelEntityProfile::isActive)
        .filter(profile -> profile.modelType() == ModelType.ENTITY)
        .map(profile -> EasyModelRuntimeContract.fromProfile(profile, animation))
        .orElseGet(
            () ->
                fallbackRuntimeContract(
                    entity, renderable, renderProfileService, profileId, animation));
  }

  private static EasyModelRuntimeContract fallbackRuntimeContract(
      Entity entity,
      EasyModelRenderable renderable,
      EasyModelRenderProfileService renderProfileService,
      ResourceLocation profileId,
      EasyModelAnimationSetting animation) {
    ResourceLocation renderProfileId =
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
        width(entity),
        height(entity),
        eyeHeight(entity),
        bodyType,
        animation);
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

  private static float attackAmount(Entity entity, float partialTick) {
    return entity instanceof LivingEntity livingEntity
        ? livingEntity.getAttackAnim(partialTick)
        : 0.0f;
  }

  private static float airborneAmount(Entity entity) {
    if (!(entity instanceof LivingEntity livingEntity) || livingEntity.onGround()) {
      return 0.0f;
    }
    float verticalMotion = (float) Math.abs(livingEntity.getDeltaMovement().y);
    float motionFactor = Mth.clamp(verticalMotion / AIRBORNE_MOTION_RANGE, 0.0f, 1.0f);
    return AIRBORNE_BASE + (1.0f - AIRBORNE_BASE) * motionFactor;
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
