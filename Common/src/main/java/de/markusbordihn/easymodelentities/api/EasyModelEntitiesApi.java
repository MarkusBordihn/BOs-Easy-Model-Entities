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

import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
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

  public static Optional<EasyModelEntityProfile> getProfile(Identifier profileId) {
    return EasyModelServices.profileService()
        .getProfile(Objects.requireNonNull(profileId, "profileId"));
  }

  public static Optional<EntityDimensions> getProfileEntityDimensions(Identifier profileId) {
    return getProfile(profileId)
        .map(
            profile ->
                EntityDimensions.scalable(profile.width(), profile.height())
                    .withEyeHeight(profile.eyeHeight()));
  }

  public static List<EasyModelEntityProfile> listProfiles() {
    return List.copyOf(EasyModelServices.profileService().getActiveProfiles());
  }

  public static List<EasyModelEntityProfile> listProfiles(ModelBodyType bodyType) {
    Objects.requireNonNull(bodyType, "bodyType");
    return List.copyOf(EasyModelServices.profileService().getActiveProfiles(bodyType));
  }

  public static List<Identifier> listProfileIds() {
    return listProfiles().stream().map(EasyModelEntityProfile::id).toList();
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
}
