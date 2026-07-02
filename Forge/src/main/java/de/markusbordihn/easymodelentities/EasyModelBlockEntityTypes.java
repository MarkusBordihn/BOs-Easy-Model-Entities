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
import java.util.Set;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class EasyModelBlockEntityTypes implements EasyModelHostBlockEntityTypeProvider {

  public static final EasyModelBlockEntityTypes INSTANCE = new EasyModelBlockEntityTypes();

  private static final DeferredRegister<Block> BLOCKS =
      DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.MOD_ID);
  private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
      DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Constants.MOD_ID);

  private static final RegistryObject<Block> STATIC_BLOCK =
      BLOCKS.register(
          ModelBlockIds.STATIC_BLOCK.getPath(),
          () ->
              new EasyModelHostBlock(
                  ModelBlockEntityPresetType.STATIC, ModelBlockIds.STATIC_BLOCK));
  private static final RegistryObject<BlockEntityType<EasyModelStaticBlockEntity>>
      STATIC_BLOCK_ENTITY =
          BLOCK_ENTITY_TYPES.register(
              ModelBlockEntityTypeIds.STATIC_BLOCK_ENTITY.getPath(),
              () ->
                  new BlockEntityType<>(
                      EasyModelStaticBlockEntity::new, Set.of(STATIC_BLOCK.get())));
  private static final RegistryObject<Block> TICKING_BLOCK =
      BLOCKS.register(
          ModelBlockIds.TICKING_BLOCK.getPath(),
          () ->
              new EasyModelHostBlock(
                  ModelBlockEntityPresetType.TICKING, ModelBlockIds.TICKING_BLOCK));
  private static final RegistryObject<BlockEntityType<EasyModelTickingBlockEntity>>
      TICKING_BLOCK_ENTITY =
          BLOCK_ENTITY_TYPES.register(
              ModelBlockEntityTypeIds.TICKING_BLOCK_ENTITY.getPath(),
              () ->
                  new BlockEntityType<>(
                      EasyModelTickingBlockEntity::new, Set.of(TICKING_BLOCK.get())));
  private static final RegistryObject<Block> ANIMATED_BLOCK =
      BLOCKS.register(
          ModelBlockIds.ANIMATED_BLOCK.getPath(),
          () ->
              new EasyModelHostBlock(
                  ModelBlockEntityPresetType.ANIMATED, ModelBlockIds.ANIMATED_BLOCK));
  private static final RegistryObject<BlockEntityType<EasyModelAnimatedBlockEntity>>
      ANIMATED_BLOCK_ENTITY =
          BLOCK_ENTITY_TYPES.register(
              ModelBlockEntityTypeIds.ANIMATED_BLOCK_ENTITY.getPath(),
              () ->
                  new BlockEntityType<>(
                      EasyModelAnimatedBlockEntity::new, Set.of(ANIMATED_BLOCK.get())));
  private static final RegistryObject<Block> ANIMATED_RANDOMLY_BLOCK =
      BLOCKS.register(
          ModelBlockIds.ANIMATED_RANDOMLY_BLOCK.getPath(),
          () ->
              new EasyModelHostBlock(
                  ModelBlockEntityPresetType.ANIMATED_RANDOMLY,
                  ModelBlockIds.ANIMATED_RANDOMLY_BLOCK));
  private static final RegistryObject<BlockEntityType<EasyModelRandomlyAnimatedBlockEntity>>
      ANIMATED_RANDOMLY_BLOCK_ENTITY =
          BLOCK_ENTITY_TYPES.register(
              ModelBlockEntityTypeIds.ANIMATED_RANDOMLY_BLOCK_ENTITY.getPath(),
              () ->
                  new BlockEntityType<>(
                      EasyModelRandomlyAnimatedBlockEntity::new,
                      Set.of(ANIMATED_RANDOMLY_BLOCK.get())));

  private EasyModelBlockEntityTypes() {}

  public static void register(BusGroup modBusGroup) {
    BLOCKS.register(modBusGroup);
    BLOCK_ENTITY_TYPES.register(modBusGroup);
  }

  @Override
  public BlockEntityType<EasyModelStaticBlockEntity> staticBlockEntityType() {
    return STATIC_BLOCK_ENTITY.get();
  }

  @Override
  public BlockEntityType<EasyModelTickingBlockEntity> tickingBlockEntityType() {
    return TICKING_BLOCK_ENTITY.get();
  }

  @Override
  public BlockEntityType<EasyModelAnimatedBlockEntity> animatedBlockEntityType() {
    return ANIMATED_BLOCK_ENTITY.get();
  }

  @Override
  public BlockEntityType<EasyModelRandomlyAnimatedBlockEntity> animatedRandomlyBlockEntityType() {
    return ANIMATED_RANDOMLY_BLOCK_ENTITY.get();
  }
}
