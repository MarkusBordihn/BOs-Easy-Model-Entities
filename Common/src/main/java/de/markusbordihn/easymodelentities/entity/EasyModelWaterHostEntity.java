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

import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.network.syncher.EasyModelEntityDataSerializers;
import de.markusbordihn.easymodelentities.runtime.EasyModelAnimationState;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.Level;
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
  private static final EntityDataAccessor<EasyModelAnimationState> ANIMATION_STATE =
      SynchedEntityData.defineId(
          EasyModelWaterHostEntity.class, EasyModelEntityDataSerializers.ANIMATION_STATE);

  private static final EasyModelHostFields FIELDS =
      new EasyModelHostFields(
          PROFILE_ID,
          RENDER_PROFILE_ID,
          VERSION,
          WIDTH,
          HEIGHT,
          EYE_HEIGHT,
          BODY_TYPE,
          ANIMATION_STATE);

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
  public void addAdditionalSaveData(CompoundTag compoundTag) {
    super.addAdditionalSaveData(compoundTag);
    EasyModelHostSupport.addAdditionalSaveData(compoundTag, this.entityData, FIELDS);
  }

  @Override
  public void readAdditionalSaveData(CompoundTag compoundTag) {
    super.readAdditionalSaveData(compoundTag);
    EasyModelHostSupport.readAdditionalSaveData(this, compoundTag, FIELDS);
  }

  @Override
  public ResourceLocation getEasyModelProfileId() {
    return EasyModelHostSupport.getProfileId(this.entityData, FIELDS);
  }

  @Override
  public void setEasyModelProfileId(ResourceLocation profileId) {
    EasyModelHostSupport.setProfileId(this, FIELDS, profileId);
  }

  @Override
  public ResourceLocation getEasyModelRenderProfileId() {
    return EasyModelHostSupport.getRenderProfileId(this.entityData, FIELDS);
  }

  @Override
  public String getEasyModelVersion() {
    return EasyModelHostSupport.getVersion(this.entityData, FIELDS);
  }

  @Override
  public EasyModelAnimationState getEasyModelAnimationState() {
    return EasyModelHostSupport.getAnimationState(this.entityData, FIELDS);
  }

  @Override
  public void setEasyModelAnimationState(EasyModelAnimationState animationState) {
    EasyModelHostSupport.setAnimationState(this.entityData, FIELDS, animationState);
  }

  @Override
  public EasyModelRuntimeContract getEasyModelRuntimeContract() {
    return EasyModelHostSupport.getRuntimeContract(this.entityData, FIELDS);
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
