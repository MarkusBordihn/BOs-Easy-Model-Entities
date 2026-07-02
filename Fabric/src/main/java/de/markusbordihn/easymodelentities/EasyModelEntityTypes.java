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

package de.markusbordihn.easymodelentities;

import de.markusbordihn.easymodelentities.entity.EasyModelAmphibiousEntity;
import de.markusbordihn.easymodelentities.entity.EasyModelAquaticEntity;
import de.markusbordihn.easymodelentities.entity.EasyModelGroundEntity;
import de.markusbordihn.easymodelentities.entity.EasyModelHostEntity;
import de.markusbordihn.easymodelentities.entity.EasyModelHostEntityTypeProvider;
import de.markusbordihn.easymodelentities.entity.EasyModelStaticEntity;
import de.markusbordihn.easymodelentities.entity.EasyModelWaterHostEntity;
import de.markusbordihn.easymodelentities.registry.ModelEntityTypeIds;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class EasyModelEntityTypes implements EasyModelHostEntityTypeProvider {

  public static final EasyModelEntityTypes INSTANCE = new EasyModelEntityTypes();

  private static final EntityType<EasyModelGroundEntity> GROUND_ENTITY =
      Registry.register(
          BuiltInRegistries.ENTITY_TYPE,
          ModelEntityTypeIds.GROUND_ENTITY,
          EntityType.Builder.<EasyModelGroundEntity>of(
                  EasyModelGroundEntity::new, MobCategory.CREATURE)
              .sized(EasyModelHostEntity.FALLBACK_WIDTH, EasyModelHostEntity.FALLBACK_HEIGHT)
              .clientTrackingRange(10)
              .build(ResourceKey.create(Registries.ENTITY_TYPE, ModelEntityTypeIds.GROUND_ENTITY)));

  private static final EntityType<EasyModelStaticEntity> STATIC_ENTITY =
      Registry.register(
          BuiltInRegistries.ENTITY_TYPE,
          ModelEntityTypeIds.STATIC_ENTITY,
          EntityType.Builder.<EasyModelStaticEntity>of(EasyModelStaticEntity::new, MobCategory.MISC)
              .sized(EasyModelHostEntity.FALLBACK_WIDTH, EasyModelHostEntity.FALLBACK_HEIGHT)
              .clientTrackingRange(10)
              .build(ResourceKey.create(Registries.ENTITY_TYPE, ModelEntityTypeIds.STATIC_ENTITY)));

  private static final EntityType<EasyModelAquaticEntity> AQUATIC_ENTITY =
      Registry.register(
          BuiltInRegistries.ENTITY_TYPE,
          ModelEntityTypeIds.AQUATIC_ENTITY,
          EntityType.Builder.<EasyModelAquaticEntity>of(
                  EasyModelAquaticEntity::new, MobCategory.WATER_CREATURE)
              .sized(0.7f, 0.4f)
              .clientTrackingRange(10)
              .build(
                  ResourceKey.create(Registries.ENTITY_TYPE, ModelEntityTypeIds.AQUATIC_ENTITY)));

  private static final EntityType<EasyModelAmphibiousEntity> AMPHIBIOUS_ENTITY =
      Registry.register(
          BuiltInRegistries.ENTITY_TYPE,
          ModelEntityTypeIds.AMPHIBIOUS_ENTITY,
          EntityType.Builder.<EasyModelAmphibiousEntity>of(
                  EasyModelAmphibiousEntity::new, MobCategory.CREATURE)
              .sized(0.9f, 0.6f)
              .clientTrackingRange(10)
              .build(
                  ResourceKey.create(
                      Registries.ENTITY_TYPE, ModelEntityTypeIds.AMPHIBIOUS_ENTITY)));

  private EasyModelEntityTypes() {}

  public static void register() {
    FabricDefaultAttributeRegistry.register(GROUND_ENTITY, EasyModelHostEntity.createAttributes());
    FabricDefaultAttributeRegistry.register(STATIC_ENTITY, EasyModelHostEntity.createAttributes());
    FabricDefaultAttributeRegistry.register(
        AQUATIC_ENTITY, EasyModelWaterHostEntity.createAttributes());
    FabricDefaultAttributeRegistry.register(
        AMPHIBIOUS_ENTITY, EasyModelHostEntity.createAttributes());
  }

  @Override
  public EntityType<EasyModelGroundEntity> groundEntityType() {
    return GROUND_ENTITY;
  }

  @Override
  public EntityType<EasyModelStaticEntity> staticEntityType() {
    return STATIC_ENTITY;
  }

  @Override
  public EntityType<EasyModelAquaticEntity> aquaticEntityType() {
    return AQUATIC_ENTITY;
  }

  @Override
  public EntityType<EasyModelAmphibiousEntity> amphibiousEntityType() {
    return AMPHIBIOUS_ENTITY;
  }
}
