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
import de.markusbordihn.easymodelentities.api.data.EasyModelBodyType;
import de.markusbordihn.easymodelentities.api.data.EasyModelDisplaySettings;
import de.markusbordihn.easymodelentities.api.data.EasyModelProfileInfo;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSetting;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationPlayback;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationSequence;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationTransition;
import de.markusbordihn.easymodelentities.data.EasyModelApiMapper;
import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.entity.EasyModelEntityHost;
import de.markusbordihn.easymodelentities.entity.EasyModelStaticEntity;
import de.markusbordihn.easymodelentities.network.animation.ClientboundEasyModelAnimationPacket;
import de.markusbordihn.easymodelentities.network.animation.ClientboundEasyModelAnimationSequencePacket;
import de.markusbordihn.easymodelentities.network.animation.EasyModelAnimationNetwork;
import de.markusbordihn.easymodelentities.network.animation.EasyModelAnimationPacketOperation;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

public final class EasyModelEntitiesApi {

  private EasyModelEntitiesApi() {}

  public static boolean hasProfile(Identifier profileId) {
    return EasyModelServices.profileService()
        .hasProfile(Objects.requireNonNull(profileId, "profileId"));
  }

  public static Optional<EasyModelProfileInfo> getProfileInfo(Identifier profileId) {
    return EasyModelServices.profileService()
        .getProfile(Objects.requireNonNull(profileId, "profileId"))
        .map(EasyModelEntitiesApi::profileInfo);
  }

  public static List<EasyModelProfileInfo> listProfileInfos() {
    return EasyModelServices.profileService().getActiveProfiles().stream()
        .map(EasyModelEntitiesApi::profileInfo)
        .toList();
  }

  public static List<EasyModelProfileInfo> listProfileInfos(EasyModelBodyType bodyType) {
    Objects.requireNonNull(bodyType, "bodyType");
    return EasyModelServices.profileService().getActiveProfiles().stream()
        .filter(profile -> EasyModelApiMapper.bodyType(profile.bodyType()) == bodyType)
        .map(EasyModelEntitiesApi::profileInfo)
        .toList();
  }

  public static List<Identifier> listProfileIds() {
    return EasyModelServices.profileService().getActiveProfiles().stream()
        .map(EasyModelEntityProfile::id)
        .toList();
  }

  public static Optional<Entity> createEntity(Level level, Identifier profileId, Vec3 position) {
    Objects.requireNonNull(level, "level");
    Objects.requireNonNull(profileId, "profileId");
    Objects.requireNonNull(position, "position");
    return EasyModelServices.entityFactory().createEntity(level, profileId, position);
  }

  public static Optional<Identifier> getProfileId(Entity entity) {
    Objects.requireNonNull(entity, "entity");
    if (entity instanceof EasyModelEntityHost hostEntity) {
      return Optional.of(hostEntity.getEasyModelProfileId());
    }
    if (entity instanceof EasyModelRenderable renderable) {
      return Optional.ofNullable(renderable.getEasyModelProfileId());
    }

    return Optional.empty();
  }

  public static float getOpacity(Entity entity) {
    Objects.requireNonNull(entity, "entity");
    if (entity instanceof EasyModelEntityHost hostEntity) {
      return hostEntity.getEasyModelOpacity();
    }
    if (entity instanceof EasyModelRenderable renderable) {
      return renderable.getEasyModelOpacity();
    }

    return EasyModelDisplaySettings.NO_OPACITY;
  }

  public static boolean setOpacity(Entity entity, float opacity) {
    Objects.requireNonNull(entity, "entity");
    if (opacity != EasyModelDisplaySettings.NO_OPACITY) {
      EasyModelDisplaySettings.requireOpacity(opacity);
    }
    if (entity instanceof EasyModelEntityHost hostEntity) {
      hostEntity.setEasyModelOpacity(opacity);
      return true;
    }
    if (entity instanceof EasyModelMutableRenderable mutableRenderable) {
      mutableRenderable.setEasyModelOpacity(opacity);
      return true;
    }

    return false;
  }

  public static boolean clearOpacity(Entity entity) {
    return setOpacity(entity, EasyModelDisplaySettings.NO_OPACITY);
  }

  public static int getLightLevel(Entity entity) {
    Objects.requireNonNull(entity, "entity");
    if (entity instanceof EasyModelEntityHost hostEntity) {
      return hostEntity.getEasyModelLightLevel();
    }
    if (entity instanceof EasyModelRenderable renderable) {
      return renderable.getEasyModelLightLevel();
    }

    return EasyModelDisplaySettings.NO_LIGHT_LEVEL;
  }

  public static boolean setLightLevel(Entity entity, int lightLevel) {
    Objects.requireNonNull(entity, "entity");
    if (lightLevel != EasyModelDisplaySettings.NO_LIGHT_LEVEL) {
      EasyModelDisplaySettings.requireLightLevel(lightLevel);
    }
    if (entity instanceof EasyModelEntityHost hostEntity) {
      hostEntity.setEasyModelLightLevel(lightLevel);
      return true;
    }
    if (entity instanceof EasyModelMutableRenderable mutableRenderable) {
      mutableRenderable.setEasyModelLightLevel(lightLevel);
      return true;
    }

    return false;
  }

  public static boolean clearLightLevel(Entity entity) {
    return setLightLevel(entity, EasyModelDisplaySettings.NO_LIGHT_LEVEL);
  }

  public static boolean isFrozen(Entity entity) {
    Objects.requireNonNull(entity, "entity");
    return entity instanceof EasyModelEntityHost && entity instanceof Mob mob && mob.isNoAi();
  }

  public static boolean setFrozen(Entity entity, boolean frozen) {
    Objects.requireNonNull(entity, "entity");
    if (!(entity instanceof EasyModelEntityHost) || !(entity instanceof Mob mob)) {
      return false;
    }

    mob.setNoAi(frozen || mob instanceof EasyModelStaticEntity);
    if (frozen) {
      mob.getNavigation().stop();
      mob.setDeltaMovement(Vec3.ZERO);
      mob.setXxa(0.0f);
      mob.setYya(0.0f);
      mob.setZza(0.0f);
    }
    return true;
  }

  public static boolean sitOnShoulder(Entity entity, Player player) {
    Objects.requireNonNull(entity, "entity");
    Objects.requireNonNull(player, "player");
    if (!(entity instanceof EasyModelEntityHost)
        || !(player instanceof ServerPlayer serverPlayer)) {
      return false;
    }

    TagValueOutput entityOutput =
        TagValueOutput.createWithContext(ProblemReporter.DISCARDING, entity.registryAccess());
    if (!entity.save(entityOutput)
        || !serverPlayer.setEntityOnShoulder(entityOutput.buildResult())) {
      return false;
    }

    entity.discard();
    return true;
  }

  public static Vec3 getShoulderOffset(Entity entity) {
    Objects.requireNonNull(entity, "entity");
    if (entity instanceof EasyModelEntityHost hostEntity) {
      return hostEntity.getEasyModelShoulderOffset();
    }

    return Vec3.ZERO;
  }

  public static boolean setShoulderOffset(Entity entity, Vec3 shoulderOffset) {
    Objects.requireNonNull(entity, "entity");
    Objects.requireNonNull(shoulderOffset, "shoulderOffset");
    EasyModelDisplaySettings.requireShoulderOffset(
        shoulderOffset.x, shoulderOffset.y, shoulderOffset.z);
    if (entity instanceof EasyModelEntityHost hostEntity) {
      hostEntity.setEasyModelShoulderOffset(shoulderOffset);
      return true;
    }

    return false;
  }

  public static boolean clearShoulderOffset(Entity entity) {
    return setShoulderOffset(entity, Vec3.ZERO);
  }

  public static EasyModelTextureSetting getTextureSetting(Entity entity) {
    Objects.requireNonNull(entity, "entity");
    if (entity instanceof EasyModelEntityHost hostEntity) {
      return hostEntity.getEasyModelTextureSetting();
    }
    if (entity instanceof EasyModelRenderable renderable) {
      return renderable.getEasyModelTextureSetting();
    }

    return EasyModelTextureSetting.EMPTY;
  }

  public static boolean setTextureSetting(Entity entity, EasyModelTextureSetting textureSetting) {
    Objects.requireNonNull(entity, "entity");
    Objects.requireNonNull(textureSetting, "textureSetting");
    if (entity instanceof EasyModelEntityHost hostEntity) {
      hostEntity.setEasyModelTexture(textureSetting);
      return true;
    }
    if (entity instanceof EasyModelMutableRenderable mutableRenderable) {
      mutableRenderable.setEasyModelTextureSetting(textureSetting);
      return true;
    }

    return false;
  }

  public static boolean clearTextureSetting(Entity entity) {
    return setTextureSetting(entity, EasyModelTextureSetting.EMPTY);
  }

  public static EasyModelAnimationSetting getAnimationSetting(Entity entity) {
    Objects.requireNonNull(entity, "entity");
    if (entity instanceof EasyModelEntityHost hostEntity) {
      return hostEntity.getEasyModelAnimationSetting();
    }
    if (entity instanceof EasyModelRenderable renderable) {
      return renderable.getEasyModelAnimationSetting();
    }

    return EasyModelAnimationSetting.AUTO;
  }

  public static boolean setAnimation(Entity entity, EasyModelAnimationSetting animationSetting) {
    Objects.requireNonNull(entity, "entity");
    Objects.requireNonNull(animationSetting, "animationSetting");
    if (entity instanceof EasyModelEntityHost hostEntity) {
      hostEntity.setEasyModelAnimation(animationSetting);
      return true;
    }
    if (entity instanceof EasyModelMutableRenderable mutableRenderable) {
      mutableRenderable.setEasyModelAnimationSetting(animationSetting);
      return true;
    }

    return false;
  }

  public static boolean playAnimation(
      Entity entity,
      EasyModelAnimation animation,
      EasyModelAnimationPlayback playback,
      EasyModelAnimationTransition transition) {
    Objects.requireNonNull(entity, "entity");
    Objects.requireNonNull(animation, "animation");
    Objects.requireNonNull(playback, "playback");
    Objects.requireNonNull(transition, "transition");
    if (entity.level().isClientSide()) {
      return false;
    }

    EasyModelAnimationNetwork.send(
        entity,
        ClientboundEasyModelAnimationPacket.playEntity(
            entity.getId(), animation, playback, transition));
    return true;
  }

  public static boolean playRandomAnimation(
      Entity entity,
      List<EasyModelAnimation> animations,
      EasyModelAnimationPlayback playback,
      EasyModelAnimationTransition transition) {
    Objects.requireNonNull(entity, "entity");
    Objects.requireNonNull(animations, "animations");
    if (animations.isEmpty()) {
      return false;
    }

    return playAnimation(
        entity,
        animations.get(entity.level().getRandom().nextInt(animations.size())),
        playback,
        transition);
  }

  public static boolean playAnimationSequence(Entity entity, EasyModelAnimationSequence sequence) {
    Objects.requireNonNull(entity, "entity");
    Objects.requireNonNull(sequence, "sequence");
    if (entity.level().isClientSide()) {
      return false;
    }

    EasyModelAnimationNetwork.send(
        entity, ClientboundEasyModelAnimationSequencePacket.forEntity(entity.getId(), sequence));
    return true;
  }

  public static boolean stopAnimation(Entity entity, EasyModelAnimationTransition transition) {
    Objects.requireNonNull(transition, "transition");
    return control(entity, EasyModelAnimationPacketOperation.STOP, transition);
  }

  public static boolean restartAnimation(Entity entity) {
    return control(
        entity, EasyModelAnimationPacketOperation.RESTART, EasyModelAnimationTransition.IMMEDIATE);
  }

  private static boolean control(
      Entity entity,
      EasyModelAnimationPacketOperation operation,
      EasyModelAnimationTransition transition) {
    Objects.requireNonNull(entity, "entity");
    if (entity.level().isClientSide()) {
      return false;
    }

    EasyModelAnimationNetwork.send(
        entity,
        ClientboundEasyModelAnimationPacket.controlEntity(entity.getId(), operation, transition));
    return true;
  }

  private static EasyModelProfileInfo profileInfo(EasyModelEntityProfile profile) {
    return new EasyModelProfileInfo(
        profile.id(),
        EasyModelApiMapper.profileType(profile.modelType()),
        EasyModelApiMapper.bodyType(profile.bodyType()),
        EntityDimensions.scalable(profile.width(), profile.height())
            .withEyeHeight(profile.eyeHeight()),
        profile.eyeHeight());
  }
}
