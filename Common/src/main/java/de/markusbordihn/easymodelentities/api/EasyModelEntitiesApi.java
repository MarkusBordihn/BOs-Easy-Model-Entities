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

import de.markusbordihn.easymodelentities.api.data.EasyModelBodyType;
import de.markusbordihn.easymodelentities.api.data.EasyModelDisplaySettings;
import de.markusbordihn.easymodelentities.api.data.EasyModelProfileInfo;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSetting;
import de.markusbordihn.easymodelentities.data.EasyModelApiMapper;
import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.entity.EasyModelEntityHost;
import de.markusbordihn.easymodelentities.entity.EasyModelHostEntity;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.Level;
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
    if (entity instanceof EasyModelHostEntity hostEntity) {
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

    return false;
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

    return false;
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
