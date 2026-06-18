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
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public abstract class EasyModelHostEntity extends PathfinderMob implements EasyModelEntityHost {

  public static final float FALLBACK_WIDTH = EasyModelHostSupport.FALLBACK_WIDTH;
  public static final float FALLBACK_HEIGHT = EasyModelHostSupport.FALLBACK_HEIGHT;
  public static final float FALLBACK_EYE_HEIGHT = EasyModelHostSupport.FALLBACK_EYE_HEIGHT;
  public static final ResourceLocation MISSING_PROFILE_ID = EasyModelHostSupport.MISSING_PROFILE_ID;

  private static final EntityDataAccessor<String> PROFILE_ID =
      SynchedEntityData.defineId(EasyModelHostEntity.class, EasyModelEntityDataSerializers.STRING);
  private static final EntityDataAccessor<String> RENDER_PROFILE_ID =
      SynchedEntityData.defineId(EasyModelHostEntity.class, EasyModelEntityDataSerializers.STRING);
  private static final EntityDataAccessor<String> VERSION =
      SynchedEntityData.defineId(EasyModelHostEntity.class, EasyModelEntityDataSerializers.STRING);
  private static final EntityDataAccessor<Float> WIDTH =
      SynchedEntityData.defineId(EasyModelHostEntity.class, EasyModelEntityDataSerializers.FLOAT);
  private static final EntityDataAccessor<Float> HEIGHT =
      SynchedEntityData.defineId(EasyModelHostEntity.class, EasyModelEntityDataSerializers.FLOAT);
  private static final EntityDataAccessor<Float> EYE_HEIGHT =
      SynchedEntityData.defineId(EasyModelHostEntity.class, EasyModelEntityDataSerializers.FLOAT);
  private static final EntityDataAccessor<ModelBodyType> BODY_TYPE =
      SynchedEntityData.defineId(
          EasyModelHostEntity.class, EasyModelEntityDataSerializers.BODY_TYPE);
  private static final EntityDataAccessor<EasyModelAnimationState> ANIMATION_STATE =
      SynchedEntityData.defineId(
          EasyModelHostEntity.class, EasyModelEntityDataSerializers.ANIMATION_STATE);

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

  protected EasyModelHostEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
    super(entityType, level);
    this.setPersistenceRequired();
  }

  public static AttributeSupplier.Builder createAttributes() {
    return EasyModelHostSupport.createAttributes();
  }

  @Override
  protected void defineSynchedData() {
    super.defineSynchedData();
    EasyModelHostSupport.defineSynchedData(this.entityData, FIELDS);
  }

  @Override
  public EntityDimensions getDimensions(Pose pose) {
    return EasyModelHostSupport.getDimensions(this.entityData, FIELDS);
  }

  @Override
  protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
    return EasyModelHostSupport.getStandingEyeHeight(this.entityData, FIELDS);
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
}
