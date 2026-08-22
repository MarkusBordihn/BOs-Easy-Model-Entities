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

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.markusbordihn.easymodelentities.api.EasyModelRenderable;
import de.markusbordihn.easymodelentities.blockentity.EasyModelHostBlockEntity;
import de.markusbordihn.easymodelentities.entity.EasyModelEntityHost;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

final class EasyModelCommandTargets {

  static final String TARGETS_ARGUMENT = "targets";
  static final String POSITION_ARGUMENT = "pos";
  static final Predicate<Entity> HOST_ENTITY = entity -> entity instanceof EasyModelEntityHost;
  static final Predicate<Entity> RENDERABLE_ENTITY = EasyModelCommandTargets::isRenderableEntity;

  private static final String NO_TARGETS_MESSAGE = "No compatible Easy Model targets selected.";
  private static final String NO_BLOCK_ENTITY_MESSAGE = "No Easy Model block entity at position.";
  private static final String NO_HOST_BLOCK_ENTITY_MESSAGE =
      "No Easy Model host block entity at position.";

  private EasyModelCommandTargets() {}

  static ArgumentBuilder<CommandSourceStack, ?> entityTargets(Predicate<Entity> predicate) {
    return Commands.argument(TARGETS_ARGUMENT, EntityArgument.entities())
        .suggests(
            (context, builder) ->
                EasyModelCommandSuggestions.suggestEntities(context, builder, predicate));
  }

  static ArgumentBuilder<CommandSourceStack, ?> blockPosition() {
    return Commands.argument(POSITION_ARGUMENT, BlockPosArgument.blockPos());
  }

  static ArgumentBuilder<CommandSourceStack, ?> targetBranches(
      ArgumentBuilder<CommandSourceStack, ?> command,
      Predicate<Entity> entityPredicate,
      TargetBranch branch) {
    return command
        .then(Commands.literal("entity").then(branch.build(entityTargets(entityPredicate), false)))
        .then(Commands.literal("block").then(branch.build(blockPosition(), true)));
  }

  static BlockTarget blockTarget(CommandContext<CommandSourceStack> context)
      throws CommandSyntaxException {
    ServerLevel level = context.getSource().getLevel();
    BlockPos blockPos = BlockPosArgument.getLoadedBlockPos(context, POSITION_ARGUMENT);
    return new BlockTarget(level, blockPos, level.getBlockEntity(blockPos));
  }

  static boolean isRenderableEntity(Entity entity) {
    return entity instanceof EasyModelEntityHost || entity instanceof EasyModelRenderable;
  }

  static int updateHosts(
      CommandContext<CommandSourceStack> context,
      boolean blockTarget,
      String action,
      Consumer<EasyModelHostBlockEntity> blockEntityUpdate,
      Consumer<EasyModelEntityHost> entityUpdate)
      throws CommandSyntaxException {
    if (blockTarget) {
      if (!(blockTarget(context).blockEntity()
          instanceof EasyModelHostBlockEntity hostBlockEntity)) {
        return sendMissingHostBlockEntity(context.getSource());
      }

      blockEntityUpdate.accept(hostBlockEntity);
      return sendBlockEntityResult(context.getSource(), action);
    }

    int updated = 0;
    for (Entity entity : EntityArgument.getEntities(context, TARGETS_ARGUMENT)) {
      if (entity instanceof EasyModelEntityHost hostEntity) {
        entityUpdate.accept(hostEntity);
        updated++;
      }
    }
    return sendEntityResult(context.getSource(), action, updated);
  }

  static int updateRenderables(
      CommandContext<CommandSourceStack> context,
      boolean blockTarget,
      String action,
      Consumer<BlockTarget> blockEntityUpdate,
      Consumer<Entity> entityUpdate)
      throws CommandSyntaxException {
    if (blockTarget) {
      BlockTarget target = blockTarget(context);
      if (!(target.blockEntity() instanceof EasyModelRenderable)) {
        return sendMissingBlockEntity(context.getSource());
      }

      blockEntityUpdate.accept(target);
      return sendBlockEntityResult(context.getSource(), action);
    }

    int updated = 0;
    for (Entity entity : EntityArgument.getEntities(context, TARGETS_ARGUMENT)) {
      if (isRenderableEntity(entity)) {
        entityUpdate.accept(entity);
        updated++;
      }
    }
    return sendEntityResult(context.getSource(), action, updated);
  }

  static int getRenderables(
      CommandContext<CommandSourceStack> context,
      boolean blockTarget,
      ToIntFunction<BlockTarget> blockEntityValue,
      ToIntFunction<Entity> entityValue)
      throws CommandSyntaxException {
    if (blockTarget) {
      BlockTarget target = blockTarget(context);
      if (!(target.blockEntity() instanceof EasyModelRenderable)) {
        return sendMissingBlockEntity(context.getSource());
      }

      return blockEntityValue.applyAsInt(target);
    }

    int matched = 0;
    int result = 0;
    for (Entity entity : EntityArgument.getEntities(context, TARGETS_ARGUMENT)) {
      if (!isRenderableEntity(entity)) {
        continue;
      }
      result = entityValue.applyAsInt(entity);
      matched++;
    }
    return matched == 0 ? sendNoTargets(context.getSource()) : result;
  }

  static int sendNoTargets(CommandSourceStack source) {
    source.sendFailure(Component.literal(NO_TARGETS_MESSAGE));
    return 0;
  }

  static int sendEntityResult(CommandSourceStack source, String action, int updated) {
    if (updated == 0) {
      return sendNoTargets(source);
    }

    source.sendSuccess(
        () ->
            Component.literal(
                action + " for " + updated + (updated == 1 ? " entity." : " entities.")),
        true);
    return updated;
  }

  static int sendBlockEntityResult(CommandSourceStack source, String action) {
    source.sendSuccess(() -> Component.literal(action + " for 1 block entity."), true);
    return 1;
  }

  static int sendMissingBlockEntity(CommandSourceStack source) {
    source.sendFailure(Component.literal(NO_BLOCK_ENTITY_MESSAGE));
    return 0;
  }

  static int sendMissingHostBlockEntity(CommandSourceStack source) {
    source.sendFailure(Component.literal(NO_HOST_BLOCK_ENTITY_MESSAGE));
    return 0;
  }

  @FunctionalInterface
  interface TargetBranch {
    ArgumentBuilder<CommandSourceStack, ?> build(
        ArgumentBuilder<CommandSourceStack, ?> target, boolean blockTarget);
  }

  record BlockTarget(ServerLevel level, BlockPos blockPos, BlockEntity blockEntity) {}
}
