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
import de.markusbordihn.easymodelentities.api.client.EasyModelPartPoseListener;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationSetting;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSetting;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationPlayback;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationTransition;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelEntityRenderOptions;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelHeadLook;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartAnimationMode;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartPose;
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
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
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
      Identifier profileId, EasyModelAnimationSetting animation) {
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

  public static void extractRenderState(
      Entity entity,
      EasyModelRuntimeContract contract,
      EasyModelEntityRenderState renderState,
      float partialTick) {
    extractRenderState(
        entity, contract, renderState, partialTick, EasyModelEntityRenderOptions.DEFAULT);
  }

  public static void extractRenderState(
      Entity entity,
      EasyModelRuntimeContract contract,
      EasyModelEntityRenderState renderState,
      float partialTick,
      EasyModelEntityRenderOptions options) {
    Objects.requireNonNull(entity, "entity");
    Objects.requireNonNull(contract, "contract");
    Objects.requireNonNull(renderState, "renderState");

    EasyModelEntityRenderOptions safeOptions =
        options == null ? EasyModelEntityRenderOptions.DEFAULT : options;
    float limbSwingAmount = limbSwingAmount(entity, partialTick);
    float airborneAmount = airborneAmount(entity);
    float attackAmount = attackAmount(entity, partialTick);

    renderState.easyModelRenderState = resolveRenderState(contract);
    renderState.entityYaw = bodyYaw(entity, partialTick);
    renderState.limbSwing = limbSwing(entity, partialTick);
    renderState.limbSwingAmount = limbSwingAmount;
    renderState.airborneAmount = airborneAmount;
    renderState.attackAmount = attackAmount;
    AnimationFrames animationFrames =
        animationFrames(
            entity,
            renderState.easyModelRenderState,
            partialTick,
            safeOptions,
            renderState.limbSwing,
            limbSwingAmount,
            airborneAmount,
            attackAmount);
    renderState.playbackFrame = animationFrames.playbackFrame();
    renderState.variantFrame = animationFrames.variantFrame();
    renderState.textureSetting =
        resolveTextureSetting(safeOptions.textureSetting(), textureSetting(entity));
    renderState.headLook = headLook(entity, renderState.entityYaw, partialTick, safeOptions);
    renderState.partAnimator = safeOptions.partAnimator();
    renderState.partAnimationMode = safeOptions.partAnimationMode();
    renderState.scaleFactor = safeOptions.scale() == null ? 1.0f : safeOptions.scale();
    renderState.packedOverlay = packedOverlay(entity);
  }

  public static int packedOverlay(Entity entity) {
    if (!(entity instanceof LivingEntity livingEntity)) {
      return OverlayTexture.NO_OVERLAY;
    }

    return OverlayTexture.pack(
        OverlayTexture.u(0.0f),
        OverlayTexture.v(livingEntity.hurtTime > 0 || livingEntity.deathTime > 0));
  }

  public static void render(
      EasyModelEntityRenderState renderState,
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
    poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - renderState.entityYaw));
    poseStack.scale(-scale, -scale, scale);
    poseStack.translate(0.0f, -1.501f, 0.0f);

    EasyModelBakedModelRenderer.render(
        easyModelRenderState.bakedModel(),
        easyModelRenderState,
        renderState.limbSwing,
        renderState.limbSwingAmount,
        renderState.airborneAmount,
        renderState.attackAmount,
        renderState.headLook == null ? EasyModelHeadLook.NONE : renderState.headLook,
        renderState.playbackFrame,
        renderState.variantFrame,
        renderState.textureSetting,
        renderState.partAnimator == null ? EasyModelPartAnimator.NONE : renderState.partAnimator,
        renderState.partAnimationMode == null
            ? EasyModelPartAnimationMode.ADD
            : renderState.partAnimationMode,
        poseStack,
        submitNodeCollector,
        packedLight,
        renderState.packedOverlay);
    poseStack.popPose();
  }

  /**
   * Resolves the world-space pose of a named model part without submitting any geometry. Mirrors
   * the transform chain applied by the submit-based {@link #render(EasyModelEntityRenderState,
   * PoseStack, SubmitNodeCollector, int)} so callers can, for example, anchor held items to a hand
   * part. The returned pose is relative to the supplied {@code poseStack} state.
   */
  public static Optional<EasyModelPartPose> resolvePartPose(
      EasyModelEntityRenderState renderState, String partName, PoseStack poseStack) {
    if (renderState == null || renderState.easyModelRenderState == null || partName == null) {
      return Optional.empty();
    }
    Objects.requireNonNull(poseStack, "poseStack");

    EasyModelRenderState easyModelRenderState = renderState.easyModelRenderState;
    CapturingPartPoseListener listener = new CapturingPartPoseListener(partName);
    float scale = easyModelRenderState.scale() * renderState.scaleFactor;

    poseStack.pushPose();
    poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - renderState.entityYaw));
    poseStack.scale(-scale, -scale, scale);
    poseStack.translate(0.0f, -1.501f, 0.0f);
    EasyModelBakedModelRenderer.render(
        easyModelRenderState.bakedModel(),
        easyModelRenderState,
        renderState.limbSwing,
        renderState.limbSwingAmount,
        renderState.airborneAmount,
        renderState.attackAmount,
        renderState.headLook == null ? EasyModelHeadLook.NONE : renderState.headLook,
        renderState.playbackFrame,
        poseStack,
        textureIndex -> null,
        0,
        renderState.partAnimator == null ? EasyModelPartAnimator.NONE : renderState.partAnimator,
        renderState.partAnimationMode == null
            ? EasyModelPartAnimationMode.ADD
            : renderState.partAnimationMode,
        listener);
    poseStack.popPose();
    return listener.capturedPose();
  }

  public static float airborneAmount(Entity entity) {
    if (!(entity instanceof LivingEntity livingEntity) || livingEntity.onGround()) {
      return 0.0f;
    }
    float verticalMotion = (float) Math.abs(livingEntity.getDeltaMovement().y);
    float motionFactor = Mth.clamp(verticalMotion / AIRBORNE_MOTION_RANGE, 0.0f, 1.0f);
    return AIRBORNE_BASE + (1.0f - AIRBORNE_BASE) * motionFactor;
  }

  public static void render(
      Entity entity,
      EasyModelRenderState renderState,
      float entityYaw,
      float partialTick,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      int packedLight) {
    render(
        entity,
        renderState,
        entityYaw,
        partialTick,
        poseStack,
        submitNodeCollector,
        packedLight,
        OverlayTexture.NO_OVERLAY);
  }

  public static void render(
      Entity entity,
      EasyModelRenderState renderState,
      float entityYaw,
      float partialTick,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      int packedLight,
      int packedOverlay) {
    render(
        entity,
        renderState,
        entityYaw,
        partialTick,
        EasyModelEntityRenderOptions.DEFAULT,
        poseStack,
        submitNodeCollector,
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
      SubmitNodeCollector submitNodeCollector,
      int packedLight) {
    render(
        entity,
        renderState,
        entityYaw,
        partialTick,
        options,
        poseStack,
        submitNodeCollector,
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
      SubmitNodeCollector submitNodeCollector,
      int packedLight,
      int packedOverlay) {
    Objects.requireNonNull(entity, "entity");
    Objects.requireNonNull(renderState, "renderState");
    Objects.requireNonNull(poseStack, "poseStack");
    Objects.requireNonNull(submitNodeCollector, "submitNodeCollector");

    EasyModelEntityRenderOptions safeOptions =
        options == null ? EasyModelEntityRenderOptions.DEFAULT : options;
    float limbSwingAmount = limbSwingAmount(entity, partialTick);
    float airborneAmount = airborneAmount(entity);
    float attackAmount = attackAmount(entity, partialTick);
    float limbSwing = limbSwing(entity, partialTick);
    AnimationFrames animationFrames =
        animationFrames(
            entity,
            renderState,
            partialTick,
            safeOptions,
            limbSwing,
            limbSwingAmount,
            airborneAmount,
            attackAmount);

    render(
        renderState,
        entityYaw,
        limbSwing,
        limbSwingAmount,
        animationFrames.playbackFrame(),
        animationFrames.variantFrame(),
        airborneAmount,
        attackAmount,
        headLook(entity, entityYaw, partialTick, safeOptions),
        safeOptions,
        resolveTextureSetting(safeOptions.textureSetting(), textureSetting(entity)),
        poseStack,
        submitNodeCollector,
        packedLight,
        packedOverlay);
  }

  private static AnimationFrames animationFrames(
      Entity entity,
      EasyModelRenderState renderState,
      float partialTick,
      EasyModelEntityRenderOptions options,
      float limbSwing,
      float limbSwingAmount,
      float airborneAmount,
      float attackAmount) {
    EasyModelAnimationSetting setting =
        resolveSetting(options.animation(), animationSetting(entity));
    if (options.animationTicks() != null) {
      ANIMATION_PLAYBACK_TRACKER.clear(entity);
      ANIMATION_VARIANT_TRACKER.clear(entity);
      return new AnimationFrames(
          EasyModelAnimationPlaybackFrame.single(
              setting.animation(), options.animationTicks(), setting.loopOverride(), false),
          EasyModelAnimationVariantFrame.NONE);
    }

    double animationClock = entity.tickCount + partialTick;
    Supplier<String> automaticClipName =
        new EasyModelAutomaticClipName(renderState, limbSwingAmount, airborneAmount, attackAmount);
    EasyModelAnimationPlaybackFrame playbackFrame =
        applySetting(
            ANIMATION_PLAYBACK_TRACKER.resolve(
                entity,
                setting.animation(),
                automaticClipName,
                animationClock,
                renderState.bakedModel().animations()),
            setting);
    return new AnimationFrames(
        playbackFrame,
        ANIMATION_VARIANT_TRACKER.resolve(
            entity,
            entity.getId(),
            renderState.bakedModel(),
            renderState.animation().variantMode(),
            playbackFrame,
            automaticClipName,
            animationClock,
            EasyModelBakedModelRenderer.walkCycles(renderState, limbSwing),
            attackAmount));
  }

  public static void render(
      EasyModelRenderState renderState,
      float yaw,
      EasyModelEntityRenderOptions options,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      int packedLight) {
    Objects.requireNonNull(renderState, "renderState");
    Objects.requireNonNull(poseStack, "poseStack");
    Objects.requireNonNull(submitNodeCollector, "submitNodeCollector");

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
        submitNodeCollector,
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
      SubmitNodeCollector submitNodeCollector,
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
        submitNodeCollector,
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
      SubmitNodeCollector submitNodeCollector,
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
        submitNodeCollector,
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

  public static EasyModelHeadLook headLook(Entity entity, float bodyYaw, float partialTick) {
    if (!(entity instanceof LivingEntity livingEntity)) {
      return EasyModelHeadLook.NONE;
    }

    return EasyModelHeadLook.of(
        Mth.rotLerp(partialTick, livingEntity.yHeadRotO, livingEntity.yHeadRot) - bodyYaw,
        Mth.lerp(partialTick, livingEntity.xRotO, livingEntity.getXRot()));
  }

  private static EasyModelHeadLook headLook(
      Entity entity, float bodyYaw, float partialTick, EasyModelEntityRenderOptions options) {
    if (options.headLook() != null) {
      return options.headLook();
    }

    return headLook(entity, bodyYaw, partialTick);
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

    Identifier profileId = Objects.requireNonNull(renderable.getEasyModelProfileId(), "profileId");
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
        width(entity),
        height(entity),
        eyeHeight(entity),
        bodyType,
        animation);
  }

  private static float bodyYaw(Entity entity, float partialTick) {
    return entity instanceof LivingEntity livingEntity
        ? Mth.rotLerp(partialTick, livingEntity.yBodyRotO, livingEntity.yBodyRot)
        : Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
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

  public static float attackAmount(Entity entity, float partialTick) {
    return entity instanceof LivingEntity livingEntity
        ? livingEntity.getAttackAnim(partialTick)
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

  private record AnimationFrames(
      EasyModelAnimationPlaybackFrame playbackFrame, EasyModelAnimationVariantFrame variantFrame) {}

  private static final class CapturingPartPoseListener implements EasyModelPartPoseListener {

    private final String partName;
    private EasyModelPartPose capturedPose;

    private CapturingPartPoseListener(String partName) {
      this.partName = partName;
    }

    @Override
    public boolean wantsPart(String candidatePartName) {
      return this.capturedPose == null && this.partName.equals(candidatePartName);
    }

    @Override
    public void onPartPose(EasyModelPartPose partPose) {
      if (this.capturedPose == null) {
        this.capturedPose = partPose;
      }
    }

    private Optional<EasyModelPartPose> capturedPose() {
      return Optional.ofNullable(this.capturedPose);
    }
  }
}
