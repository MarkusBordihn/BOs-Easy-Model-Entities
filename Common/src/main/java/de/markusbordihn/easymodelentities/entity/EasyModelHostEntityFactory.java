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

import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.profile.ModelType;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.registry.ModelEntityTypeIds;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EasyModelHostEntityFactory implements EasyModelEntityFactory {

  private final EasyModelHostEntityTypeProvider entityTypeProvider;

  public EasyModelHostEntityFactory(EasyModelHostEntityTypeProvider entityTypeProvider) {
    this.entityTypeProvider = Objects.requireNonNull(entityTypeProvider, "entityTypeProvider");
  }

  @Override
  public Optional<Entity> createEntity(Level level, ResourceLocation profileId, Vec3 position) {
    Objects.requireNonNull(level, "level");
    Objects.requireNonNull(profileId, "profileId");
    Objects.requireNonNull(position, "position");

    Optional<EasyModelEntityProfile> profile =
        EasyModelServices.profileService()
            .getProfile(profileId)
            .filter(EasyModelEntityProfile::isActive)
            .filter(profileValue -> profileValue.modelType() == ModelType.ENTITY);
    if (profile.isEmpty()) {
      return Optional.empty();
    }

    ResourceLocation hostEntityTypeId = profile.get().hostEntityType();

    if (ModelEntityTypeIds.AQUATIC_ENTITY.equals(hostEntityTypeId)) {
      return createAquaticEntity(level, profileId, position);
    }

    EntityType<?> entityType = entityType(hostEntityTypeId);
    if (entityType == null) {
      return Optional.empty();
    }

    Entity entity = entityType.create(level);
    if (entity instanceof EasyModelEntityHost hostEntity) {
      entity.setPos(position);
      hostEntity.setEasyModelProfileId(profileId);
      return Optional.of(entity);
    }
    return Optional.empty();
  }

  private EntityType<?> entityType(ResourceLocation entityTypeId) {
    if (ModelEntityTypeIds.GROUND_ENTITY.equals(entityTypeId)) {
      return this.entityTypeProvider.groundEntityType();
    }
    if (ModelEntityTypeIds.STATIC_ENTITY.equals(entityTypeId)) {
      return this.entityTypeProvider.staticEntityType();
    }
    if (ModelEntityTypeIds.AMPHIBIOUS_ENTITY.equals(entityTypeId)) {
      return this.entityTypeProvider.amphibiousEntityType();
    }

    return null;
  }

  private Optional<Entity> createAquaticEntity(
      Level level, ResourceLocation profileId, Vec3 position) {
    EntityType<EasyModelAquaticEntity> aquaticType = this.entityTypeProvider.aquaticEntityType();
    EasyModelAquaticEntity entity = aquaticType.create(level);
    if (entity == null) {
      return Optional.empty();
    }
    entity.setPos(position);
    entity.setEasyModelProfileId(profileId);
    return Optional.of(entity);
  }
}
