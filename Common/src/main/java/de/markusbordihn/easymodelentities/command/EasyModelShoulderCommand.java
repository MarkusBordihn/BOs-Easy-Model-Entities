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

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.markusbordihn.easymodelentities.api.EasyModelEntitiesApi;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class EasyModelShoulderCommand {

  private static final String PLAYER_ARGUMENT = "player";

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
                                        .executes(EasyModelShoulderCommand::sit)))));
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
}
