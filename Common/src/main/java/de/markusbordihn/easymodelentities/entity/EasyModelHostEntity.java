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

package de.markusbordihn.easymodelentities.entity;

import de.markusbordihn.easymodelentities.Constants;
import de.markusbordihn.easymodelentities.network.syncher.EasyModelEntityDataSerializers;
import de.markusbordihn.easymodelentities.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.profile.ModelBehaviorMode;
import de.markusbordihn.easymodelentities.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.runtime.EasyModelAnimationState;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public abstract class EasyModelHostEntity extends PathfinderMob {

  public static final float FALLBACK_WIDTH = 0.6f;
  public static final float FALLBACK_HEIGHT = 1.8f;
  public static final float FALLBACK_EYE_HEIGHT = 1.62f;
  public static final ResourceLocation MISSING_PROFILE_ID =
      new ResourceLocation(Constants.MOD_ID, "missing");

  private static final String PROFILE_ID_TAG = "ProfileId";
  private static final String RENDER_PROFILE_ID_TAG = "RenderProfileId";
  private static final String ASSET_FINGERPRINT_TAG = "AssetFingerprint";
  private static final String BODY_TYPE_TAG = "BodyType";
  private static final String ANIMATION_STATE_TAG = "AnimationState";

  private static final EntityDataAccessor<String> PROFILE_ID =
      EasyModelEntityDataSerializers.defineId(
          EasyModelHostEntity.class, EasyModelEntityDataSerializers.STRING);
  private static final EntityDataAccessor<String> RENDER_PROFILE_ID =
      EasyModelEntityDataSerializers.defineId(
          EasyModelHostEntity.class, EasyModelEntityDataSerializers.STRING);
  private static final EntityDataAccessor<String> ASSET_FINGERPRINT =
      EasyModelEntityDataSerializers.defineId(
          EasyModelHostEntity.class, EasyModelEntityDataSerializers.STRING);
  private static final EntityDataAccessor<Float> WIDTH =
      EasyModelEntityDataSerializers.defineId(
          EasyModelHostEntity.class, EasyModelEntityDataSerializers.FLOAT);
  private static final EntityDataAccessor<Float> HEIGHT =
      EasyModelEntityDataSerializers.defineId(
          EasyModelHostEntity.class, EasyModelEntityDataSerializers.FLOAT);
  private static final EntityDataAccessor<Float> EYE_HEIGHT =
      EasyModelEntityDataSerializers.defineId(
          EasyModelHostEntity.class, EasyModelEntityDataSerializers.FLOAT);
  private static final EntityDataAccessor<ModelBodyType> BODY_TYPE =
      EasyModelEntityDataSerializers.defineId(
          EasyModelHostEntity.class, EasyModelEntityDataSerializers.BODY_TYPE);
  private static final EntityDataAccessor<EasyModelAnimationState> ANIMATION_STATE =
      EasyModelEntityDataSerializers.defineId(
          EasyModelHostEntity.class, EasyModelEntityDataSerializers.ANIMATION_STATE);

  protected EasyModelHostEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
    super(entityType, level);
    this.setPersistenceRequired();
  }

  public static AttributeSupplier.Builder createAttributes() {
    return Mob.createMobAttributes()
        .add(Attributes.MAX_HEALTH, 10.0)
        .add(Attributes.MOVEMENT_SPEED, 0.0)
        .add(Attributes.FOLLOW_RANGE, 16.0);
  }

  private static Optional<EasyModelEntityProfile> activeProfile(ResourceLocation profileId) {
    return EasyModelServices.profileService()
        .getProfile(profileId)
        .filter(EasyModelEntityProfile::isActive);
  }

  private static ResourceLocation parseResourceLocationOrMissing(String resourceLocation) {
    ResourceLocation parsedResourceLocation = parseResourceLocation(resourceLocation);
    return parsedResourceLocation == null ? MISSING_PROFILE_ID : parsedResourceLocation;
  }

  private static ResourceLocation parseResourceLocation(String resourceLocation) {
    return resourceLocation == null || resourceLocation.isBlank()
        ? null
        : ResourceLocation.tryParse(resourceLocation);
  }

  @Override
  protected void defineSynchedData() {
    super.defineSynchedData();
    EasyModelRuntimeContract contract = EasyModelRuntimeContract.fallback(MISSING_PROFILE_ID);
    this.entityData.define(PROFILE_ID, contract.profileId().toString());
    this.entityData.define(RENDER_PROFILE_ID, contract.renderProfileId().toString());
    this.entityData.define(ASSET_FINGERPRINT, contract.assetFingerprint());
    this.entityData.define(WIDTH, contract.width());
    this.entityData.define(HEIGHT, contract.height());
    this.entityData.define(EYE_HEIGHT, contract.eyeHeight());
    this.entityData.define(BODY_TYPE, contract.bodyType());
    this.entityData.define(ANIMATION_STATE, contract.animationState());
  }

  @Override
  public EntityDimensions getDimensions(Pose pose) {
    return EntityDimensions.scalable(this.entityData.get(WIDTH), this.entityData.get(HEIGHT));
  }

  @Override
  protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
    return this.entityData.get(EYE_HEIGHT);
  }

  @Override
  public void addAdditionalSaveData(CompoundTag compoundTag) {
    super.addAdditionalSaveData(compoundTag);
    compoundTag.putString(PROFILE_ID_TAG, this.entityData.get(PROFILE_ID));
    compoundTag.putString(RENDER_PROFILE_ID_TAG, this.entityData.get(RENDER_PROFILE_ID));
    compoundTag.putString(ASSET_FINGERPRINT_TAG, this.entityData.get(ASSET_FINGERPRINT));
    compoundTag.putString(BODY_TYPE_TAG, this.entityData.get(BODY_TYPE).getSerializedName());
    compoundTag.putString(
        ANIMATION_STATE_TAG, this.entityData.get(ANIMATION_STATE).getSerializedName());
  }

  @Override
  public void readAdditionalSaveData(CompoundTag compoundTag) {
    super.readAdditionalSaveData(compoundTag);

    ResourceLocation profileId = parseResourceLocation(compoundTag.getString(PROFILE_ID_TAG));
    ResourceLocation renderProfileId =
        parseResourceLocation(compoundTag.getString(RENDER_PROFILE_ID_TAG));
    String assetFingerprint = compoundTag.getString(ASSET_FINGERPRINT_TAG);
    ModelBodyType bodyType = ModelBodyType.bySerializedName(compoundTag.getString(BODY_TYPE_TAG));
    EasyModelAnimationState animationState =
        EasyModelAnimationState.bySerializedName(compoundTag.getString(ANIMATION_STATE_TAG));

    if (profileId == null) {
      applyRuntimeContract(EasyModelRuntimeContract.fallback(MISSING_PROFILE_ID, animationState));
      return;
    }

    Optional<EasyModelEntityProfile> profile = activeProfile(profileId);
    if (profile.isPresent()) {
      applyProfile(profile.get(), animationState);
      return;
    }

    applyRuntimeContract(
        new EasyModelRuntimeContract(
            profileId,
            renderProfileId == null ? profileId : renderProfileId,
            Objects.requireNonNullElse(assetFingerprint, ""),
            FALLBACK_WIDTH,
            FALLBACK_HEIGHT,
            FALLBACK_EYE_HEIGHT,
            bodyType,
            animationState));
  }

  public ResourceLocation getEasyModelProfileId() {
    return parseResourceLocationOrMissing(this.entityData.get(PROFILE_ID));
  }

  public void setEasyModelProfileId(ResourceLocation profileId) {
    Objects.requireNonNull(profileId, "profileId");
    Optional<EasyModelEntityProfile> profile = activeProfile(profileId);
    if (profile.isPresent()) {
      applyProfile(profile.get(), this.entityData.get(ANIMATION_STATE));
      return;
    }

    applyRuntimeContract(
        EasyModelRuntimeContract.fallback(profileId, this.entityData.get(ANIMATION_STATE)));
  }

  public ResourceLocation getEasyModelRenderProfileId() {
    return parseResourceLocationOrMissing(this.entityData.get(RENDER_PROFILE_ID));
  }

  public String getEasyModelAssetFingerprint() {
    return this.entityData.get(ASSET_FINGERPRINT);
  }

  public EasyModelAnimationState getEasyModelAnimationState() {
    return this.entityData.get(ANIMATION_STATE);
  }

  public void setEasyModelAnimationState(EasyModelAnimationState animationState) {
    this.entityData.set(ANIMATION_STATE, Objects.requireNonNull(animationState, "animationState"));
  }

  public EasyModelRuntimeContract getEasyModelRuntimeContract() {
    return new EasyModelRuntimeContract(
        getEasyModelProfileId(),
        getEasyModelRenderProfileId(),
        getEasyModelAssetFingerprint(),
        this.entityData.get(WIDTH),
        this.entityData.get(HEIGHT),
        this.entityData.get(EYE_HEIGHT),
        this.entityData.get(BODY_TYPE),
        getEasyModelAnimationState());
  }

  public boolean isEasyModelRandomStrollEnabled() {
    return shouldRandomStroll();
  }

  protected boolean shouldLookAtPlayers() {
    return activeProfile(getEasyModelProfileId())
        .map(profile -> profile.behavior().lookAtPlayers())
        .orElse(false);
  }

  protected boolean shouldRandomStroll() {
    return activeProfile(getEasyModelProfileId())
        .map(
            profile ->
                profile.behavior().mode() == ModelBehaviorMode.AMBIENT
                    && profile.behavior().randomStroll())
        .orElse(false);
  }

  private void applyProfile(
      EasyModelEntityProfile profile, EasyModelAnimationState animationState) {
    applyRuntimeContract(EasyModelRuntimeContract.fromProfile(profile, animationState));
    this.setNoGravity(!profile.movement().gravity());
    this.setMaxUpStep(profile.movement().stepHeight());

    if (this.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
      this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(profile.movement().speed());
    }
  }

  private void applyRuntimeContract(EasyModelRuntimeContract contract) {
    this.entityData.set(PROFILE_ID, contract.profileId().toString());
    this.entityData.set(RENDER_PROFILE_ID, contract.renderProfileId().toString());
    this.entityData.set(ASSET_FINGERPRINT, contract.assetFingerprint());
    this.entityData.set(WIDTH, contract.width());
    this.entityData.set(HEIGHT, contract.height());
    this.entityData.set(EYE_HEIGHT, contract.eyeHeight());
    this.entityData.set(BODY_TYPE, contract.bodyType());
    this.entityData.set(ANIMATION_STATE, contract.animationState());
    this.refreshDimensions();
  }
}
