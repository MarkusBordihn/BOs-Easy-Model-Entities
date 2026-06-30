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

import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.spawn.EasyModelSpawnSupport;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EasyModelEntitySpawnItem extends EasyModelSpawnItem {

  public EasyModelEntitySpawnItem(Properties properties) {
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

    Optional<String> rejection = EasyModelSpawnSupport.entitySpawnRejection(profileId.get());
    if (rejection.isPresent()) {
      notifyPlayer(context.getPlayer(), rejection.get());
      return InteractionResult.FAIL;
    }

    BlockPos clickedPos = context.getClickedPos();
    BlockPos spawnPos =
        level.getBlockState(clickedPos).getCollisionShape(level, clickedPos).isEmpty()
            ? clickedPos
            : clickedPos.relative(context.getClickedFace());
    Vec3 position = new Vec3(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);

    Optional<Entity> entity =
        EasyModelServices.entityFactory().createEntity(level, profileId.get(), position);
    if (entity.isEmpty()) {
      notifyPlayer(
          context.getPlayer(),
          "Could not create Easy Model Entities host entity for " + profileId.get() + ".");
      return InteractionResult.FAIL;
    }

    level.addFreshEntity(entity.get());
    Player player = context.getPlayer();
    if (player == null || !player.getAbilities().instabuild) {
      stack.shrink(1);
    }
    return InteractionResult.CONSUME;
  }
}
