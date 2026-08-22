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
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSetting;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.network.syncher.EasyModelEntityDataSerializers;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public abstract class EasyModelWaterHostEntity extends WaterAnimal implements EasyModelEntityHost {

  private static final EntityDataAccessor<String> PROFILE_ID =
      SynchedEntityData.defineId(
          EasyModelWaterHostEntity.class, EasyModelEntityDataSerializers.STRING);
  private static final EntityDataAccessor<String> RENDER_PROFILE_ID =
      SynchedEntityData.defineId(
          EasyModelWaterHostEntity.class, EasyModelEntityDataSerializers.STRING);
  private static final EntityDataAccessor<String> VERSION =
      SynchedEntityData.defineId(
          EasyModelWaterHostEntity.class, EasyModelEntityDataSerializers.STRING);
  private static final EntityDataAccessor<Float> WIDTH =
      SynchedEntityData.defineId(
          EasyModelWaterHostEntity.class, EasyModelEntityDataSerializers.FLOAT);
  private static final EntityDataAccessor<Float> HEIGHT =
      SynchedEntityData.defineId(
          EasyModelWaterHostEntity.class, EasyModelEntityDataSerializers.FLOAT);
  private static final EntityDataAccessor<Float> EYE_HEIGHT =
      SynchedEntityData.defineId(
          EasyModelWaterHostEntity.class, EasyModelEntityDataSerializers.FLOAT);
  private static final EntityDataAccessor<ModelBodyType> BODY_TYPE =
      SynchedEntityData.defineId(
          EasyModelWaterHostEntity.class, EasyModelEntityDataSerializers.BODY_TYPE);
  private static final EntityDataAccessor<EasyModelAnimationSetting> ANIMATION_STATE =
      SynchedEntityData.defineId(
          EasyModelWaterHostEntity.class, EasyModelEntityDataSerializers.ANIMATION_SETTING);
  private static final EntityDataAccessor<Boolean> LOOK_AT_PLAYERS =
      SynchedEntityData.defineId(EasyModelWaterHostEntity.class, EntityDataSerializers.BOOLEAN);
  private static final EntityDataAccessor<Boolean> RANDOM_STROLL =
      SynchedEntityData.defineId(EasyModelWaterHostEntity.class, EntityDataSerializers.BOOLEAN);
  private static final EntityDataAccessor<EasyModelTextureSetting> TEXTURE =
      SynchedEntityData.defineId(
          EasyModelWaterHostEntity.class, EasyModelEntityDataSerializers.TEXTURE_SETTING);
  private static final EntityDataAccessor<Float> OPACITY =
      SynchedEntityData.defineId(
          EasyModelWaterHostEntity.class, EasyModelEntityDataSerializers.FLOAT);
  private static final EntityDataAccessor<Integer> LIGHT_LEVEL =
      SynchedEntityData.defineId(
          EasyModelWaterHostEntity.class, EasyModelEntityDataSerializers.INT);

  private static final EasyModelHostFields FIELDS =
      new EasyModelHostFields(
          PROFILE_ID,
          RENDER_PROFILE_ID,
          VERSION,
          WIDTH,
          HEIGHT,
          EYE_HEIGHT,
          BODY_TYPE,
          ANIMATION_STATE,
          LOOK_AT_PLAYERS,
          RANDOM_STROLL,
          TEXTURE,
          OPACITY,
          LIGHT_LEVEL);

  private EasyModelRuntimeContract runtimeContract;

  protected EasyModelWaterHostEntity(EntityType<? extends WaterAnimal> entityType, Level level) {
    super(entityType, level);
    EasyModelHostSupport.registerHost(this, FIELDS);
    this.setPersistenceRequired();
    this.moveControl = new EasyModelWaterMoveControl(this);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return EasyModelHostSupport.createAttributes();
  }

  @Override
  protected PathNavigation createNavigation(Level level) {
    return new WaterBoundPathNavigation(this, level);
  }

  @Override
  public void travel(Vec3 travelVector) {
    if (this.isEffectiveAi() && this.isInWater()) {
      this.moveRelative(0.01f, travelVector);
      this.move(MoverType.SELF, this.getDeltaMovement());
      this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
      if (this.getTarget() == null) {
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.005, 0.0));
      }
    } else {
      super.travel(travelVector);
    }
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder builder) {
    super.defineSynchedData(builder);
    EasyModelHostSupport.defineSynchedData(builder, FIELDS);
  }

  @Override
  public EntityDimensions getDefaultDimensions(Pose pose) {
    return EasyModelHostSupport.getDimensions(this.entityData, FIELDS);
  }

  @Override
  public void addAdditionalSaveData(ValueOutput output) {
    super.addAdditionalSaveData(output);
    EasyModelHostSupport.addAdditionalSaveData(output, this.entityData, FIELDS);
  }

  @Override
  public void readAdditionalSaveData(ValueInput input) {
    super.readAdditionalSaveData(input);
    EasyModelHostSupport.readAdditionalSaveData(this, input, FIELDS);
  }

  @Override
  public Identifier getEasyModelProfileId() {
    return getEasyModelRuntimeContract().profileId();
  }

  @Override
  public void setEasyModelProfileId(Identifier profileId) {
    EasyModelHostSupport.setProfileId(this, FIELDS, profileId);
  }

  @Override
  public Identifier getEasyModelRenderProfileId() {
    return getEasyModelRuntimeContract().renderProfileId();
  }

  @Override
  public String getEasyModelVersion() {
    return getEasyModelRuntimeContract().version();
  }

  @Override
  public EasyModelAnimationSetting getEasyModelAnimationSetting() {
    return getEasyModelRuntimeContract().animation();
  }

  @Override
  public void setEasyModelAnimation(EasyModelAnimationSetting animation) {
    EasyModelHostSupport.setAnimation(this.entityData, FIELDS, animation);
  }

  @Override
  public EasyModelTextureSetting getEasyModelTextureSetting() {
    return EasyModelHostSupport.getTexture(this.entityData, FIELDS);
  }

  @Override
  public void setEasyModelTexture(EasyModelTextureSetting texture) {
    EasyModelHostSupport.setTexture(this.entityData, FIELDS, texture);
  }

  @Override
  public float getEasyModelOpacity() {
    return EasyModelHostSupport.getOpacity(this.entityData, FIELDS);
  }

  @Override
  public void setEasyModelOpacity(float opacity) {
    EasyModelHostSupport.setOpacity(this.entityData, FIELDS, opacity);
  }

  @Override
  public int getEasyModelLightLevel() {
    return EasyModelHostSupport.getLightLevel(this.entityData, FIELDS);
  }

  @Override
  public void setEasyModelLightLevel(int lightLevel) {
    EasyModelHostSupport.setLightLevel(this.entityData, FIELDS, lightLevel);
  }

  @Override
  public EasyModelRuntimeContract getEasyModelRuntimeContract() {
    EasyModelRuntimeContract cachedContract = this.runtimeContract;
    if (cachedContract != null) {
      return cachedContract;
    }
    EasyModelRuntimeContract resolvedContract =
        EasyModelHostSupport.getRuntimeContract(this.entityData, FIELDS);
    this.runtimeContract = resolvedContract;
    return resolvedContract;
  }

  @Override
  public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
    super.onSyncedDataUpdated(entityDataAccessor);
    if (FIELDS.isRuntimeContractField(entityDataAccessor)) {
      this.runtimeContract = null;
    }
    if (FIELDS.isDimensionsField(entityDataAccessor)) {
      this.refreshDimensions();
    }
  }

  @Override
  public boolean isEasyModelRandomStrollEnabled() {
    return shouldRandomStroll();
  }

  protected boolean shouldLookAtPlayers() {
    return EasyModelHostSupport.shouldLookAtPlayers(this.entityData, FIELDS);
  }

  protected boolean shouldRandomStroll() {
    return EasyModelHostSupport.shouldRandomStroll(this.entityData, FIELDS);
  }

  private static class EasyModelWaterMoveControl extends MoveControl {

    private final EasyModelWaterHostEntity fish;

    EasyModelWaterMoveControl(EasyModelWaterHostEntity fish) {
      super(fish);
      this.fish = fish;
    }

    @Override
    public void tick() {
      if (this.fish.isEyeInFluid(FluidTags.WATER)) {
        this.fish.setDeltaMovement(this.fish.getDeltaMovement().add(0.0, 0.005, 0.0));
      }

      if (this.operation == MoveControl.Operation.MOVE_TO && !this.fish.getNavigation().isDone()) {
        float speed =
            (float) (this.speedModifier * this.fish.getAttributeValue(Attributes.MOVEMENT_SPEED));
        this.fish.setSpeed(Mth.lerp(0.125f, this.fish.getSpeed(), speed));
        double deltaX = this.wantedX - this.fish.getX();
        double deltaY = this.wantedY - this.fish.getY();
        double deltaZ = this.wantedZ - this.fish.getZ();
        if (deltaY != 0.0) {
          double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
          this.fish.setDeltaMovement(
              this.fish
                  .getDeltaMovement()
                  .add(0.0, this.fish.getSpeed() * (deltaY / distance) * 0.1, 0.0));
        }

        if (deltaX != 0.0 || deltaZ != 0.0) {
          float yRot = (float) (Mth.atan2(deltaZ, deltaX) * 180.0 / Math.PI) - 90.0f;
          this.fish.setYRot(this.rotlerp(this.fish.getYRot(), yRot, 90.0f));
          this.fish.yBodyRot = this.fish.getYRot();
        }
      } else {
        this.fish.setSpeed(0.0f);
      }
    }
  }
}
