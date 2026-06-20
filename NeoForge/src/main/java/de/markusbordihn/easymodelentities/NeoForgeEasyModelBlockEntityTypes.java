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
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoForgeEasyModelBlockEntityTypes
    implements EasyModelHostBlockEntityTypeProvider {

  public static final NeoForgeEasyModelBlockEntityTypes INSTANCE =
      new NeoForgeEasyModelBlockEntityTypes();

  private static final DeferredRegister.Blocks BLOCKS =
      DeferredRegister.createBlocks(Constants.MOD_ID);
  private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
      DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);

  private static final DeferredBlock<Block> STATIC_BLOCK =
      BLOCKS.register(
          ModelBlockIds.STATIC_BLOCK.getPath(),
          () ->
              new EasyModelHostBlock(
                  ModelBlockEntityPresetType.STATIC, ModelBlockIds.STATIC_BLOCK));
  private static final DeferredHolder<
          BlockEntityType<?>, BlockEntityType<EasyModelStaticBlockEntity>>
      STATIC_BLOCK_ENTITY =
          BLOCK_ENTITY_TYPES.register(
              ModelBlockEntityTypeIds.STATIC_BLOCK_ENTITY.getPath(),
              () -> new BlockEntityType<>(EasyModelStaticBlockEntity::new, STATIC_BLOCK.get()));

  private static final DeferredBlock<Block> TICKING_BLOCK =
      BLOCKS.register(
          ModelBlockIds.TICKING_BLOCK.getPath(),
          () ->
              new EasyModelHostBlock(
                  ModelBlockEntityPresetType.TICKING, ModelBlockIds.TICKING_BLOCK));
  private static final DeferredHolder<
          BlockEntityType<?>, BlockEntityType<EasyModelTickingBlockEntity>>
      TICKING_BLOCK_ENTITY =
          BLOCK_ENTITY_TYPES.register(
              ModelBlockEntityTypeIds.TICKING_BLOCK_ENTITY.getPath(),
              () -> new BlockEntityType<>(EasyModelTickingBlockEntity::new, TICKING_BLOCK.get()));

  private static final DeferredBlock<Block> ANIMATED_BLOCK =
      BLOCKS.register(
          ModelBlockIds.ANIMATED_BLOCK.getPath(),
          () ->
              new EasyModelHostBlock(
                  ModelBlockEntityPresetType.ANIMATED, ModelBlockIds.ANIMATED_BLOCK));
  private static final DeferredHolder<
          BlockEntityType<?>, BlockEntityType<EasyModelAnimatedBlockEntity>>
      ANIMATED_BLOCK_ENTITY =
          BLOCK_ENTITY_TYPES.register(
              ModelBlockEntityTypeIds.ANIMATED_BLOCK_ENTITY.getPath(),
              () -> new BlockEntityType<>(EasyModelAnimatedBlockEntity::new, ANIMATED_BLOCK.get()));

  private static final DeferredBlock<Block> ANIMATED_RANDOMLY_BLOCK =
      BLOCKS.register(
          ModelBlockIds.ANIMATED_RANDOMLY_BLOCK.getPath(),
          () ->
              new EasyModelHostBlock(
                  ModelBlockEntityPresetType.ANIMATED_RANDOMLY,
                  ModelBlockIds.ANIMATED_RANDOMLY_BLOCK));
  private static final DeferredHolder<
          BlockEntityType<?>, BlockEntityType<EasyModelRandomlyAnimatedBlockEntity>>
      ANIMATED_RANDOMLY_BLOCK_ENTITY =
          BLOCK_ENTITY_TYPES.register(
              ModelBlockEntityTypeIds.ANIMATED_RANDOMLY_BLOCK_ENTITY.getPath(),
              () ->
                  new BlockEntityType<>(
                      EasyModelRandomlyAnimatedBlockEntity::new, ANIMATED_RANDOMLY_BLOCK.get()));

  private NeoForgeEasyModelBlockEntityTypes() {}

  public static void register(IEventBus modEventBus) {
    BLOCKS.register(modEventBus);
    BLOCK_ENTITY_TYPES.register(modEventBus);
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
