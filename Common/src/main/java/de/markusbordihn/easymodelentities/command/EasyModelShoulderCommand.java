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

import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.HOST_ENTITY;
import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.TARGETS_ARGUMENT;
import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.entityTargets;
import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.sendEntityResult;
import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.sendNoTargets;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.markusbordihn.easymodelentities.api.EasyModelEntitiesApi;
import de.markusbordihn.easymodelentities.api.data.EasyModelDisplaySettings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class EasyModelShoulderCommand {

  private static final String PLAYER_ARGUMENT = "player";
  private static final String X_ARGUMENT = "x";
  private static final String Y_ARGUMENT = "y";
  private static final String Z_ARGUMENT = "z";

  private EasyModelShoulderCommand() {}

  static ArgumentBuilder<CommandSourceStack, ?> register() {
    return Commands.literal("shoulder")
        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
        .then(
            Commands.literal("sit")
                .then(
                    Commands.literal("entity")
                        .then(
                            entityTargets(HOST_ENTITY)
                                .then(
                                    Commands.argument(PLAYER_ARGUMENT, EntityArgument.player())
                                        .executes(EasyModelShoulderCommand::sit)))))
        .then(offsetCommand());
  }

  private static ArgumentBuilder<CommandSourceStack, ?> offsetCommand() {
    return Commands.literal("offset")
        .then(Commands.literal("set").then(Commands.literal("entity").then(setOffsetTargets())))
        .then(
            Commands.literal("clear")
                .then(
                    Commands.literal("entity")
                        .then(
                            entityTargets(HOST_ENTITY)
                                .executes(EasyModelShoulderCommand::clearOffset))))
        .then(
            Commands.literal("get")
                .then(
                    Commands.literal("entity")
                        .then(
                            entityTargets(HOST_ENTITY)
                                .executes(EasyModelShoulderCommand::getOffset))));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> setOffsetTargets() {
    return entityTargets(HOST_ENTITY)
        .then(
            offsetArgument(X_ARGUMENT)
                .then(
                    offsetArgument(Y_ARGUMENT)
                        .then(
                            offsetArgument(Z_ARGUMENT)
                                .executes(EasyModelShoulderCommand::setOffset))));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> offsetArgument(String name) {
    return Commands.argument(
        name,
        DoubleArgumentType.doubleArg(
            -EasyModelDisplaySettings.MAX_SHOULDER_OFFSET,
            EasyModelDisplaySettings.MAX_SHOULDER_OFFSET));
  }

  private static int sit(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    Player player = EntityArgument.getPlayer(context, PLAYER_ARGUMENT);
    int seated = 0;
    for (Entity entity : EntityArgument.getEntities(context, TARGETS_ARGUMENT)) {
      if (EasyModelEntitiesApi.sitOnShoulder(entity, player)) {
        seated++;
      }
    }
    return sendEntityResult(context.getSource(), "Seated on shoulder", seated);
  }

  private static int setOffset(CommandContext<CommandSourceStack> context)
      throws CommandSyntaxException {
    Vec3 shoulderOffset =
        new Vec3(
            DoubleArgumentType.getDouble(context, X_ARGUMENT),
            DoubleArgumentType.getDouble(context, Y_ARGUMENT),
            DoubleArgumentType.getDouble(context, Z_ARGUMENT));
    int updated = 0;
    for (Entity entity : EntityArgument.getEntities(context, TARGETS_ARGUMENT)) {
      if (EasyModelEntitiesApi.setShoulderOffset(entity, shoulderOffset)) {
        updated++;
      }
    }
    return sendEntityResult(context.getSource(), "Set shoulder offset", updated);
  }

  private static int clearOffset(CommandContext<CommandSourceStack> context)
      throws CommandSyntaxException {
    int updated = 0;
    for (Entity entity : EntityArgument.getEntities(context, TARGETS_ARGUMENT)) {
      if (EasyModelEntitiesApi.clearShoulderOffset(entity)) {
        updated++;
      }
    }
    return sendEntityResult(context.getSource(), "Cleared shoulder offset", updated);
  }

  private static int getOffset(CommandContext<CommandSourceStack> context)
      throws CommandSyntaxException {
    CommandSourceStack source = context.getSource();
    int found = 0;
    for (Entity entity : EntityArgument.getEntities(context, TARGETS_ARGUMENT)) {
      if (!HOST_ENTITY.test(entity)) {
        continue;
      }
      Vec3 shoulderOffset = EasyModelEntitiesApi.getShoulderOffset(entity);
      source.sendSuccess(
          () ->
              Component.literal(
                  entity.getName().getString()
                      + ": shoulder offset "
                      + shoulderOffset.x
                      + " "
                      + shoulderOffset.y
                      + " "
                      + shoulderOffset.z),
          false);
      found++;
    }
    return found == 0 ? sendNoTargets(source) : found;
  }
}
