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

package de.markusbordihn.easymodelentities.item;

import de.markusbordihn.easymodelentities.blockentity.EasyModelHostBlockEntity;
import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.spawn.EasyModelSpawnSupport;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class EasyModelBlockSpawnItem extends EasyModelSpawnItem {

  public EasyModelBlockSpawnItem(Properties properties) {
    super(properties);
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    ItemStack stack = context.getItemInHand();
    Optional<ResourceLocation> profileId = EasyModelEntitiesItems.profileId(stack);
    if (profileId.isEmpty()) {
      return InteractionResult.PASS;
    }

    Level level = context.getLevel();
    if (level.isClientSide) {
      return InteractionResult.SUCCESS;
    }

    Optional<String> rejection = EasyModelSpawnSupport.blockPlacementRejection(profileId.get());
    if (rejection.isPresent()) {
      notifyPlayer(context.getPlayer(), rejection.get());
      return InteractionResult.FAIL;
    }

    EasyModelEntityProfile profile =
        EasyModelServices.profileService().getProfile(profileId.get()).orElseThrow();
    ResourceLocation blockId = EasyModelSpawnSupport.hostBlockId(profile).orElseThrow();
    Block block = BuiltInRegistries.BLOCK.get(blockId);
    if (block == Blocks.AIR) {
      notifyPlayer(context.getPlayer(), "Missing Easy Model Entities host block " + blockId + ".");
      return InteractionResult.FAIL;
    }

    BlockPlaceContext placeContext = new BlockPlaceContext(context);
    if (!placeContext.canPlace()) {
      return InteractionResult.FAIL;
    }

    BlockPos placePos = placeContext.getClickedPos();
    BlockState replacedState = level.getBlockState(placePos);
    BlockState blockState = block.getStateForPlacement(placeContext);
    if (blockState == null) {
      blockState = block.defaultBlockState();
    }
    if (!level.setBlock(placePos, blockState, Block.UPDATE_ALL)) {
      return InteractionResult.FAIL;
    }

    if (level.getBlockEntity(placePos) instanceof EasyModelHostBlockEntity hostBlockEntity) {
      hostBlockEntity.setEasyModelProfileId(profileId.get());
    } else {
      level.setBlock(placePos, replacedState, Block.UPDATE_ALL);
      notifyPlayer(
          context.getPlayer(), "Placed block at " + placePos + " without Easy Model BlockEntity.");
      return InteractionResult.FAIL;
    }

    block.setPlacedBy(level, placePos, blockState, context.getPlayer(), stack);
    level.gameEvent(
        GameEvent.BLOCK_PLACE, placePos, GameEvent.Context.of(context.getPlayer(), blockState));

    Player player = context.getPlayer();
    if (player == null || !player.getAbilities().instabuild) {
      stack.shrink(1);
    }
    return InteractionResult.CONSUME;
  }
}
