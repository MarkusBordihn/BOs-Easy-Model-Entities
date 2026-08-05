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

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.markusbordihn.easymodelentities.api.EasyModelRenderable;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationLoop;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationSetting;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationPlayback;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationPlaybackMode;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationSwitchTiming;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationTransition;
import de.markusbordihn.easymodelentities.blockentity.EasyModelHostBlockEntity;
import de.markusbordihn.easymodelentities.entity.EasyModelEntityHost;
import de.markusbordihn.easymodelentities.network.animation.ClientboundEasyModelAnimationPacket;
import de.markusbordihn.easymodelentities.network.animation.EasyModelAnimationNetwork;
import de.markusbordihn.easymodelentities.network.animation.EasyModelAnimationPacketOperation;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

final class EasyModelAnimationCommand {

  private static final String TARGETS_ARGUMENT = "targets";
  private static final String POSITION_ARGUMENT = "pos";
  private static final String ANIMATION_ARGUMENT = "animation";
  private static final String STATE_ARGUMENT = "state";
  private static final String REPEAT_COUNT_ARGUMENT = "count";
  private static final String BLEND_TICKS_ARGUMENT = "blend_ticks";
  private static final String DURATION_TICKS_ARGUMENT = "duration_ticks";

  private EasyModelAnimationCommand() {}

  static ArgumentBuilder<CommandSourceStack, ?> register() {
    return Commands.literal("animation")
        .requires(source -> source.hasPermission(2))
        .then(playCommand())
        .then(stopCommand())
        .then(restartCommand())
        .then(setCommand());
  }

  private static ArgumentBuilder<CommandSourceStack, ?> playCommand() {
    return Commands.literal("play")
        .then(
            Commands.literal("entity")
                .then(
                    Commands.argument(TARGETS_ARGUMENT, EntityArgument.entities())
                        .suggests(
                            (context, builder) ->
                                EasyModelCommandSuggestions.suggestEntities(
                                    context,
                                    builder,
                                    EasyModelAnimationCommand::isRenderableEntity))
                        .then(playAnimationArgument(false))))
        .then(
            Commands.literal("block")
                .then(
                    Commands.argument(POSITION_ARGUMENT, BlockPosArgument.blockPos())
                        .then(playAnimationArgument(true))));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> playAnimationArgument(boolean blockTarget) {
    return Commands.argument(ANIMATION_ARGUMENT, StringArgumentType.string())
        .suggests(
            (context, builder) ->
                SharedSuggestionProvider.suggest(
                    EasyModelAnimation.standardStates().stream()
                        .map(EasyModelAnimation::serializedName),
                    builder))
        .executes(
            context ->
                play(
                    context,
                    blockTarget,
                    EasyModelAnimationPlayback.DEFAULT,
                    EasyModelAnimationTransition.DEFAULT))
        .then(playMode("once", EasyModelAnimationPlaybackMode.ONCE, blockTarget))
        .then(playMode("loop", EasyModelAnimationPlaybackMode.LOOP, blockTarget))
        .then(
            Commands.literal("repeat")
                .then(
                    Commands.argument(REPEAT_COUNT_ARGUMENT, IntegerArgumentType.integer(1))
                        .executes(
                            context ->
                                play(
                                    context,
                                    blockTarget,
                                    playback(context, EasyModelAnimationPlaybackMode.REPEAT, 0.0f),
                                    EasyModelAnimationTransition.DEFAULT))
                        .then(
                            timingBranch(
                                "immediate",
                                EasyModelAnimationSwitchTiming.IMMEDIATE,
                                EasyModelAnimationPlaybackMode.REPEAT,
                                blockTarget))
                        .then(
                            timingBranch(
                                "after_current",
                                EasyModelAnimationSwitchTiming.AFTER_CURRENT,
                                EasyModelAnimationPlaybackMode.REPEAT,
                                blockTarget))));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> playMode(
      String literal, EasyModelAnimationPlaybackMode mode, boolean blockTarget) {
    return Commands.literal(literal)
        .executes(
            context ->
                play(
                    context,
                    blockTarget,
                    playback(context, mode, 0.0f),
                    EasyModelAnimationTransition.DEFAULT))
        .then(
            timingBranch("immediate", EasyModelAnimationSwitchTiming.IMMEDIATE, mode, blockTarget))
        .then(
            timingBranch(
                "after_current", EasyModelAnimationSwitchTiming.AFTER_CURRENT, mode, blockTarget));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> timingBranch(
      String literal,
      EasyModelAnimationSwitchTiming timing,
      EasyModelAnimationPlaybackMode mode,
      boolean blockTarget) {
    return Commands.literal(literal)
        .executes(
            context ->
                play(
                    context,
                    blockTarget,
                    playback(context, mode, 0.0f),
                    new EasyModelAnimationTransition(
                        timing, EasyModelAnimationTransition.DEFAULT_BLEND_DURATION_TICKS)))
        .then(
            Commands.argument(BLEND_TICKS_ARGUMENT, FloatArgumentType.floatArg(0.0f))
                .executes(
                    context ->
                        play(
                            context,
                            blockTarget,
                            playback(context, mode, 0.0f),
                            transition(context, timing)))
                .then(
                    Commands.argument(DURATION_TICKS_ARGUMENT, FloatArgumentType.floatArg(0.0f))
                        .executes(
                            context ->
                                play(
                                    context,
                                    blockTarget,
                                    playback(
                                        context,
                                        mode,
                                        FloatArgumentType.getFloat(
                                            context, DURATION_TICKS_ARGUMENT)),
                                    transition(context, timing)))));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> stopCommand() {
    return Commands.literal("stop")
        .then(
            Commands.literal("entity")
                .then(
                    Commands.argument(TARGETS_ARGUMENT, EntityArgument.entities())
                        .suggests(
                            (context, builder) ->
                                EasyModelCommandSuggestions.suggestEntities(
                                    context,
                                    builder,
                                    EasyModelAnimationCommand::isRenderableEntity))
                        .executes(
                            context -> stop(context, false, EasyModelAnimationTransition.IMMEDIATE))
                        .then(
                            stopTimingBranch(
                                "immediate", EasyModelAnimationSwitchTiming.IMMEDIATE, false))
                        .then(
                            stopTimingBranch(
                                "after_current",
                                EasyModelAnimationSwitchTiming.AFTER_CURRENT,
                                false))))
        .then(
            Commands.literal("block")
                .then(
                    Commands.argument(POSITION_ARGUMENT, BlockPosArgument.blockPos())
                        .executes(
                            context -> stop(context, true, EasyModelAnimationTransition.IMMEDIATE))
                        .then(
                            stopTimingBranch(
                                "immediate", EasyModelAnimationSwitchTiming.IMMEDIATE, true))
                        .then(
                            stopTimingBranch(
                                "after_current",
                                EasyModelAnimationSwitchTiming.AFTER_CURRENT,
                                true))));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> stopTimingBranch(
      String literal, EasyModelAnimationSwitchTiming timing, boolean blockTarget) {
    return Commands.literal(literal)
        .executes(
            context -> stop(context, blockTarget, new EasyModelAnimationTransition(timing, 0.0f)))
        .then(
            Commands.argument(BLEND_TICKS_ARGUMENT, FloatArgumentType.floatArg(0.0f))
                .executes(context -> stop(context, blockTarget, transition(context, timing))));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> restartCommand() {
    return Commands.literal("restart")
        .then(
            Commands.literal("entity")
                .then(
                    Commands.argument(TARGETS_ARGUMENT, EntityArgument.entities())
                        .suggests(
                            (context, builder) ->
                                EasyModelCommandSuggestions.suggestEntities(
                                    context,
                                    builder,
                                    EasyModelAnimationCommand::isRenderableEntity))
                        .executes(context -> restart(context, false))))
        .then(
            Commands.literal("block")
                .then(
                    Commands.argument(POSITION_ARGUMENT, BlockPosArgument.blockPos())
                        .executes(context -> restart(context, true))));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> setCommand() {
    return Commands.literal("set")
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
                        .then(stateArgument(false))))
        .then(
            Commands.literal("block")
                .then(
                    Commands.argument(POSITION_ARGUMENT, BlockPosArgument.blockPos())
                        .then(stateArgument(true))));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> stateArgument(boolean blockTarget) {
    ArgumentBuilder<CommandSourceStack, ?> stateArgument =
        Commands.argument(STATE_ARGUMENT, StringArgumentType.string())
            .suggests(
                (context, builder) ->
                    SharedSuggestionProvider.suggest(
                        EasyModelAnimation.standardStates().stream()
                            .map(EasyModelAnimation::serializedName),
                        builder))
            .executes(context -> set(context, blockTarget, EasyModelAnimationLoop.CLIP));
    for (EasyModelAnimationLoop loop : EasyModelAnimationLoop.values()) {
      stateArgument =
          stateArgument.then(
              Commands.literal(loop.getSerializedName())
                  .executes(context -> set(context, blockTarget, loop)));
    }
    return stateArgument;
  }

  private static int play(
      CommandContext<CommandSourceStack> context,
      boolean blockTarget,
      EasyModelAnimationPlayback playback,
      EasyModelAnimationTransition transition)
      throws CommandSyntaxException {
    Optional<EasyModelAnimation> animation =
        parseAnimation(StringArgumentType.getString(context, ANIMATION_ARGUMENT));
    if (animation.isEmpty()) {
      context.getSource().sendFailure(Component.literal("Invalid animation name."));
      return 0;
    }

    return blockTarget
        ? playBlockEntity(context, animation.get(), playback, transition)
        : playEntities(context, animation.get(), playback, transition);
  }

  private static int playEntities(
      CommandContext<CommandSourceStack> context,
      EasyModelAnimation animation,
      EasyModelAnimationPlayback playback,
      EasyModelAnimationTransition transition)
      throws CommandSyntaxException {
    int updated = 0;
    for (Entity entity : EntityArgument.getEntities(context, TARGETS_ARGUMENT)) {
      if (!isRenderableEntity(entity)) {
        continue;
      }
      EasyModelAnimationNetwork.send(
          entity,
          ClientboundEasyModelAnimationPacket.playEntity(
              entity.getId(), animation, playback, transition));
      updated++;
    }
    return sendResult(
        context.getSource(), "Started animation", updated, updated == 1 ? "entity" : "entities");
  }

  private static int playBlockEntity(
      CommandContext<CommandSourceStack> context,
      EasyModelAnimation animation,
      EasyModelAnimationPlayback playback,
      EasyModelAnimationTransition transition)
      throws CommandSyntaxException {
    BlockTarget target = blockTarget(context);
    if (!(target.blockEntity instanceof EasyModelRenderable)) {
      context.getSource().sendFailure(Component.literal("No Easy Model block entity at position."));
      return 0;
    }
    EasyModelAnimationNetwork.send(
        target.level,
        target.blockPos,
        ClientboundEasyModelAnimationPacket.playBlockEntity(
            target.blockPos, animation, playback, transition));
    return sendResult(context.getSource(), "Started animation", 1, "block entity");
  }

  private static int stop(
      CommandContext<CommandSourceStack> context,
      boolean blockTarget,
      EasyModelAnimationTransition transition)
      throws CommandSyntaxException {
    if (blockTarget) {
      BlockTarget target = blockTarget(context);
      if (!(target.blockEntity instanceof EasyModelRenderable)) {
        context
            .getSource()
            .sendFailure(Component.literal("No Easy Model block entity at position."));
        return 0;
      }
      EasyModelAnimationNetwork.send(
          target.level,
          target.blockPos,
          ClientboundEasyModelAnimationPacket.controlBlockEntity(
              target.blockPos, EasyModelAnimationPacketOperation.STOP, transition));
      return sendResult(context.getSource(), "Stopped animation", 1, "block entity");
    }

    int updated = 0;
    for (Entity entity : EntityArgument.getEntities(context, TARGETS_ARGUMENT)) {
      if (!isRenderableEntity(entity)) {
        continue;
      }
      EasyModelAnimationNetwork.send(
          entity,
          ClientboundEasyModelAnimationPacket.controlEntity(
              entity.getId(), EasyModelAnimationPacketOperation.STOP, transition));
      updated++;
    }
    return sendResult(
        context.getSource(), "Stopped animation", updated, updated == 1 ? "entity" : "entities");
  }

  private static int restart(CommandContext<CommandSourceStack> context, boolean blockTarget)
      throws CommandSyntaxException {
    if (blockTarget) {
      BlockTarget target = blockTarget(context);
      if (!(target.blockEntity instanceof EasyModelRenderable)) {
        context
            .getSource()
            .sendFailure(Component.literal("No Easy Model block entity at position."));
        return 0;
      }
      EasyModelAnimationNetwork.send(
          target.level,
          target.blockPos,
          ClientboundEasyModelAnimationPacket.controlBlockEntity(
              target.blockPos,
              EasyModelAnimationPacketOperation.RESTART,
              EasyModelAnimationTransition.IMMEDIATE));
      return sendResult(context.getSource(), "Restarted animation", 1, "block entity");
    }

    int updated = 0;
    for (Entity entity : EntityArgument.getEntities(context, TARGETS_ARGUMENT)) {
      if (!isRenderableEntity(entity)) {
        continue;
      }
      EasyModelAnimationNetwork.send(
          entity,
          ClientboundEasyModelAnimationPacket.controlEntity(
              entity.getId(),
              EasyModelAnimationPacketOperation.RESTART,
              EasyModelAnimationTransition.IMMEDIATE));
      updated++;
    }
    return sendResult(
        context.getSource(), "Restarted animation", updated, updated == 1 ? "entity" : "entities");
  }

  private static int set(
      CommandContext<CommandSourceStack> context, boolean blockTarget, EasyModelAnimationLoop loop)
      throws CommandSyntaxException {
    Optional<EasyModelAnimation> animation =
        parseAnimation(StringArgumentType.getString(context, STATE_ARGUMENT));
    if (animation.isEmpty()) {
      context.getSource().sendFailure(Component.literal("Invalid animation name."));
      return 0;
    }

    Optional<EasyModelAnimationSetting> state =
        animation.map(parsed -> new EasyModelAnimationSetting(parsed, loop));
    if (blockTarget) {
      BlockTarget target = blockTarget(context);
      if (!(target.blockEntity instanceof EasyModelHostBlockEntity hostBlockEntity)) {
        context
            .getSource()
            .sendFailure(Component.literal("No Easy Model host block entity at position."));
        return 0;
      }
      hostBlockEntity.setEasyModelAnimation(state.get());
      return sendResult(context.getSource(), "Set animation", 1, "block entity");
    }

    int updated = 0;
    Collection<? extends Entity> targets = EntityArgument.getEntities(context, TARGETS_ARGUMENT);
    for (Entity entity : targets) {
      if (entity instanceof EasyModelEntityHost hostEntity) {
        hostEntity.setEasyModelAnimation(state.get());
        updated++;
      }
    }
    return sendResult(
        context.getSource(), "Set animation", updated, updated == 1 ? "entity" : "entities");
  }

  static Optional<EasyModelAnimation> parseAnimation(String value) {
    if (value == null || value.isBlank()) {
      return Optional.empty();
    }
    Optional<EasyModelAnimation> parsed = EasyModelAnimation.parse(value);
    if (parsed.isEmpty() && value.trim().toLowerCase(Locale.ROOT).startsWith("named:")) {
      return Optional.empty();
    }
    EasyModelAnimation animation = parsed.orElseGet(() -> EasyModelAnimation.named(value));
    return animation.serializedName().length()
            <= ClientboundEasyModelAnimationPacket.MAX_ANIMATION_NAME_LENGTH
        ? Optional.of(animation)
        : Optional.empty();
  }

  static boolean isRenderableEntity(Entity entity) {
    return entity instanceof EasyModelEntityHost || entity instanceof EasyModelRenderable;
  }

  private static EasyModelAnimationPlayback playback(
      CommandContext<CommandSourceStack> context,
      EasyModelAnimationPlaybackMode mode,
      float durationTicks) {
    int repeatCount =
        mode == EasyModelAnimationPlaybackMode.REPEAT
            ? IntegerArgumentType.getInteger(context, REPEAT_COUNT_ARGUMENT)
            : 1;
    return new EasyModelAnimationPlayback(mode, repeatCount, durationTicks);
  }

  private static EasyModelAnimationTransition transition(
      CommandContext<CommandSourceStack> context, EasyModelAnimationSwitchTiming timing) {
    return new EasyModelAnimationTransition(
        timing, FloatArgumentType.getFloat(context, BLEND_TICKS_ARGUMENT));
  }

  private static BlockTarget blockTarget(CommandContext<CommandSourceStack> context)
      throws CommandSyntaxException {
    ServerLevel level = context.getSource().getLevel();
    BlockPos blockPos = BlockPosArgument.getLoadedBlockPos(context, POSITION_ARGUMENT);
    return new BlockTarget(level, blockPos, level.getBlockEntity(blockPos));
  }

  private static int sendResult(
      CommandSourceStack source, String action, int updated, String targetName) {
    if (updated == 0) {
      source.sendFailure(Component.literal("No compatible Easy Model targets selected."));
      return 0;
    }
    source.sendSuccess(
        () -> Component.literal(action + " for " + updated + " " + targetName + "."), true);
    return updated;
  }

  private record BlockTarget(ServerLevel level, BlockPos blockPos, BlockEntity blockEntity) {}
}
