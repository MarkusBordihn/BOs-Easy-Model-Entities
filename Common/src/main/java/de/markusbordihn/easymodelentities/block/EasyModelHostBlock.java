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

package de.markusbordihn.easymodelentities.block;

import com.mojang.serialization.MapCodec;
import de.markusbordihn.easymodelentities.blockentity.EasyModelAnimatedBlockEntity;
import de.markusbordihn.easymodelentities.blockentity.EasyModelHostBlockEntity;
import de.markusbordihn.easymodelentities.blockentity.EasyModelRandomlyAnimatedBlockEntity;
import de.markusbordihn.easymodelentities.blockentity.EasyModelStaticBlockEntity;
import de.markusbordihn.easymodelentities.blockentity.EasyModelTickingBlockEntity;
import de.markusbordihn.easymodelentities.data.profile.ModelBlockEntityPresetType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class EasyModelHostBlock extends BaseEntityBlock {

  public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

  private final ModelBlockEntityPresetType presetType;

  public EasyModelHostBlock(ModelBlockEntityPresetType presetType, Identifier blockId) {
    super(
        BlockBehaviour.Properties.of()
            .setId(ResourceKey.create(Registries.BLOCK, blockId))
            .strength(1.5f)
            .noOcclusion());
    this.presetType = presetType;
    this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));
  }

  public static BlockState facingAwayFrom(BlockState blockState, float yawDegrees) {
    if (!blockState.hasProperty(FACING)) {
      return blockState;
    }

    return blockState.setValue(FACING, Direction.fromYRot(yawDegrees).getOpposite());
  }

  @SuppressWarnings("unchecked")
  private static <T extends BlockEntity> BlockEntityTicker<T> ticker() {
    return (level, blockPos, blockState, blockEntity) -> {
      if (blockEntity instanceof EasyModelHostBlockEntity hostBlockEntity) {
        if (level.isClientSide()) {
          hostBlockEntity.clientTick(level, blockPos, blockState);
        } else {
          hostBlockEntity.serverTick(level, blockPos, blockState);
        }
      }
    };
  }

  @Override
  public MapCodec<? extends BaseEntityBlock> codec() {
    throw new UnsupportedOperationException(
        "EasyModelHostBlock does not support codec serialization");
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(FACING);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return this.defaultBlockState()
        .setValue(FACING, context.getHorizontalDirection().getOpposite());
  }

  @Override
  public BlockState rotate(BlockState blockState, Rotation rotation) {
    return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
  }

  @Override
  public BlockState mirror(BlockState blockState, Mirror mirror) {
    return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
  }

  @Override
  public RenderShape getRenderShape(BlockState blockState) {
    return RenderShape.INVISIBLE;
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
    return switch (this.presetType) {
      case STATIC -> new EasyModelStaticBlockEntity(blockPos, blockState);
      case TICKING -> new EasyModelTickingBlockEntity(blockPos, blockState);
      case ANIMATED -> new EasyModelAnimatedBlockEntity(blockPos, blockState);
      case ANIMATED_RANDOMLY -> new EasyModelRandomlyAnimatedBlockEntity(blockPos, blockState);
    };
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
    boolean enabled =
        level.isClientSide() ? this.presetType.hasClientTick() : this.presetType.hasServerTick();
    return enabled ? ticker() : null;
  }
}
