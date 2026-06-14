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

import de.markusbordihn.easymodelentities.block.EasyModelHostBlock;
import de.markusbordihn.easymodelentities.blockentity.EasyModelAnimatedBlockEntity;
import de.markusbordihn.easymodelentities.blockentity.EasyModelHostBlockEntityTypeProvider;
import de.markusbordihn.easymodelentities.blockentity.EasyModelRandomlyAnimatedBlockEntity;
import de.markusbordihn.easymodelentities.blockentity.EasyModelStaticBlockEntity;
import de.markusbordihn.easymodelentities.blockentity.EasyModelTickingBlockEntity;
import de.markusbordihn.easymodelentities.data.profile.ModelBlockEntityPresetType;
import de.markusbordihn.easymodelentities.registry.ModelBlockEntityTypeIds;
import de.markusbordihn.easymodelentities.registry.ModelBlockIds;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class FabricEasyModelBlockEntityTypes implements EasyModelHostBlockEntityTypeProvider {

  public static final FabricEasyModelBlockEntityTypes INSTANCE =
      new FabricEasyModelBlockEntityTypes();

  public static final Block STATIC_BLOCK =
      Registry.register(
          BuiltInRegistries.BLOCK,
          ModelBlockIds.STATIC_BLOCK,
          new EasyModelHostBlock(ModelBlockEntityPresetType.STATIC));
  public static final Block TICKING_BLOCK =
      Registry.register(
          BuiltInRegistries.BLOCK,
          ModelBlockIds.TICKING_BLOCK,
          new EasyModelHostBlock(ModelBlockEntityPresetType.TICKING));
  public static final Block ANIMATED_BLOCK =
      Registry.register(
          BuiltInRegistries.BLOCK,
          ModelBlockIds.ANIMATED_BLOCK,
          new EasyModelHostBlock(ModelBlockEntityPresetType.ANIMATED));
  public static final Block ANIMATED_RANDOMLY_BLOCK =
      Registry.register(
          BuiltInRegistries.BLOCK,
          ModelBlockIds.ANIMATED_RANDOMLY_BLOCK,
          new EasyModelHostBlock(ModelBlockEntityPresetType.ANIMATED_RANDOMLY));

  private static final BlockEntityType<EasyModelStaticBlockEntity> STATIC_BLOCK_ENTITY =
      Registry.register(
          BuiltInRegistries.BLOCK_ENTITY_TYPE,
          ModelBlockEntityTypeIds.STATIC_BLOCK_ENTITY,
          BlockEntityType.Builder.of(EasyModelStaticBlockEntity::new, STATIC_BLOCK).build(null));
  private static final BlockEntityType<EasyModelTickingBlockEntity> TICKING_BLOCK_ENTITY =
      Registry.register(
          BuiltInRegistries.BLOCK_ENTITY_TYPE,
          ModelBlockEntityTypeIds.TICKING_BLOCK_ENTITY,
          BlockEntityType.Builder.of(EasyModelTickingBlockEntity::new, TICKING_BLOCK).build(null));
  private static final BlockEntityType<EasyModelAnimatedBlockEntity> ANIMATED_BLOCK_ENTITY =
      Registry.register(
          BuiltInRegistries.BLOCK_ENTITY_TYPE,
          ModelBlockEntityTypeIds.ANIMATED_BLOCK_ENTITY,
          BlockEntityType.Builder.of(EasyModelAnimatedBlockEntity::new, ANIMATED_BLOCK)
              .build(null));
  private static final BlockEntityType<EasyModelRandomlyAnimatedBlockEntity>
      ANIMATED_RANDOMLY_BLOCK_ENTITY =
          Registry.register(
              BuiltInRegistries.BLOCK_ENTITY_TYPE,
              ModelBlockEntityTypeIds.ANIMATED_RANDOMLY_BLOCK_ENTITY,
              BlockEntityType.Builder.of(
                      EasyModelRandomlyAnimatedBlockEntity::new, ANIMATED_RANDOMLY_BLOCK)
                  .build(null));

  private FabricEasyModelBlockEntityTypes() {}

  public static void register() {}

  @Override
  public BlockEntityType<EasyModelStaticBlockEntity> staticBlockEntityType() {
    return STATIC_BLOCK_ENTITY;
  }

  @Override
  public BlockEntityType<EasyModelTickingBlockEntity> tickingBlockEntityType() {
    return TICKING_BLOCK_ENTITY;
  }

  @Override
  public BlockEntityType<EasyModelAnimatedBlockEntity> animatedBlockEntityType() {
    return ANIMATED_BLOCK_ENTITY;
  }

  @Override
  public BlockEntityType<EasyModelRandomlyAnimatedBlockEntity> animatedRandomlyBlockEntityType() {
    return ANIMATED_RANDOMLY_BLOCK_ENTITY;
  }
}
