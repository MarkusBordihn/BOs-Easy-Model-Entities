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

import de.markusbordihn.easymodelentities.api.EasyModelRenderable;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationSetting;
import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
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
      ModelResourcePaths.modResourceLocation("missing");
  public static final int RANDOM_IDLE_BURST_LENGTH = 53;
  public static final int RANDOM_IDLE_MIN_GAP = 200;
  public static final int RANDOM_IDLE_GAP_RANGE = 201;
  private static final Set<EasyModelHostBlockEntity> LOADED_HOSTS =
      Collections.newSetFromMap(Collections.synchronizedMap(new WeakHashMap<>()));

  static {
    EasyModelReloadDispatcher.addProfileReloadListener(EasyModelHostBlockEntity::reloadProfiles);
  }

  private EasyModelRuntimeContract runtimeContract;
  private int animationTicks = 0;
  private int randomIdleTicks = 0;
  private int randomIdleBurstTicks = 0;
  private int randomIdleDelayTicks = -1;

  protected EasyModelHostBlockEntity(
      BlockEntityType<? extends EasyModelHostBlockEntity> blockEntityType,
      BlockPos blockPos,
      BlockState blockState) {
    super(blockEntityType, blockPos, blockState);
    LOADED_HOSTS.add(this);
    this.runtimeContract =
        new EasyModelRuntimeContract(
            MISSING_PROFILE_ID,
            MISSING_PROFILE_ID,
            "",
            FALLBACK_WIDTH,
            FALLBACK_HEIGHT,
            FALLBACK_EYE_HEIGHT,
            ModelBodyType.STATIC,
            EasyModelAnimationSetting.AUTO);
  }

  private static Optional<EasyModelEntityProfile> activeBlockEntityProfile(
      ResourceLocation profileId) {
    return EasyModelServices.profileService()
        .getProfile(profileId)
        .filter(EasyModelEntityProfile::isActive)
        .filter(profile -> profile.modelType() == ModelType.BLOCK_ENTITY);
  }

  private static int randomIdleDelay(RandomSource random) {
    return RANDOM_IDLE_MIN_GAP + random.nextInt(RANDOM_IDLE_GAP_RANGE);
  }

  private static void reloadProfiles() {
    List<EasyModelHostBlockEntity> hosts;
    synchronized (LOADED_HOSTS) {
      hosts = new ArrayList<>(LOADED_HOSTS);
    }
    for (EasyModelHostBlockEntity host : hosts) {
      if (host.level == null || host.level.isClientSide || host.isRemoved()) {
        continue;
      }
      ResourceLocation profileId = host.runtimeContract.profileId();
      activeBlockEntityProfile(profileId)
          .ifPresentOrElse(
              profile -> host.applyProfile(profile, host.runtimeContract.animation(), true),
              () ->
                  host.applyRuntimeContract(
                      host.fallbackRuntimeContract(profileId, host.runtimeContract.animation()),
                      true));
    }
  }

  public void serverTick(Level level, BlockPos blockPos, BlockState blockState) {}

  public void clientTick(Level level, BlockPos blockPos, BlockState blockState) {
    if (this instanceof EasyModelRandomlyAnimatedBlockEntity) {
      randomIdleTick(level == null ? RandomSource.create() : level.random);
      return;
    }

    this.animationTicks++;
  }

  @Override
  public void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider registries) {
    super.loadAdditional(compoundTag, registries);

    EasyModelHostPersistence.State state = EasyModelHostPersistence.read(compoundTag);
    ResourceLocation profileId = state.profileId();
    ResourceLocation renderProfileId = state.renderProfileId();
    String version = state.version();
    ModelBodyType bodyType = state.bodyType();
    EasyModelAnimationSetting animation = state.animation();

    if (profileId == null) {
      applyRuntimeContract(fallbackRuntimeContract(MISSING_PROFILE_ID, animation), false);
      return;
    }

    Optional<EasyModelEntityProfile> profile = activeBlockEntityProfile(profileId);
    if (profile.isPresent()) {
      applyProfile(profile.get(), animation, false);
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
            animation),
        false);
  }

  @Override
  protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider registries) {
    super.saveAdditional(compoundTag, registries);
    compoundTag.putString(
        EasyModelHostPersistence.PROFILE_ID_TAG, this.runtimeContract.profileId().toString());
    compoundTag.putString(
        EasyModelHostPersistence.RENDER_PROFILE_ID_TAG,
        this.runtimeContract.renderProfileId().toString());
    compoundTag.putString(EasyModelHostPersistence.VERSION_TAG, this.runtimeContract.version());
    compoundTag.putString(
        EasyModelHostPersistence.BODY_TYPE_TAG,
        this.runtimeContract.bodyType().getSerializedName());
    EasyModelHostPersistence.writeAnimation(compoundTag, this.runtimeContract.animation());
  }

  @Override
  public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
    CompoundTag compoundTag = super.getUpdateTag(registries);
    saveAdditional(compoundTag, registries);
    return compoundTag;
  }

  @Override
  public ClientboundBlockEntityDataPacket getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }

  @Override
  public ResourceLocation getEasyModelProfileId() {
    return this.runtimeContract.profileId();
  }

  public void setEasyModelProfileId(ResourceLocation profileId) {
    Objects.requireNonNull(profileId, "profileId");
    Optional<EasyModelEntityProfile> profile = activeBlockEntityProfile(profileId);
    if (profile.isPresent()) {
      applyProfile(profile.get(), this.runtimeContract.animation(), true);
      return;
    }

    applyRuntimeContract(
        fallbackRuntimeContract(profileId, this.runtimeContract.animation()), true);
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
  public EasyModelAnimationSetting getEasyModelAnimationSetting() {
    return this.runtimeContract.animation();
  }

  public void setEasyModelAnimation(EasyModelAnimationSetting animation) {
    applyRuntimeContract(
        new EasyModelRuntimeContract(
            this.runtimeContract.profileId(),
            this.runtimeContract.renderProfileId(),
            this.runtimeContract.version(),
            this.runtimeContract.width(),
            this.runtimeContract.height(),
            this.runtimeContract.eyeHeight(),
            this.runtimeContract.bodyType(),
            Objects.requireNonNull(animation, "animationState")),
        true);
  }

  public int getEasyModelAnimationTicks() {
    return this.animationTicks;
  }

  public float getEasyModelAnimationTicks(float partialTick) {
    return this instanceof EasyModelRandomlyAnimatedBlockEntity
        ? getRandomIdleAnimationTicks(partialTick)
        : this.animationTicks + partialTick;
  }

  public EasyModelRuntimeContract getEasyModelRuntimeContract() {
    return this.runtimeContract;
  }

  protected EasyModelRuntimeContract fallbackRuntimeContract(
      ResourceLocation profileId, EasyModelAnimationSetting animation) {
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
        animation);
  }

  protected void applyProfile(
      EasyModelEntityProfile profile, EasyModelAnimationSetting animation, boolean sync) {
    applyRuntimeContract(EasyModelRuntimeContract.fromProfile(profile, animation), sync);
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

  private void randomIdleTick(RandomSource random) {
    if (this.randomIdleDelayTicks < 0) {
      this.randomIdleDelayTicks = randomIdleDelay(random);
    }
    if (this.randomIdleBurstTicks > 0) {
      this.randomIdleTicks++;
      this.randomIdleBurstTicks--;
      return;
    }

    if (--this.randomIdleDelayTicks <= 0) {
      this.randomIdleTicks = 0;
      this.randomIdleBurstTicks = RANDOM_IDLE_BURST_LENGTH;
      this.randomIdleDelayTicks = randomIdleDelay(random);
    }
  }

  private float getRandomIdleAnimationTicks(float partialTick) {
    return this.randomIdleBurstTicks > 0 ? this.randomIdleTicks + partialTick : 0.0f;
  }
}
