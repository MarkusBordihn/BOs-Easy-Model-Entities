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
import de.markusbordihn.easymodelentities.entity.EasyModelEntityHost;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public final class EasyModelBehaviorCommand {

  private static final String TARGETS_ARGUMENT = "targets";

  private EasyModelBehaviorCommand() {}

  static ArgumentBuilder<CommandSourceStack, ?> register() {
    return Commands.literal("behavior")
        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
        .then(
            Commands.literal("set")
                .then(
                    Commands.literal("entity")
                        .then(
                            Commands.argument(TARGETS_ARGUMENT, EntityArgument.entities())
                                .suggests(
                                    (context, builder) ->
                                        EasyModelCommandSuggestions.suggestEntities(
                                            context,
                                            builder,
                                            entity -> entity instanceof EasyModelEntityHost))
                                .then(
                                    Commands.literal("auto")
                                        .executes(context -> set(context, false)))
                                .then(
                                    Commands.literal("frozen")
                                        .executes(context -> set(context, true))))));
  }

  public static void setFrozen(Mob hostEntity, boolean frozen) {
    hostEntity.setNoAi(frozen);
    if (!frozen) {
      return;
    }

    hostEntity.getNavigation().stop();
    hostEntity.setDeltaMovement(Vec3.ZERO);
    hostEntity.setXxa(0.0f);
    hostEntity.setYya(0.0f);
    hostEntity.setZza(0.0f);
  }

  private static int set(CommandContext<CommandSourceStack> context, boolean frozen)
      throws CommandSyntaxException {
    int updatedEntities = 0;
    for (Entity entity : EntityArgument.getEntities(context, TARGETS_ARGUMENT)) {
      if (entity instanceof EasyModelEntityHost && entity instanceof Mob hostEntity) {
        setFrozen(hostEntity, frozen);
        updatedEntities++;
      }
    }

    CommandSourceStack source = context.getSource();
    if (updatedEntities == 0) {
      source.sendFailure(Component.literal("No Easy Model host entities in the selection."));
      return 0;
    }

    int updateCount = updatedEntities;
    source.sendSuccess(
        () ->
            Component.literal(
                (frozen ? "Froze " : "Restored profile behavior for ")
                    + updateCount
                    + (updateCount == 1 ? " entity." : " entities.")),
        true);
    return updatedEntities;
  }
}
