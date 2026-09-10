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
import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.targetBranches;
import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.updateHosts;
import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.updateRenderables;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationLoop;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationSetting;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationPlayback;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationPlaybackMode;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationSequence;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationSwitchTiming;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationTransition;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationClips;
import de.markusbordihn.easymodelentities.network.animation.ClientboundEasyModelAnimationPacket;
import de.markusbordihn.easymodelentities.network.animation.ClientboundEasyModelAnimationSequencePacket;
import de.markusbordihn.easymodelentities.network.animation.EasyModelAnimationNetwork;
import de.markusbordihn.easymodelentities.network.animation.EasyModelAnimationPacketOperation;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.Entity;

final class EasyModelAnimationCommand {

  private static final String ANIMATION_ARGUMENT = "animation";
  private static final String CLIPS_ARGUMENT = "clips";
  private static final String STATE_ARGUMENT = "state";
  private static final int MAX_RANDOM_CLIPS = 16;
  private static final String REPEAT_COUNT_ARGUMENT = "count";
  private static final String BLEND_TICKS_ARGUMENT = "blend_ticks";
  private static final String DURATION_TICKS_ARGUMENT = "duration_ticks";
  private static final String INVALID_ANIMATION_MESSAGE = "Invalid animation name.";

  private EasyModelAnimationCommand() {}

  static ArgumentBuilder<CommandSourceStack, ?> register() {
    return Commands.literal("animation")
        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
        .then(playCommand())
        .then(stopCommand())
        .then(restartCommand())
        .then(setCommand());
  }

  private static ArgumentBuilder<CommandSourceStack, ?> playCommand() {
    return targetBranches(
        Commands.literal("play"),
        RENDERABLE_ENTITY,
        (target, blockTarget) ->
            target
                .then(playAnimationArgument(blockTarget, false))
                .then(Commands.literal("random").then(playAnimationArgument(blockTarget, true)))
                .then(Commands.literal("sequence").then(playSequenceArgument(blockTarget))));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> playSequenceArgument(boolean blockTarget) {
    return Commands.argument(CLIPS_ARGUMENT, StringArgumentType.string())
        .suggests(
            (context, builder) ->
                SharedSuggestionProvider.suggest(
                    Stream.of(
                        "\""
                            + ModelAnimationClips.IDLE
                            + ","
                            + ModelAnimationClips.WALK
                            + ","
                            + ModelAnimationClips.IDLE
                            + "\""),
                    builder))
        .executes(context -> playSequence(context, blockTarget, false))
        .then(
            Commands.literal("fallback")
                .then(
                    Commands.argument(ANIMATION_ARGUMENT, StringArgumentType.string())
                        .suggests(
                            (context, builder) ->
                                SharedSuggestionProvider.suggest(
                                    EasyModelAnimation.standardStates().stream()
                                        .map(EasyModelAnimation::serializedName),
                                    builder))
                        .executes(context -> playSequence(context, blockTarget, true))));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> playAnimationArgument(
      boolean blockTarget, boolean randomPick) {
    return Commands.argument(
            randomPick ? CLIPS_ARGUMENT : ANIMATION_ARGUMENT, StringArgumentType.string())
        .suggests(
            (context, builder) ->
                randomPick
                    ? SharedSuggestionProvider.suggest(
                        Stream.of(
                            "\""
                                + ModelAnimationClips.IDLE
                                + ","
                                + ModelAnimationClips.IDLE
                                + "_2,"
                                + ModelAnimationClips.IDLE
                                + "_3\""),
                        builder)
                    : SharedSuggestionProvider.suggest(
                        EasyModelAnimation.standardStates().stream()
                            .map(EasyModelAnimation::serializedName),
                        builder))
        .executes(
            context ->
                play(
                    context,
                    blockTarget,
                    randomPick,
                    EasyModelAnimationPlayback.DEFAULT,
                    EasyModelAnimationTransition.DEFAULT))
        .then(playMode("once", EasyModelAnimationPlaybackMode.ONCE, blockTarget, randomPick))
        .then(playMode("loop", EasyModelAnimationPlaybackMode.LOOP, blockTarget, randomPick))
        .then(
            Commands.literal("repeat")
                .then(
                    Commands.argument(REPEAT_COUNT_ARGUMENT, IntegerArgumentType.integer(1))
                        .executes(
                            context ->
                                play(
                                    context,
                                    blockTarget,
                                    randomPick,
                                    playback(context, EasyModelAnimationPlaybackMode.REPEAT, 0.0f),
                                    EasyModelAnimationTransition.DEFAULT))
                        .then(
                            timingBranch(
                                "immediate",
                                EasyModelAnimationSwitchTiming.IMMEDIATE,
                                EasyModelAnimationPlaybackMode.REPEAT,
                                blockTarget,
                                randomPick))
                        .then(
                            timingBranch(
                                "after_current",
                                EasyModelAnimationSwitchTiming.AFTER_CURRENT,
                                EasyModelAnimationPlaybackMode.REPEAT,
                                blockTarget,
                                randomPick))));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> playMode(
      String literal,
      EasyModelAnimationPlaybackMode mode,
      boolean blockTarget,
      boolean randomPick) {
    return Commands.literal(literal)
        .executes(
            context ->
                play(
                    context,
                    blockTarget,
                    randomPick,
                    playback(context, mode, 0.0f),
                    EasyModelAnimationTransition.DEFAULT))
        .then(
            timingBranch(
                "immediate",
                EasyModelAnimationSwitchTiming.IMMEDIATE,
                mode,
                blockTarget,
                randomPick))
        .then(
            timingBranch(
                "after_current",
                EasyModelAnimationSwitchTiming.AFTER_CURRENT,
                mode,
                blockTarget,
                randomPick));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> timingBranch(
      String literal,
      EasyModelAnimationSwitchTiming timing,
      EasyModelAnimationPlaybackMode mode,
      boolean blockTarget,
      boolean randomPick) {
    return Commands.literal(literal)
        .executes(
            context ->
                play(
                    context,
                    blockTarget,
                    randomPick,
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
                            randomPick,
                            playback(context, mode, 0.0f),
                            transition(context, timing)))
                .then(
                    Commands.argument(DURATION_TICKS_ARGUMENT, FloatArgumentType.floatArg(0.0f))
                        .executes(
                            context ->
                                play(
                                    context,
                                    blockTarget,
                                    randomPick,
                                    playback(
                                        context,
                                        mode,
                                        FloatArgumentType.getFloat(
                                            context, DURATION_TICKS_ARGUMENT)),
                                    transition(context, timing)))));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> stopCommand() {
    return targetBranches(
        Commands.literal("stop"),
        RENDERABLE_ENTITY,
        (target, blockTarget) ->
            target
                .executes(
                    context -> stop(context, blockTarget, EasyModelAnimationTransition.IMMEDIATE))
                .then(
                    stopTimingBranch(
                        "immediate", EasyModelAnimationSwitchTiming.IMMEDIATE, blockTarget))
                .then(
                    stopTimingBranch(
                        "after_current",
                        EasyModelAnimationSwitchTiming.AFTER_CURRENT,
                        blockTarget)));
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
    return targetBranches(
        Commands.literal("restart"),
        RENDERABLE_ENTITY,
        (target, blockTarget) -> target.executes(context -> restart(context, blockTarget)));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> setCommand() {
    return targetBranches(
        Commands.literal("set"),
        HOST_ENTITY,
        (target, blockTarget) -> target.then(stateArgument(blockTarget)));
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
      boolean randomPick,
      EasyModelAnimationPlayback playback,
      EasyModelAnimationTransition transition)
      throws CommandSyntaxException {
    List<EasyModelAnimation> candidates =
        randomPick
            ? parseAnimations(StringArgumentType.getString(context, CLIPS_ARGUMENT))
            : parseAnimation(StringArgumentType.getString(context, ANIMATION_ARGUMENT)).stream()
                .toList();
    if (candidates.isEmpty()) {
      context.getSource().sendFailure(Component.literal(INVALID_ANIMATION_MESSAGE));
      return 0;
    }

    return updateRenderables(
        context,
        blockTarget,
        "Started animation",
        target ->
            EasyModelAnimationNetwork.send(
                target.level(),
                target.blockPos(),
                ClientboundEasyModelAnimationPacket.playBlockEntity(
                    target.blockPos(), pick(context, candidates), playback, transition)),
        entity ->
            EasyModelAnimationNetwork.send(
                entity,
                ClientboundEasyModelAnimationPacket.playEntity(
                    entity.getId(), pick(context, candidates), playback, transition)));
  }

  private static int playSequence(
      CommandContext<CommandSourceStack> context, boolean blockTarget, boolean withFallback)
      throws CommandSyntaxException {
    List<EasyModelAnimation> animations =
        parseAnimationSequence(StringArgumentType.getString(context, CLIPS_ARGUMENT));
    Optional<EasyModelAnimation> fallback =
        withFallback
            ? parseAnimation(StringArgumentType.getString(context, ANIMATION_ARGUMENT))
            : Optional.of(EasyModelAnimation.AUTO);
    if (animations.isEmpty() || fallback.isEmpty()) {
      context.getSource().sendFailure(Component.literal(INVALID_ANIMATION_MESSAGE));
      return 0;
    }

    EasyModelAnimationSequence sequence =
        EasyModelAnimationSequence.of(animations).withFallback(fallback.get());
    return updateRenderables(
        context,
        blockTarget,
        "Started animation sequence",
        target ->
            EasyModelAnimationNetwork.send(
                target.level(),
                target.blockPos(),
                ClientboundEasyModelAnimationSequencePacket.forBlockEntity(
                    target.blockPos(), sequence)),
        entity ->
            EasyModelAnimationNetwork.send(
                entity,
                ClientboundEasyModelAnimationSequencePacket.forEntity(entity.getId(), sequence)));
  }

  private static EasyModelAnimation pick(
      CommandContext<CommandSourceStack> context, List<EasyModelAnimation> candidates) {
    return candidates.size() == 1
        ? candidates.get(0)
        : candidates.get(context.getSource().getLevel().getRandom().nextInt(candidates.size()));
  }

  private static int stop(
      CommandContext<CommandSourceStack> context,
      boolean blockTarget,
      EasyModelAnimationTransition transition)
      throws CommandSyntaxException {
    return control(
        context,
        blockTarget,
        EasyModelAnimationPacketOperation.STOP,
        transition,
        "Stopped animation");
  }

  private static int restart(CommandContext<CommandSourceStack> context, boolean blockTarget)
      throws CommandSyntaxException {
    return control(
        context,
        blockTarget,
        EasyModelAnimationPacketOperation.RESTART,
        EasyModelAnimationTransition.IMMEDIATE,
        "Restarted animation");
  }

  private static int control(
      CommandContext<CommandSourceStack> context,
      boolean blockTarget,
      EasyModelAnimationPacketOperation operation,
      EasyModelAnimationTransition transition,
      String action)
      throws CommandSyntaxException {
    return updateRenderables(
        context,
        blockTarget,
        action,
        target ->
            EasyModelAnimationNetwork.send(
                target.level(),
                target.blockPos(),
                ClientboundEasyModelAnimationPacket.controlBlockEntity(
                    target.blockPos(), operation, transition)),
        entity ->
            EasyModelAnimationNetwork.send(
                entity,
                ClientboundEasyModelAnimationPacket.controlEntity(
                    entity.getId(), operation, transition)));
  }

  private static int set(
      CommandContext<CommandSourceStack> context, boolean blockTarget, EasyModelAnimationLoop loop)
      throws CommandSyntaxException {
    Optional<EasyModelAnimation> animation =
        parseAnimation(StringArgumentType.getString(context, STATE_ARGUMENT));
    if (animation.isEmpty()) {
      context.getSource().sendFailure(Component.literal(INVALID_ANIMATION_MESSAGE));
      return 0;
    }

    EasyModelAnimationSetting setting = new EasyModelAnimationSetting(animation.get(), loop);
    return updateHosts(
        context,
        blockTarget,
        "Set animation",
        hostBlockEntity -> hostBlockEntity.setEasyModelAnimation(setting),
        hostEntity -> hostEntity.setEasyModelAnimation(setting));
  }

  static List<EasyModelAnimation> parseAnimations(String value) {
    if (value == null || value.isBlank()) {
      return List.of();
    }

    return Arrays.stream(value.split(","))
        .map(String::trim)
        .filter(clipName -> !clipName.isEmpty())
        .distinct()
        .limit(MAX_RANDOM_CLIPS)
        .map(EasyModelAnimationCommand::parseAnimation)
        .flatMap(Optional::stream)
        .toList();
  }

  static List<EasyModelAnimation> parseAnimationSequence(String value) {
    if (value == null || value.isBlank()) {
      return List.of();
    }

    return Arrays.stream(value.split(","))
        .map(String::trim)
        .filter(clipName -> !clipName.isEmpty())
        .limit(EasyModelAnimationSequence.MAX_STEPS)
        .map(EasyModelAnimationCommand::parseAnimation)
        .flatMap(Optional::stream)
        .filter(animation -> !animation.equals(EasyModelAnimation.AUTO))
        .toList();
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
}
