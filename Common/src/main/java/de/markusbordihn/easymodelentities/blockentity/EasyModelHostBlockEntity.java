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

package de.markusbordihn.easymodelentities.blockentity;

import de.markusbordihn.easymodelentities.Constants;
import de.markusbordihn.easymodelentities.api.EasyModelRenderable;
import de.markusbordihn.easymodelentities.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.profile.ModelType;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.runtime.EasyModelAnimationState;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class EasyModelHostBlockEntity extends BlockEntity implements EasyModelRenderable {

  public static final float FALLBACK_WIDTH = 1.0f;
  public static final float FALLBACK_HEIGHT = 1.0f;
  public static final float FALLBACK_EYE_HEIGHT = 0.5f;
  public static final ResourceLocation MISSING_PROFILE_ID =
      new ResourceLocation(Constants.MOD_ID, "missing");

  private static final String PROFILE_ID_TAG = "ProfileId";
  private static final String RENDER_PROFILE_ID_TAG = "RenderProfileId";
  private static final String VERSION_TAG = "Version";
  private static final String BODY_TYPE_TAG = "BodyType";
  private static final String ANIMATION_STATE_TAG = "AnimationState";

  private EasyModelRuntimeContract runtimeContract =
      EasyModelRuntimeContract.fallback(MISSING_PROFILE_ID);
  private int animationTicks = 0;

  protected EasyModelHostBlockEntity(
      BlockEntityType<? extends EasyModelHostBlockEntity> blockEntityType,
      BlockPos blockPos,
      BlockState blockState) {
    super(blockEntityType, blockPos, blockState);
    this.runtimeContract =
        new EasyModelRuntimeContract(
            MISSING_PROFILE_ID,
            MISSING_PROFILE_ID,
            "",
            FALLBACK_WIDTH,
            FALLBACK_HEIGHT,
            FALLBACK_EYE_HEIGHT,
            ModelBodyType.STATIC,
            EasyModelAnimationState.AUTO);
  }

  private static Optional<EasyModelEntityProfile> activeBlockEntityProfile(
      ResourceLocation profileId) {
    return EasyModelServices.profileService()
        .getProfile(profileId)
        .filter(EasyModelEntityProfile::isActive)
        .filter(profile -> profile.modelType() == ModelType.BLOCK_ENTITY);
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

  public void serverTick(Level level, BlockPos blockPos, BlockState blockState) {}

  public void clientTick(Level level, BlockPos blockPos, BlockState blockState) {
    this.animationTicks++;
  }

  @Override
  public void load(CompoundTag compoundTag) {
    super.load(compoundTag);

    ResourceLocation profileId = parseResourceLocation(compoundTag.getString(PROFILE_ID_TAG));
    ResourceLocation renderProfileId =
        parseResourceLocation(compoundTag.getString(RENDER_PROFILE_ID_TAG));
    String version = compoundTag.getString(VERSION_TAG);
    ModelBodyType bodyType = ModelBodyType.bySerializedName(compoundTag.getString(BODY_TYPE_TAG));
    EasyModelAnimationState animationState =
        EasyModelAnimationState.bySerializedName(compoundTag.getString(ANIMATION_STATE_TAG));

    if (profileId == null) {
      applyRuntimeContract(fallbackRuntimeContract(MISSING_PROFILE_ID, animationState), false);
      return;
    }

    Optional<EasyModelEntityProfile> profile = activeBlockEntityProfile(profileId);
    if (profile.isPresent()) {
      applyProfile(profile.get(), animationState, false);
      return;
    }

    applyRuntimeContract(
        new EasyModelRuntimeContract(
            profileId,
            renderProfileId == null ? profileId : renderProfileId,
            Objects.requireNonNullElse(version, ""),
            FALLBACK_WIDTH,
            FALLBACK_HEIGHT,
            FALLBACK_EYE_HEIGHT,
            bodyType,
            animationState),
        false);
  }

  @Override
  protected void saveAdditional(CompoundTag compoundTag) {
    super.saveAdditional(compoundTag);
    compoundTag.putString(PROFILE_ID_TAG, this.runtimeContract.profileId().toString());
    compoundTag.putString(RENDER_PROFILE_ID_TAG, this.runtimeContract.renderProfileId().toString());
    compoundTag.putString(VERSION_TAG, this.runtimeContract.version());
    compoundTag.putString(BODY_TYPE_TAG, this.runtimeContract.bodyType().getSerializedName());
    compoundTag.putString(
        ANIMATION_STATE_TAG, this.runtimeContract.animationState().getSerializedName());
  }

  @Override
  public CompoundTag getUpdateTag() {
    CompoundTag compoundTag = super.getUpdateTag();
    saveAdditional(compoundTag);
    return compoundTag;
  }

  @Override
  public ClientboundBlockEntityDataPacket getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }

  @Override
  public ResourceLocation getEasyModelProfileId() {
    return parseResourceLocationOrMissing(this.runtimeContract.profileId().toString());
  }

  public void setEasyModelProfileId(ResourceLocation profileId) {
    Objects.requireNonNull(profileId, "profileId");
    Optional<EasyModelEntityProfile> profile = activeBlockEntityProfile(profileId);
    if (profile.isPresent()) {
      applyProfile(profile.get(), this.runtimeContract.animationState(), true);
      return;
    }

    applyRuntimeContract(
        fallbackRuntimeContract(profileId, this.runtimeContract.animationState()), true);
  }

  @Override
  public ResourceLocation getEasyModelRenderProfileId() {
    return this.runtimeContract.renderProfileId();
  }

  @Override
  public String getEasyModelVersion() {
    return this.runtimeContract.version();
  }

  @Override
  public int getEasyModelAnimationState() {
    return this.runtimeContract.animationState().getApiState();
  }

  public void setEasyModelAnimationState(EasyModelAnimationState animationState) {
    applyRuntimeContract(
        new EasyModelRuntimeContract(
            this.runtimeContract.profileId(),
            this.runtimeContract.renderProfileId(),
            this.runtimeContract.version(),
            this.runtimeContract.width(),
            this.runtimeContract.height(),
            this.runtimeContract.eyeHeight(),
            this.runtimeContract.bodyType(),
            Objects.requireNonNull(animationState, "animationState")),
        true);
  }

  public int getEasyModelAnimationTicks() {
    return this.animationTicks;
  }

  public EasyModelRuntimeContract getEasyModelRuntimeContract() {
    return this.runtimeContract;
  }

  protected EasyModelRuntimeContract fallbackRuntimeContract(
      ResourceLocation profileId, EasyModelAnimationState animationState) {
    ResourceLocation fallbackProfileId =
        profileId == null ? MISSING_PROFILE_ID : Objects.requireNonNull(profileId, "profileId");
    return new EasyModelRuntimeContract(
        fallbackProfileId,
        fallbackProfileId,
        "",
        FALLBACK_WIDTH,
        FALLBACK_HEIGHT,
        FALLBACK_EYE_HEIGHT,
        ModelBodyType.STATIC,
        animationState);
  }

  protected void applyProfile(
      EasyModelEntityProfile profile, EasyModelAnimationState animationState, boolean sync) {
    applyRuntimeContract(EasyModelRuntimeContract.fromProfile(profile, animationState), sync);
  }

  protected void applyRuntimeContract(EasyModelRuntimeContract contract, boolean sync) {
    this.runtimeContract = Objects.requireNonNull(contract, "contract");
    setChanged();
    if (sync) {
      syncBlockEntity();
    }
  }

  protected void syncBlockEntity() {
    if (this.level != null && !this.level.isClientSide) {
      BlockState blockState = getBlockState();
      this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_CLIENTS);
    }
  }
}
