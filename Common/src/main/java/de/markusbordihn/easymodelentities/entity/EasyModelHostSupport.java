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

import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationSetting;
import de.markusbordihn.easymodelentities.api.data.EasyModelDisplaySettings;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSetting;
import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.profile.ModelBehaviorMode;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.profile.ModelType;
import de.markusbordihn.easymodelentities.event.EasyModelReloadDispatcher;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.registry.ModelResourcePaths;
import de.markusbordihn.easymodelentities.runtime.EasyModelHostPersistence;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.WeakHashMap;
import net.minecraft.core.Holder;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class EasyModelHostSupport {

  public static final float FALLBACK_WIDTH = 0.6f;
  public static final float FALLBACK_HEIGHT = 1.8f;
  public static final float FALLBACK_EYE_HEIGHT = 1.62f;
  public static final Identifier MISSING_PROFILE_ID = ModelResourcePaths.modIdentifier("missing");
  private static final Map<Mob, EasyModelHostFields> LOADED_HOSTS =
      Collections.synchronizedMap(new WeakHashMap<>());

  static {
    EasyModelReloadDispatcher.addProfileReloadListener(EasyModelHostSupport::reloadProfiles);
  }

  private EasyModelHostSupport() {}

  public static void registerHost(Mob entity, EasyModelHostFields fields) {
    LOADED_HOSTS.put(
        Objects.requireNonNull(entity, "entity"), Objects.requireNonNull(fields, "fields"));
  }

  private static void reloadProfiles() {
    List<Map.Entry<Mob, EasyModelHostFields>> hosts = new ArrayList<>();
    synchronized (LOADED_HOSTS) {
      LOADED_HOSTS.forEach((entity, fields) -> hosts.add(Map.entry(entity, fields)));
    }
    for (Map.Entry<Mob, EasyModelHostFields> entry : hosts) {
      Mob entity = entry.getKey();
      if (entity.level().isClientSide() || entity.isRemoved()) {
        continue;
      }
      Identifier profileId = getProfileId(entity.getEntityData(), entry.getValue());
      EasyModelAnimationSetting animation = getAnimation(entity.getEntityData(), entry.getValue());
      activeProfile(profileId)
          .ifPresentOrElse(
              profile -> applyProfile(entity, entry.getValue(), profile, animation),
              () -> applyFallback(entity, entry.getValue(), profileId, animation));
    }
  }

  public static AttributeSupplier.Builder createAttributes() {
    return Mob.createMobAttributes()
        .add(Attributes.MAX_HEALTH, 10.0)
        .add(Attributes.MOVEMENT_SPEED, 0.0)
        .add(Attributes.FOLLOW_RANGE, 16.0);
  }

  public static Optional<EasyModelEntityProfile> activeProfile(Identifier profileId) {
    return EasyModelServices.profileService()
        .getProfile(profileId)
        .filter(EasyModelEntityProfile::isActive)
        .filter(profile -> profile.modelType() == ModelType.ENTITY);
  }

  public static void defineSynchedData(
      SynchedEntityData.Builder builder, EasyModelHostFields fields) {
    EasyModelRuntimeContract contract = EasyModelRuntimeContract.fallback(MISSING_PROFILE_ID);
    builder.define(fields.profileId(), contract.profileId().toString());
    builder.define(fields.renderProfileId(), contract.renderProfileId().toString());
    builder.define(fields.version(), contract.version());
    builder.define(fields.width(), contract.width());
    builder.define(fields.height(), contract.height());
    builder.define(fields.eyeHeight(), contract.eyeHeight());
    builder.define(fields.bodyType(), contract.bodyType());
    builder.define(fields.animation(), contract.animation());
    builder.define(fields.lookAtPlayers(), false);
    builder.define(fields.randomStroll(), false);
    builder.define(fields.texture(), EasyModelTextureSetting.EMPTY);
    builder.define(fields.opacity(), EasyModelDisplaySettings.NO_OPACITY);
    builder.define(fields.lightLevel(), EasyModelDisplaySettings.NO_LIGHT_LEVEL);
  }

  public static EntityDimensions getDimensions(
      SynchedEntityData entityData, EasyModelHostFields fields) {
    return EntityDimensions.scalable(
            entityData.get(fields.width()), entityData.get(fields.height()))
        .withEyeHeight(entityData.get(fields.eyeHeight()));
  }

  public static void addAdditionalSaveData(
      ValueOutput valueOutput, SynchedEntityData entityData, EasyModelHostFields fields) {
    EasyModelHostPersistence.write(
        valueOutput,
        Identifier.tryParse(entityData.get(fields.profileId())),
        Identifier.tryParse(entityData.get(fields.renderProfileId())),
        entityData.get(fields.version()),
        entityData.get(fields.bodyType()),
        entityData.get(fields.animation()),
        entityData.get(fields.texture()),
        entityData.get(fields.opacity()),
        entityData.get(fields.lightLevel()));
  }

  public static void readAdditionalSaveData(
      Mob entity, ValueInput valueInput, EasyModelHostFields fields) {
    EasyModelHostPersistence.State state = EasyModelHostPersistence.read(valueInput);
    Identifier profileId = state.profileId();
    Identifier renderProfileId = state.renderProfileId();
    String version = state.version();
    ModelBodyType bodyType = state.bodyType();
    EasyModelAnimationSetting animation = state.animation();
    setTexture(entity.getEntityData(), fields, state.texture());
    setOpacity(entity.getEntityData(), fields, state.opacity());
    setLightLevel(entity.getEntityData(), fields, state.lightLevel());

    if (profileId == null) {
      applyFallback(entity, fields, MISSING_PROFILE_ID, animation);
      return;
    }

    Optional<EasyModelEntityProfile> profile = activeProfile(profileId);
    if (profile.isPresent()) {
      applyProfile(entity, fields, profile.get(), animation);
      return;
    }

    applyFallback(
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
            animation));
  }

  public static Identifier getProfileId(SynchedEntityData entityData, EasyModelHostFields fields) {
    return EasyModelHostPersistence.parseResourceLocationOrMissing(
        entityData.get(fields.profileId()), MISSING_PROFILE_ID);
  }

  public static void setProfileId(Mob entity, EasyModelHostFields fields, Identifier profileId) {
    Objects.requireNonNull(profileId, "profileId");
    EasyModelAnimationSetting animation = entity.getEntityData().get(fields.animation());
    Optional<EasyModelEntityProfile> profile = activeProfile(profileId);
    if (profile.isPresent()) {
      applyProfile(entity, fields, profile.get(), animation);
      entity.setHealth(entity.getMaxHealth());
      return;
    }

    applyFallback(entity, fields, profileId, animation);
  }

  private static void applyFallback(
      Mob entity,
      EasyModelHostFields fields,
      Identifier profileId,
      EasyModelAnimationSetting animation) {
    applyFallback(entity, fields, EasyModelRuntimeContract.fallback(profileId, animation));
  }

  private static void applyFallback(
      Mob entity, EasyModelHostFields fields, EasyModelRuntimeContract contract) {
    applyRuntimeContract(entity, fields, contract);
    entity.setNoGravity(false);
    setBaseAttribute(entity, Attributes.STEP_HEIGHT, 0.6);
    setBaseAttribute(entity, Attributes.MOVEMENT_SPEED, 0.0);
    setBaseAttribute(entity, Attributes.MAX_HEALTH, 10.0);
    setBaseAttribute(entity, Attributes.FOLLOW_RANGE, 16.0);
    entity.setHealth(Math.min(entity.getHealth(), entity.getMaxHealth()));
  }

  private static void setBaseAttribute(Mob entity, Holder<Attribute> attribute, double value) {
    if (entity.getAttribute(attribute) != null) {
      entity.getAttribute(attribute).setBaseValue(value);
    }
  }

  public static Identifier getRenderProfileId(
      SynchedEntityData entityData, EasyModelHostFields fields) {
    return EasyModelHostPersistence.parseResourceLocationOrMissing(
        entityData.get(fields.renderProfileId()), MISSING_PROFILE_ID);
  }

  public static String getVersion(SynchedEntityData entityData, EasyModelHostFields fields) {
    return entityData.get(fields.version());
  }

  public static EasyModelAnimationSetting getAnimation(
      SynchedEntityData entityData, EasyModelHostFields fields) {
    return entityData.get(fields.animation());
  }

  public static void setAnimation(
      SynchedEntityData entityData,
      EasyModelHostFields fields,
      EasyModelAnimationSetting animation) {
    entityData.set(fields.animation(), Objects.requireNonNull(animation, "animation"));
  }

  public static EasyModelTextureSetting getTexture(
      SynchedEntityData entityData, EasyModelHostFields fields) {
    return entityData.get(fields.texture());
  }

  public static void setTexture(
      SynchedEntityData entityData, EasyModelHostFields fields, EasyModelTextureSetting texture) {
    entityData.set(fields.texture(), Objects.requireNonNull(texture, "texture"));
  }

  public static float getOpacity(SynchedEntityData entityData, EasyModelHostFields fields) {
    return entityData.get(fields.opacity());
  }

  public static void setOpacity(
      SynchedEntityData entityData, EasyModelHostFields fields, float opacity) {
    entityData.set(fields.opacity(), EasyModelDisplaySettings.clampOpacityOverride(opacity));
  }

  public static int getLightLevel(SynchedEntityData entityData, EasyModelHostFields fields) {
    return entityData.get(fields.lightLevel());
  }

  public static void setLightLevel(
      SynchedEntityData entityData, EasyModelHostFields fields, int lightLevel) {
    entityData.set(fields.lightLevel(), EasyModelDisplaySettings.clampLightLevel(lightLevel));
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
        getAnimation(entityData, fields));
  }

  public static boolean shouldLookAtPlayers(
      SynchedEntityData entityData, EasyModelHostFields fields) {
    return entityData.get(fields.lookAtPlayers());
  }

  public static boolean shouldRandomStroll(
      SynchedEntityData entityData, EasyModelHostFields fields) {
    return entityData.get(fields.randomStroll());
  }

  public static void applyProfile(
      Mob entity,
      EasyModelHostFields fields,
      EasyModelEntityProfile profile,
      EasyModelAnimationSetting animation) {
    applyRuntimeContract(entity, fields, EasyModelRuntimeContract.fromProfile(profile, animation));
    SynchedEntityData entityData = entity.getEntityData();
    entityData.set(fields.lookAtPlayers(), profile.behavior().lookAtPlayers());
    entityData.set(
        fields.randomStroll(),
        profile.behavior().mode() == ModelBehaviorMode.AMBIENT
            && profile.behavior().randomStroll());
    entity.setNoGravity(!profile.movement().gravity());

    setBaseAttribute(entity, Attributes.MOVEMENT_SPEED, profile.movement().speed());
    setBaseAttribute(entity, Attributes.MAX_HEALTH, profile.attributes().maxHealth());
    setBaseAttribute(entity, Attributes.FOLLOW_RANGE, profile.attributes().followRange());
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
    entityData.set(fields.animation(), contract.animation());
    entityData.set(fields.lookAtPlayers(), false);
    entityData.set(fields.randomStroll(), false);
    entity.refreshDimensions();
  }
}
