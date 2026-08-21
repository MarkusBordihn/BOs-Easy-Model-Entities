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
import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.RENDERABLE_ENTITY;
import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.getRenderables;
import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.targetBranches;
import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.updateHosts;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.markusbordihn.easymodelentities.api.EasyModelBlockEntitiesApi;
import de.markusbordihn.easymodelentities.api.EasyModelEntitiesApi;
import de.markusbordihn.easymodelentities.api.data.EasyModelDisplaySettings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

final class EasyModelDisplayCommand {

  private static final String OPACITY_ARGUMENT = "opacity";
  private static final String LIGHT_LEVEL_ARGUMENT = "level";
  private static final int MIN_OPACITY_PERCENT = 0;
  private static final int MAX_OPACITY_PERCENT = 100;

  private EasyModelDisplayCommand() {}

  static ArgumentBuilder<CommandSourceStack, ?> register() {
    return Commands.literal("display")
        .requires(source -> source.hasPermission(2))
        .then(opacityCommand())
        .then(lightCommand());
  }

  private static ArgumentBuilder<CommandSourceStack, ?> opacityCommand() {
    return Commands.literal("opacity")
        .then(
            targetBranches(
                Commands.literal("set"),
                HOST_ENTITY,
                (target, blockTarget) ->
                    target.then(
                        opacityArgument().executes(context -> setOpacity(context, blockTarget)))))
        .then(
            targetBranches(
                Commands.literal("clear"),
                HOST_ENTITY,
                (target, blockTarget) ->
                    target.executes(context -> clearOpacity(context, blockTarget))))
        .then(
            targetBranches(
                Commands.literal("get"),
                RENDERABLE_ENTITY,
                (target, blockTarget) ->
                    target.executes(context -> getOpacity(context, blockTarget))));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> lightCommand() {
    return Commands.literal("light")
        .then(
            targetBranches(
                Commands.literal("set"),
                HOST_ENTITY,
                (target, blockTarget) ->
                    target.then(
                        lightLevelArgument()
                            .executes(context -> setLightLevel(context, blockTarget)))))
        .then(
            targetBranches(
                Commands.literal("clear"),
                HOST_ENTITY,
                (target, blockTarget) ->
                    target.executes(context -> clearLightLevel(context, blockTarget))))
        .then(
            targetBranches(
                Commands.literal("get"),
                RENDERABLE_ENTITY,
                (target, blockTarget) ->
                    target.executes(context -> getLightLevel(context, blockTarget))));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> opacityArgument() {
    return Commands.argument(
        OPACITY_ARGUMENT, IntegerArgumentType.integer(MIN_OPACITY_PERCENT, MAX_OPACITY_PERCENT));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> lightLevelArgument() {
    return Commands.argument(
        LIGHT_LEVEL_ARGUMENT,
        IntegerArgumentType.integer(
            EasyModelDisplaySettings.MIN_LIGHT_LEVEL, EasyModelDisplaySettings.MAX_LIGHT_LEVEL));
  }

  private static int setOpacity(CommandContext<CommandSourceStack> context, boolean blockTarget)
      throws CommandSyntaxException {
    int percent = IntegerArgumentType.getInteger(context, OPACITY_ARGUMENT);
    return applyOpacity(context, blockTarget, percent / (float) MAX_OPACITY_PERCENT, "Set opacity");
  }

  private static int clearOpacity(CommandContext<CommandSourceStack> context, boolean blockTarget)
      throws CommandSyntaxException {
    return applyOpacity(
        context, blockTarget, EasyModelDisplaySettings.NO_OPACITY, "Cleared opacity");
  }

  private static int applyOpacity(
      CommandContext<CommandSourceStack> context, boolean blockTarget, float opacity, String action)
      throws CommandSyntaxException {
    return updateHosts(
        context,
        blockTarget,
        action,
        hostBlockEntity -> hostBlockEntity.setEasyModelOpacity(opacity),
        hostEntity -> hostEntity.setEasyModelOpacity(opacity));
  }

  private static int setLightLevel(CommandContext<CommandSourceStack> context, boolean blockTarget)
      throws CommandSyntaxException {
    return applyLightLevel(
        context,
        blockTarget,
        IntegerArgumentType.getInteger(context, LIGHT_LEVEL_ARGUMENT),
        "Set light level");
  }

  private static int clearLightLevel(
      CommandContext<CommandSourceStack> context, boolean blockTarget)
      throws CommandSyntaxException {
    return applyLightLevel(
        context, blockTarget, EasyModelDisplaySettings.NO_LIGHT_LEVEL, "Cleared light level");
  }

  private static int applyLightLevel(
      CommandContext<CommandSourceStack> context,
      boolean blockTarget,
      int lightLevel,
      String action)
      throws CommandSyntaxException {
    return updateHosts(
        context,
        blockTarget,
        action,
        hostBlockEntity -> hostBlockEntity.setEasyModelLightLevel(lightLevel),
        hostEntity -> hostEntity.setEasyModelLightLevel(lightLevel));
  }

  private static int getOpacity(CommandContext<CommandSourceStack> context, boolean blockTarget)
      throws CommandSyntaxException {
    CommandSourceStack source = context.getSource();
    return getRenderables(
        context,
        blockTarget,
        target ->
            sendOpacityLine(
                source,
                target.blockPos().toShortString(),
                EasyModelBlockEntitiesApi.getOpacity(target.blockEntity())),
        entity ->
            sendOpacityLine(
                source, entity.getName().getString(), EasyModelEntitiesApi.getOpacity(entity)));
  }

  private static int getLightLevel(CommandContext<CommandSourceStack> context, boolean blockTarget)
      throws CommandSyntaxException {
    CommandSourceStack source = context.getSource();
    return getRenderables(
        context,
        blockTarget,
        target ->
            sendLightLevelLine(
                source,
                target.blockPos().toShortString(),
                EasyModelBlockEntitiesApi.getLightLevel(target.blockEntity())),
        entity ->
            sendLightLevelLine(
                source, entity.getName().getString(), EasyModelEntitiesApi.getLightLevel(entity)));
  }

  private static int sendOpacityLine(CommandSourceStack source, String targetName, float opacity) {
    if (!EasyModelDisplaySettings.hasOpacityOverride(opacity)) {
      source.sendSuccess(() -> Component.literal(targetName + ": no opacity override"), false);
      return MAX_OPACITY_PERCENT;
    }

    int percent = Math.round(opacity * MAX_OPACITY_PERCENT);
    source.sendSuccess(() -> Component.literal(targetName + ": opacity " + percent + "%"), false);
    return percent;
  }

  private static int sendLightLevelLine(
      CommandSourceStack source, String targetName, int lightLevel) {
    if (lightLevel <= EasyModelDisplaySettings.NO_LIGHT_LEVEL) {
      source.sendSuccess(() -> Component.literal(targetName + ": no light level override"), false);
      return 0;
    }

    source.sendSuccess(() -> Component.literal(targetName + ": light level " + lightLevel), false);
    return lightLevel;
  }
}
