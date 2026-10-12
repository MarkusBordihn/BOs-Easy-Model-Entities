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

package de.markusbordihn.easymodelentities.api;

import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationSetting;
import de.markusbordihn.easymodelentities.api.data.EasyModelDisplaySettings;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSetting;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationPlayback;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationSequence;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationTransition;
import de.markusbordihn.easymodelentities.blockentity.EasyModelHostBlockEntity;
import de.markusbordihn.easymodelentities.network.animation.ClientboundEasyModelAnimationPacket;
import de.markusbordihn.easymodelentities.network.animation.ClientboundEasyModelAnimationSequencePacket;
import de.markusbordihn.easymodelentities.network.animation.EasyModelAnimationNetwork;
import de.markusbordihn.easymodelentities.network.animation.EasyModelAnimationPacketOperation;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class EasyModelBlockEntitiesApi {

  private EasyModelBlockEntitiesApi() {}

  public static Optional<ResourceLocation> getProfileId(BlockEntity blockEntity) {
    Objects.requireNonNull(blockEntity, "blockEntity");
    if (blockEntity instanceof EasyModelHostBlockEntity hostBlockEntity) {
      return Optional.of(hostBlockEntity.getEasyModelProfileId());
    }
    if (blockEntity instanceof EasyModelRenderable renderable) {
      return Optional.ofNullable(renderable.getEasyModelProfileId());
    }

    return Optional.empty();
  }

  public static boolean setProfileId(BlockEntity blockEntity, ResourceLocation profileId) {
    Objects.requireNonNull(blockEntity, "blockEntity");
    Objects.requireNonNull(profileId, "profileId");
    if (blockEntity instanceof EasyModelHostBlockEntity hostBlockEntity) {
      hostBlockEntity.setEasyModelProfileId(profileId);
      return true;
    }

    return false;
  }

  public static EasyModelTextureSetting getTextureSetting(BlockEntity blockEntity) {
    Objects.requireNonNull(blockEntity, "blockEntity");
    if (blockEntity instanceof EasyModelRenderable renderable) {
      return renderable.getEasyModelTextureSetting();
    }

    return EasyModelTextureSetting.EMPTY;
  }

  public static boolean setTextureSetting(
      BlockEntity blockEntity, EasyModelTextureSetting textureSetting) {
    Objects.requireNonNull(blockEntity, "blockEntity");
    Objects.requireNonNull(textureSetting, "textureSetting");
    if (blockEntity instanceof EasyModelHostBlockEntity hostBlockEntity) {
      hostBlockEntity.setEasyModelTexture(textureSetting);
      return true;
    }
    if (blockEntity instanceof EasyModelMutableRenderable mutableRenderable) {
      mutableRenderable.setEasyModelTextureSetting(textureSetting);
      return true;
    }

    return false;
  }

  public static boolean clearTextureSetting(BlockEntity blockEntity) {
    return setTextureSetting(blockEntity, EasyModelTextureSetting.EMPTY);
  }

  public static EasyModelAnimationSetting getAnimationSetting(BlockEntity blockEntity) {
    Objects.requireNonNull(blockEntity, "blockEntity");
    if (blockEntity instanceof EasyModelRenderable renderable) {
      return renderable.getEasyModelAnimationSetting();
    }

    return EasyModelAnimationSetting.AUTO;
  }

  public static boolean setAnimation(
      BlockEntity blockEntity, EasyModelAnimationSetting animationSetting) {
    Objects.requireNonNull(blockEntity, "blockEntity");
    Objects.requireNonNull(animationSetting, "animationSetting");
    if (blockEntity instanceof EasyModelHostBlockEntity hostBlockEntity) {
      hostBlockEntity.setEasyModelAnimation(animationSetting);
      return true;
    }
    if (blockEntity instanceof EasyModelMutableRenderable mutableRenderable) {
      mutableRenderable.setEasyModelAnimationSetting(animationSetting);
      return true;
    }

    return false;
  }

  public static float getOpacity(BlockEntity blockEntity) {
    Objects.requireNonNull(blockEntity, "blockEntity");
    if (blockEntity instanceof EasyModelRenderable renderable) {
      return renderable.getEasyModelOpacity();
    }

    return EasyModelDisplaySettings.NO_OPACITY;
  }

  public static boolean setOpacity(BlockEntity blockEntity, float opacity) {
    Objects.requireNonNull(blockEntity, "blockEntity");
    if (opacity != EasyModelDisplaySettings.NO_OPACITY) {
      EasyModelDisplaySettings.requireOpacity(opacity);
    }
    if (blockEntity instanceof EasyModelHostBlockEntity hostBlockEntity) {
      hostBlockEntity.setEasyModelOpacity(opacity);
      return true;
    }
    if (blockEntity instanceof EasyModelMutableRenderable mutableRenderable) {
      mutableRenderable.setEasyModelOpacity(opacity);
      return true;
    }

    return false;
  }

  public static boolean clearOpacity(BlockEntity blockEntity) {
    return setOpacity(blockEntity, EasyModelDisplaySettings.NO_OPACITY);
  }

  public static int getLightLevel(BlockEntity blockEntity) {
    Objects.requireNonNull(blockEntity, "blockEntity");
    if (blockEntity instanceof EasyModelRenderable renderable) {
      return renderable.getEasyModelLightLevel();
    }

    return EasyModelDisplaySettings.NO_LIGHT_LEVEL;
  }

  public static boolean setLightLevel(BlockEntity blockEntity, int lightLevel) {
    Objects.requireNonNull(blockEntity, "blockEntity");
    if (lightLevel != EasyModelDisplaySettings.NO_LIGHT_LEVEL) {
      EasyModelDisplaySettings.requireLightLevel(lightLevel);
    }
    if (blockEntity instanceof EasyModelHostBlockEntity hostBlockEntity) {
      hostBlockEntity.setEasyModelLightLevel(lightLevel);
      return true;
    }
    if (blockEntity instanceof EasyModelMutableRenderable mutableRenderable) {
      mutableRenderable.setEasyModelLightLevel(lightLevel);
      return true;
    }

    return false;
  }

  public static boolean clearLightLevel(BlockEntity blockEntity) {
    return setLightLevel(blockEntity, EasyModelDisplaySettings.NO_LIGHT_LEVEL);
  }

  public static boolean playAnimation(
      BlockEntity blockEntity,
      EasyModelAnimation animation,
      EasyModelAnimationPlayback playback,
      EasyModelAnimationTransition transition) {
    Objects.requireNonNull(blockEntity, "blockEntity");
    Objects.requireNonNull(animation, "animation");
    Objects.requireNonNull(playback, "playback");
    Objects.requireNonNull(transition, "transition");
    if (!(blockEntity.getLevel() instanceof ServerLevel serverLevel)) {
      return false;
    }

    BlockPos blockPos = blockEntity.getBlockPos();
    EasyModelAnimationNetwork.send(
        serverLevel,
        blockPos,
        ClientboundEasyModelAnimationPacket.playBlockEntity(
            blockPos, animation, playback, transition));
    return true;
  }

  public static boolean playRandomAnimation(
      BlockEntity blockEntity,
      List<EasyModelAnimation> animations,
      EasyModelAnimationPlayback playback,
      EasyModelAnimationTransition transition) {
    Objects.requireNonNull(blockEntity, "blockEntity");
    Objects.requireNonNull(animations, "animations");
    if (animations.isEmpty() || blockEntity.getLevel() == null) {
      return false;
    }

    return playAnimation(
        blockEntity,
        animations.get(blockEntity.getLevel().getRandom().nextInt(animations.size())),
        playback,
        transition);
  }

  public static boolean playAnimationSequence(
      BlockEntity blockEntity, EasyModelAnimationSequence sequence) {
    Objects.requireNonNull(blockEntity, "blockEntity");
    Objects.requireNonNull(sequence, "sequence");
    if (!(blockEntity.getLevel() instanceof ServerLevel serverLevel)) {
      return false;
    }

    BlockPos blockPos = blockEntity.getBlockPos();
    EasyModelAnimationNetwork.send(
        serverLevel,
        blockPos,
        ClientboundEasyModelAnimationSequencePacket.forBlockEntity(blockPos, sequence));
    return true;
  }

  public static boolean stopAnimation(
      BlockEntity blockEntity, EasyModelAnimationTransition transition) {
    Objects.requireNonNull(transition, "transition");
    return control(blockEntity, EasyModelAnimationPacketOperation.STOP, transition);
  }

  public static boolean restartAnimation(BlockEntity blockEntity) {
    return control(
        blockEntity,
        EasyModelAnimationPacketOperation.RESTART,
        EasyModelAnimationTransition.IMMEDIATE);
  }

  private static boolean control(
      BlockEntity blockEntity,
      EasyModelAnimationPacketOperation operation,
      EasyModelAnimationTransition transition) {
    Objects.requireNonNull(blockEntity, "blockEntity");
    if (!(blockEntity.getLevel() instanceof ServerLevel serverLevel)) {
      return false;
    }

    BlockPos blockPos = blockEntity.getBlockPos();
    EasyModelAnimationNetwork.send(
        serverLevel,
        blockPos,
        ClientboundEasyModelAnimationPacket.controlBlockEntity(blockPos, operation, transition));
    return true;
  }
}
