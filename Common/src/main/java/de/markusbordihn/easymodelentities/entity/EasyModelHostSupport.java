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
import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.profile.ModelBehaviorMode;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.profile.ModelType;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.runtime.EasyModelAnimationState;
import de.markusbordihn.easymodelentities.runtime.EasyModelHostPersistence;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class EasyModelHostSupport {

  public static final float FALLBACK_WIDTH = 0.6f;
  public static final float FALLBACK_HEIGHT = 1.8f;
  public static final float FALLBACK_EYE_HEIGHT = 1.62f;
  public static final ResourceLocation MISSING_PROFILE_ID =
      new ResourceLocation(Constants.MOD_ID, "missing");

  private EasyModelHostSupport() {}

  public static AttributeSupplier.Builder createAttributes() {
    return Mob.createMobAttributes()
        .add(Attributes.MAX_HEALTH, 10.0)
        .add(Attributes.MOVEMENT_SPEED, 0.0)
        .add(Attributes.FOLLOW_RANGE, 16.0);
  }

  public static Optional<EasyModelEntityProfile> activeProfile(ResourceLocation profileId) {
    return EasyModelServices.profileService()
        .getProfile(profileId)
        .filter(EasyModelEntityProfile::isActive)
        .filter(profile -> profile.modelType() == ModelType.ENTITY);
  }

  public static void defineSynchedData(SynchedEntityData entityData, EasyModelHostFields fields) {
    EasyModelRuntimeContract contract = EasyModelRuntimeContract.fallback(MISSING_PROFILE_ID);
    entityData.define(fields.profileId(), contract.profileId().toString());
    entityData.define(fields.renderProfileId(), contract.renderProfileId().toString());
    entityData.define(fields.version(), contract.version());
    entityData.define(fields.width(), contract.width());
    entityData.define(fields.height(), contract.height());
    entityData.define(fields.eyeHeight(), contract.eyeHeight());
    entityData.define(fields.bodyType(), contract.bodyType());
    entityData.define(fields.animationState(), contract.animationState());
  }

  public static EntityDimensions getDimensions(
      SynchedEntityData entityData, EasyModelHostFields fields) {
    return EntityDimensions.scalable(
        entityData.get(fields.width()), entityData.get(fields.height()));
  }

  public static float getStandingEyeHeight(
      SynchedEntityData entityData, EasyModelHostFields fields) {
    return entityData.get(fields.eyeHeight());
  }

  public static void addAdditionalSaveData(
      CompoundTag compoundTag, SynchedEntityData entityData, EasyModelHostFields fields) {
    compoundTag.putString(
        EasyModelHostPersistence.PROFILE_ID_TAG, entityData.get(fields.profileId()));
    compoundTag.putString(
        EasyModelHostPersistence.RENDER_PROFILE_ID_TAG, entityData.get(fields.renderProfileId()));
    compoundTag.putString(EasyModelHostPersistence.VERSION_TAG, entityData.get(fields.version()));
    compoundTag.putString(
        EasyModelHostPersistence.BODY_TYPE_TAG,
        entityData.get(fields.bodyType()).getSerializedName());
    compoundTag.putString(
        EasyModelHostPersistence.ANIMATION_STATE_TAG,
        entityData.get(fields.animationState()).getSerializedName());
  }

  public static void readAdditionalSaveData(
      Mob entity, CompoundTag compoundTag, EasyModelHostFields fields) {
    EasyModelHostPersistence.State state = EasyModelHostPersistence.read(compoundTag);
    ResourceLocation profileId = state.profileId();
    ResourceLocation renderProfileId = state.renderProfileId();
    String version = state.version();
    ModelBodyType bodyType = state.bodyType();
    EasyModelAnimationState animationState = state.animationState();

    if (profileId == null) {
      applyRuntimeContract(
          entity, fields, EasyModelRuntimeContract.fallback(MISSING_PROFILE_ID, animationState));
      return;
    }

    Optional<EasyModelEntityProfile> profile = activeProfile(profileId);
    if (profile.isPresent()) {
      applyProfile(entity, fields, profile.get(), animationState);
      return;
    }

    applyRuntimeContract(
        entity,
        fields,
        new EasyModelRuntimeContract(
            profileId,
            renderProfileId == null ? profileId : renderProfileId,
            Objects.requireNonNullElse(version, ""),
            FALLBACK_WIDTH,
            FALLBACK_HEIGHT,
            FALLBACK_EYE_HEIGHT,
            bodyType,
            animationState));
  }

  public static ResourceLocation getProfileId(
      SynchedEntityData entityData, EasyModelHostFields fields) {
    return EasyModelHostPersistence.parseResourceLocationOrMissing(
        entityData.get(fields.profileId()), MISSING_PROFILE_ID);
  }

  public static void setProfileId(
      Mob entity, EasyModelHostFields fields, ResourceLocation profileId) {
    Objects.requireNonNull(profileId, "profileId");
    EasyModelAnimationState animationState = entity.getEntityData().get(fields.animationState());
    Optional<EasyModelEntityProfile> profile = activeProfile(profileId);
    if (profile.isPresent()) {
      applyProfile(entity, fields, profile.get(), animationState);
      entity.setHealth(entity.getMaxHealth());
      return;
    }

    applyRuntimeContract(
        entity, fields, EasyModelRuntimeContract.fallback(profileId, animationState));
  }

  public static ResourceLocation getRenderProfileId(
      SynchedEntityData entityData, EasyModelHostFields fields) {
    return EasyModelHostPersistence.parseResourceLocationOrMissing(
        entityData.get(fields.renderProfileId()), MISSING_PROFILE_ID);
  }

  public static String getVersion(SynchedEntityData entityData, EasyModelHostFields fields) {
    return entityData.get(fields.version());
  }

  public static EasyModelAnimationState getAnimationState(
      SynchedEntityData entityData, EasyModelHostFields fields) {
    return entityData.get(fields.animationState());
  }

  public static void setAnimationState(
      SynchedEntityData entityData,
      EasyModelHostFields fields,
      EasyModelAnimationState animationState) {
    entityData.set(
        fields.animationState(), Objects.requireNonNull(animationState, "animationState"));
  }

  public static EasyModelRuntimeContract getRuntimeContract(
      SynchedEntityData entityData, EasyModelHostFields fields) {
    return new EasyModelRuntimeContract(
        getProfileId(entityData, fields),
        getRenderProfileId(entityData, fields),
        getVersion(entityData, fields),
        entityData.get(fields.width()),
        entityData.get(fields.height()),
        entityData.get(fields.eyeHeight()),
        entityData.get(fields.bodyType()),
        getAnimationState(entityData, fields));
  }

  public static boolean shouldLookAtPlayers(
      SynchedEntityData entityData, EasyModelHostFields fields) {
    return activeProfile(getProfileId(entityData, fields))
        .map(profile -> profile.behavior().lookAtPlayers())
        .orElse(false);
  }

  public static boolean shouldRandomStroll(
      SynchedEntityData entityData, EasyModelHostFields fields) {
    return activeProfile(getProfileId(entityData, fields))
        .map(
            profile ->
                profile.behavior().mode() == ModelBehaviorMode.AMBIENT
                    && profile.behavior().randomStroll())
        .orElse(false);
  }

  public static void applyProfile(
      Mob entity,
      EasyModelHostFields fields,
      EasyModelEntityProfile profile,
      EasyModelAnimationState animationState) {
    applyRuntimeContract(
        entity, fields, EasyModelRuntimeContract.fromProfile(profile, animationState));
    entity.setNoGravity(!profile.movement().gravity());
    entity.setMaxUpStep(profile.movement().stepHeight());

    if (entity.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
      entity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(profile.movement().speed());
    }
    if (entity.getAttribute(Attributes.MAX_HEALTH) != null) {
      entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(profile.attributes().maxHealth());
    }
    if (entity.getAttribute(Attributes.FOLLOW_RANGE) != null) {
      entity.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(profile.attributes().followRange());
    }
  }

  public static void applyRuntimeContract(
      Mob entity, EasyModelHostFields fields, EasyModelRuntimeContract contract) {
    SynchedEntityData entityData = entity.getEntityData();
    entityData.set(fields.profileId(), contract.profileId().toString());
    entityData.set(fields.renderProfileId(), contract.renderProfileId().toString());
    entityData.set(fields.version(), contract.version());
    entityData.set(fields.width(), contract.width());
    entityData.set(fields.height(), contract.height());
    entityData.set(fields.eyeHeight(), contract.eyeHeight());
    entityData.set(fields.bodyType(), contract.bodyType());
    entityData.set(fields.animationState(), contract.animationState());
    entity.refreshDimensions();
  }
}
