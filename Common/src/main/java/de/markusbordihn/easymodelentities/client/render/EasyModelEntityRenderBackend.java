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
import de.markusbordihn.easymodelentities.api.data.client.EasyModelEntityRenderOptions;
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
import de.markusbordihn.easymodelentities.runtime.EasyModelAnimationState;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class EasyModelEntityRenderBackend {

  private static final float AIRBORNE_BASE = 0.35f;
  private static final float AIRBORNE_MOTION_RANGE = 0.4f;

  private EasyModelEntityRenderBackend() {}

  public static EasyModelRenderState resolveRenderState(EasyModelRuntimeContract contract) {
    return EasyModelRenderStateCache.resolve(contract);
  }

  public static Optional<EasyModelRuntimeContract> resolveContract(
      Identifier profileId, EasyModelAnimationState animationState) {
    Objects.requireNonNull(profileId, "profileId");
    Objects.requireNonNull(animationState, "animationState");
    Optional<EasyModelRuntimeContract> contract =
        EasyModelServices.profileService()
            .getProfile(profileId)
            .filter(EasyModelEntityProfile::isActive)
            .map(profile -> EasyModelRuntimeContract.fromProfile(profile, animationState));
    if (contract.isPresent()) {
      return contract;
    }
    return EasyModelServices.renderProfileService()
        .getRenderProfile(profileId)
        .filter(EasyModelRenderProfile::isActive)
        .map(
            renderProfile ->
                EasyModelRuntimeContract.fromRenderProfile(
                    profileId, renderProfile, animationState));
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
    poseStack.pushPose();
    poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - renderState.entityYaw));
    poseStack.scale(
        -easyModelRenderState.scale(), -easyModelRenderState.scale(), easyModelRenderState.scale());
    poseStack.translate(0.0f, -1.501f, 0.0f);

    EasyModelBakedModelRenderer.render(
        easyModelRenderState.bakedModel(),
        easyModelRenderState,
        renderState.limbSwing,
        renderState.limbSwingAmount,
        renderState.ageInTicks,
        renderState.airborneAmount,
        renderState.attackAmount,
        renderState.animationState,
        renderState.partAnimator == null ? EasyModelPartAnimator.NONE : renderState.partAnimator,
        renderState.partAnimationMode == null
            ? EasyModelPartAnimationMode.ADD
            : renderState.partAnimationMode,
        poseStack,
        submitNodeCollector,
        packedLight);
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

    poseStack.pushPose();
    poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - renderState.entityYaw));
    poseStack.scale(
        -easyModelRenderState.scale(), -easyModelRenderState.scale(), easyModelRenderState.scale());
    poseStack.translate(0.0f, -1.501f, 0.0f);
    EasyModelBakedModelRenderer.render(
        easyModelRenderState.bakedModel(),
        easyModelRenderState,
        renderState.limbSwing,
        renderState.limbSwingAmount,
        renderState.ageInTicks,
        renderState.airborneAmount,
        renderState.attackAmount,
        renderState.animationState,
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
      MultiBufferSource bufferSource,
      int packedLight) {
    render(
        entity,
        renderState,
        entityYaw,
        partialTick,
        EasyModelEntityRenderOptions.DEFAULT,
        poseStack,
        bufferSource,
        packedLight);
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
    Objects.requireNonNull(entity, "entity");
    Objects.requireNonNull(renderState, "renderState");
    Objects.requireNonNull(poseStack, "poseStack");
    Objects.requireNonNull(bufferSource, "bufferSource");

    EasyModelEntityRenderOptions safeOptions =
        options == null ? EasyModelEntityRenderOptions.DEFAULT : options;
    float ageInTicks =
        safeOptions.animationTicks() == null
            ? entity.tickCount + partialTick
            : safeOptions.animationTicks();

    render(
        renderState,
        entityYaw,
        limbSwing(entity, partialTick),
        limbSwingAmount(entity, partialTick),
        ageInTicks,
        airborneAmount(entity),
        attackAmount(entity, partialTick),
        resolveAnimationState(safeOptions, animationState(entity)),
        safeOptions,
        poseStack,
        bufferSource,
        packedLight);
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

    render(
        renderState,
        yaw,
        0.0f,
        0.0f,
        ageInTicks,
        0.0f,
        0.0f,
        resolveAnimationState(safeOptions, EasyModelAnimationState.AUTO),
        safeOptions,
        poseStack,
        bufferSource,
        packedLight);
  }

  private static void render(
      EasyModelRenderState renderState,
      float yaw,
      float limbSwing,
      float limbSwingAmount,
      float ageInTicks,
      float airborneAmount,
      float attackAmount,
      EasyModelAnimationState animationState,
      EasyModelEntityRenderOptions options,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight) {
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
        ageInTicks,
        airborneAmount,
        attackAmount,
        animationState,
        options.partAnimator(),
        options.partAnimationMode(),
        options.partPoseListener(),
        poseStack,
        bufferSource,
        packedLight);
    poseStack.popPose();
  }

  private static EasyModelAnimationState resolveAnimationState(
      EasyModelEntityRenderOptions options, EasyModelAnimationState fallback) {
    return options.animationState() == null
        ? fallback
        : EasyModelAnimationState.byApiState(options.animationState());
  }

  private static EasyModelAnimationState animationState(Entity entity) {
    if (entity instanceof EasyModelEntityHost hostEntity) {
      return hostEntity.getEasyModelAnimationState();
    }
    if (entity instanceof EasyModelRenderable renderable) {
      return EasyModelAnimationState.byApiState(renderable.getEasyModelAnimationState());
    }
    return EasyModelAnimationState.AUTO;
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
      Identifier profileId,
      EasyModelAnimationState animationState) {
    Identifier renderProfileId =
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
