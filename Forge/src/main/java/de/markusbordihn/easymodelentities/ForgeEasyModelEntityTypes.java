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

import de.markusbordihn.easymodelentities.entity.EasyModelGroundEntity;
import de.markusbordihn.easymodelentities.entity.EasyModelHostEntity;
import de.markusbordihn.easymodelentities.entity.EasyModelHostEntityTypeProvider;
import de.markusbordihn.easymodelentities.entity.EasyModelStaticEntity;
import de.markusbordihn.easymodelentities.registry.ModelEntityTypeIds;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ForgeEasyModelEntityTypes implements EasyModelHostEntityTypeProvider {

  public static final ForgeEasyModelEntityTypes INSTANCE = new ForgeEasyModelEntityTypes();

  private static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
      DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Constants.MOD_ID);

  private static final RegistryObject<EntityType<EasyModelGroundEntity>> GROUND_ENTITY =
      ENTITY_TYPES.register(
          ModelEntityTypeIds.GROUND_ENTITY.getPath(),
          () ->
              EntityType.Builder.<EasyModelGroundEntity>of(
                      EasyModelGroundEntity::new, MobCategory.CREATURE)
                  .sized(EasyModelHostEntity.FALLBACK_WIDTH, EasyModelHostEntity.FALLBACK_HEIGHT)
                  .clientTrackingRange(10)
                  .build(ModelEntityTypeIds.GROUND_ENTITY.toString()));

  private static final RegistryObject<EntityType<EasyModelStaticEntity>> STATIC_ENTITY =
      ENTITY_TYPES.register(
          ModelEntityTypeIds.STATIC_ENTITY.getPath(),
          () ->
              EntityType.Builder.<EasyModelStaticEntity>of(
                      EasyModelStaticEntity::new, MobCategory.MISC)
                  .sized(EasyModelHostEntity.FALLBACK_WIDTH, EasyModelHostEntity.FALLBACK_HEIGHT)
                  .clientTrackingRange(10)
                  .build(ModelEntityTypeIds.STATIC_ENTITY.toString()));

  private ForgeEasyModelEntityTypes() {}

  public static void register(IEventBus modEventBus) {
    ENTITY_TYPES.register(modEventBus);
    modEventBus.addListener(ForgeEasyModelEntityTypes::registerAttributes);
  }

  private static void registerAttributes(EntityAttributeCreationEvent event) {
    event.put(GROUND_ENTITY.get(), EasyModelHostEntity.createAttributes().build());
    event.put(STATIC_ENTITY.get(), EasyModelHostEntity.createAttributes().build());
  }

  @Override
  public EntityType<EasyModelGroundEntity> groundEntityType() {
    return GROUND_ENTITY.get();
  }

  @Override
  public EntityType<EasyModelStaticEntity> staticEntityType() {
    return STATIC_ENTITY.get();
  }
}
