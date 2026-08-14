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

package de.markusbordihn.easymodelentities.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.markusbordihn.easymodelentities.api.EasyModelRenderable;
import de.markusbordihn.easymodelentities.entity.EasyModelEntityHost;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

final class EasyModelCommandTargets {

  static final String TARGETS_ARGUMENT = "targets";
  static final String POSITION_ARGUMENT = "pos";

  private EasyModelCommandTargets() {}

  static BlockTarget blockTarget(CommandContext<CommandSourceStack> context)
      throws CommandSyntaxException {
    ServerLevel level = context.getSource().getLevel();
    BlockPos blockPos = BlockPosArgument.getLoadedBlockPos(context, POSITION_ARGUMENT);
    return new BlockTarget(level, blockPos, level.getBlockEntity(blockPos));
  }

  static boolean isRenderableEntity(Entity entity) {
    return entity instanceof EasyModelEntityHost || entity instanceof EasyModelRenderable;
  }

  static int sendResult(CommandSourceStack source, String action, int updated, String targetName) {
    if (updated == 0) {
      source.sendFailure(Component.literal("No compatible Easy Model targets selected."));
      return 0;
    }
    source.sendSuccess(
        () -> Component.literal(action + " for " + updated + " " + targetName + "."), true);
    return updated;
  }

  record BlockTarget(ServerLevel level, BlockPos blockPos, BlockEntity blockEntity) {}
}
