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
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.FORGE)
public final class EasyModelEntityTypes implements EasyModelHostEntityTypeProvider {

  public static final EasyModelEntityTypes INSTANCE = new EasyModelEntityTypes();

  private static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
      DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Constants.MOD_ID);

  private static final RegistryObject<EntityType<EasyModelGroundEntity>> GROUND_ENTITY =
      ENTITY_TYPES.register(
          ModelEntityTypeIds.GROUND_ENTITY.getPath(),
          () ->
              EntityType.Builder.of(EasyModelGroundEntity::new, MobCategory.CREATURE)
                  .sized(EasyModelHostEntity.FALLBACK_WIDTH, EasyModelHostEntity.FALLBACK_HEIGHT)
                  .clientTrackingRange(10)
                  .build(
                      ResourceKey.create(
                          Registries.ENTITY_TYPE, ModelEntityTypeIds.GROUND_ENTITY)));

  private static final RegistryObject<EntityType<EasyModelStaticEntity>> STATIC_ENTITY =
      ENTITY_TYPES.register(
          ModelEntityTypeIds.STATIC_ENTITY.getPath(),
          () ->
              EntityType.Builder.of(EasyModelStaticEntity::new, MobCategory.MISC)
                  .sized(EasyModelHostEntity.FALLBACK_WIDTH, EasyModelHostEntity.FALLBACK_HEIGHT)
                  .clientTrackingRange(10)
                  .build(
                      ResourceKey.create(
                          Registries.ENTITY_TYPE, ModelEntityTypeIds.STATIC_ENTITY)));

  private static final RegistryObject<EntityType<EasyModelAquaticEntity>> AQUATIC_ENTITY =
      ENTITY_TYPES.register(
          ModelEntityTypeIds.AQUATIC_ENTITY.getPath(),
          () ->
              EntityType.Builder.of(EasyModelAquaticEntity::new, MobCategory.WATER_CREATURE)
                  .sized(0.7f, 0.4f)
                  .clientTrackingRange(10)
                  .build(
                      ResourceKey.create(
                          Registries.ENTITY_TYPE, ModelEntityTypeIds.AQUATIC_ENTITY)));

  private static final RegistryObject<EntityType<EasyModelAmphibiousEntity>> AMPHIBIOUS_ENTITY =
      ENTITY_TYPES.register(
          ModelEntityTypeIds.AMPHIBIOUS_ENTITY.getPath(),
          () ->
              EntityType.Builder.of(EasyModelAmphibiousEntity::new, MobCategory.CREATURE)
                  .sized(0.9f, 0.6f)
                  .clientTrackingRange(10)
                  .build(
                      ResourceKey.create(
                          Registries.ENTITY_TYPE, ModelEntityTypeIds.AMPHIBIOUS_ENTITY)));

  private EasyModelEntityTypes() {}

  public static void register(BusGroup modBusGroup) {
    ENTITY_TYPES.register(modBusGroup);
  }

  @SubscribeEvent
  public static void registerAttributes(EntityAttributeCreationEvent event) {
    event.put(GROUND_ENTITY.get(), EasyModelHostEntity.createAttributes().build());
    event.put(STATIC_ENTITY.get(), EasyModelHostEntity.createAttributes().build());
    event.put(AQUATIC_ENTITY.get(), EasyModelWaterHostEntity.createAttributes().build());
    event.put(AMPHIBIOUS_ENTITY.get(), EasyModelHostEntity.createAttributes().build());
  }

  @Override
  public EntityType<EasyModelGroundEntity> groundEntityType() {
    return GROUND_ENTITY.get();
  }

  @Override
  public EntityType<EasyModelStaticEntity> staticEntityType() {
    return STATIC_ENTITY.get();
  }

  @Override
  public EntityType<EasyModelAquaticEntity> aquaticEntityType() {
    return AQUATIC_ENTITY.get();
  }

  @Override
  public EntityType<EasyModelAmphibiousEntity> amphibiousEntityType() {
    return AMPHIBIOUS_ENTITY.get();
  }
}
