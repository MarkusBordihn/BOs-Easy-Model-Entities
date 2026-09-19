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

package de.markusbordihn.easymodelentities.network.animation;

import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationPlayback;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationPlaybackMode;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationSequence;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationStep;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationSwitchTiming;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationTransition;
import de.markusbordihn.easymodelentities.registry.ModelResourcePaths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundEasyModelAnimationSequencePacket(
    EasyModelAnimationPacketTarget targetType, long targetValue, List<Step> steps, String fallback)
    implements CustomPacketPayload {

  public static final CustomPacketPayload.Type<ClientboundEasyModelAnimationSequencePacket> TYPE =
      new CustomPacketPayload.Type<>(ModelResourcePaths.modIdentifier("animation_sequence"));
  public static final StreamCodec<FriendlyByteBuf, ClientboundEasyModelAnimationSequencePacket>
      STREAM_CODEC =
          StreamCodec.of(
              (buffer, packet) -> packet.encode(buffer),
              ClientboundEasyModelAnimationSequencePacket::decode);

  public ClientboundEasyModelAnimationSequencePacket {
    Objects.requireNonNull(targetType, "targetType");
    steps = List.copyOf(Objects.requireNonNull(steps, "steps"));
    fallback = Objects.requireNonNull(fallback, "fallback").trim().toLowerCase(Locale.ROOT);
    if (steps.size() > EasyModelAnimationSequence.MAX_STEPS) {
      throw new IllegalArgumentException(
          "Sequence exceeds " + EasyModelAnimationSequence.MAX_STEPS + " steps.");
    }
    if (fallback.length() > ClientboundEasyModelAnimationPacket.MAX_ANIMATION_NAME_LENGTH) {
      throw new IllegalArgumentException("Animation name exceeds 256 characters.");
    }
    if (targetType == EasyModelAnimationPacketTarget.ENTITY
        && (targetValue < 0 || targetValue > Integer.MAX_VALUE)) {
      throw new IllegalArgumentException("Entity target must contain a valid entity id.");
    }
  }

  public static ClientboundEasyModelAnimationSequencePacket forEntity(
      int entityId, EasyModelAnimationSequence sequence) {
    return new ClientboundEasyModelAnimationSequencePacket(
        EasyModelAnimationPacketTarget.ENTITY,
        entityId,
        steps(sequence),
        sequence.fallback().serializedName());
  }

  public static ClientboundEasyModelAnimationSequencePacket forBlockEntity(
      BlockPos blockPos, EasyModelAnimationSequence sequence) {
    return new ClientboundEasyModelAnimationSequencePacket(
        EasyModelAnimationPacketTarget.BLOCK_ENTITY,
        blockPos.asLong(),
        steps(sequence),
        sequence.fallback().serializedName());
  }

  private static List<Step> steps(EasyModelAnimationSequence sequence) {
    return Objects.requireNonNull(sequence, "sequence").steps().stream()
        .map(
            step -> new Step(step.animation().serializedName(), step.playback(), step.transition()))
        .toList();
  }

  public static ClientboundEasyModelAnimationSequencePacket decode(FriendlyByteBuf buffer) {
    EasyModelAnimationPacketTarget targetType =
        readEnum(
            buffer, EasyModelAnimationPacketTarget.values(), EasyModelAnimationPacketTarget.ENTITY);
    long rawTargetValue = buffer.readLong();
    String fallback = buffer.readUtf(ClientboundEasyModelAnimationPacket.MAX_ANIMATION_NAME_LENGTH);
    int stepCount =
        Math.max(0, Math.min(buffer.readVarInt(), EasyModelAnimationSequence.MAX_STEPS));
    List<Step> steps = new ArrayList<>(stepCount);
    for (int i = 0; i < stepCount; i++) {
      steps.add(decodeStep(buffer));
    }

    return new ClientboundEasyModelAnimationSequencePacket(
        targetType, targetValue(targetType, rawTargetValue), steps, fallback);
  }

  private static Step decodeStep(FriendlyByteBuf buffer) {
    String animation =
        buffer.readUtf(ClientboundEasyModelAnimationPacket.MAX_ANIMATION_NAME_LENGTH);
    EasyModelAnimationPlaybackMode playbackMode =
        readEnum(
            buffer, EasyModelAnimationPlaybackMode.values(), EasyModelAnimationPlaybackMode.ONCE);
    int repeatCount = buffer.readVarInt();
    float durationTicks = buffer.readFloat();
    EasyModelAnimationSwitchTiming timing =
        readEnum(
            buffer,
            EasyModelAnimationSwitchTiming.values(),
            EasyModelAnimationSwitchTiming.AFTER_CURRENT);
    float blendDurationTicks = buffer.readFloat();
    return new Step(
        animation,
        new EasyModelAnimationPlayback(
            playbackMode, Math.max(1, repeatCount), nonNegativeFinite(durationTicks)),
        new EasyModelAnimationTransition(timing, nonNegativeFinite(blendDurationTicks)));
  }

  private static <E extends Enum<E>> E readEnum(FriendlyByteBuf buffer, E[] values, E fallback) {
    int ordinal = buffer.readVarInt();
    return ordinal < 0 || ordinal >= values.length ? fallback : values[ordinal];
  }

  private static long targetValue(EasyModelAnimationPacketTarget targetType, long targetValue) {
    return targetType == EasyModelAnimationPacketTarget.ENTITY
        ? Math.max(0L, Math.min(targetValue, Integer.MAX_VALUE))
        : targetValue;
  }

  private static float nonNegativeFinite(float value) {
    return Float.isFinite(value) && value > 0.0f ? value : 0.0f;
  }

  @Override
  public CustomPacketPayload.Type<ClientboundEasyModelAnimationSequencePacket> type() {
    return TYPE;
  }

  public void encode(FriendlyByteBuf buffer) {
    buffer.writeEnum(this.targetType);
    buffer.writeLong(this.targetValue);
    buffer.writeUtf(this.fallback, ClientboundEasyModelAnimationPacket.MAX_ANIMATION_NAME_LENGTH);
    buffer.writeVarInt(this.steps.size());
    for (Step step : this.steps) {
      buffer.writeUtf(
          step.animation(), ClientboundEasyModelAnimationPacket.MAX_ANIMATION_NAME_LENGTH);
      buffer.writeEnum(step.playback().mode());
      buffer.writeVarInt(step.playback().repeatCount());
      buffer.writeFloat(step.playback().durationTicks());
      buffer.writeEnum(step.transition().timing());
      buffer.writeFloat(step.transition().blendDurationTicks());
    }
  }

  public Optional<EasyModelAnimationSequence> toSequence() {
    List<EasyModelAnimationStep> sequenceSteps = new ArrayList<>(this.steps.size());
    for (Step step : this.steps) {
      EasyModelAnimation.parse(step.animation())
          .filter(animation -> !animation.equals(EasyModelAnimation.AUTO))
          .ifPresent(
              animation ->
                  sequenceSteps.add(
                      new EasyModelAnimationStep(animation, step.playback(), step.transition())));
    }
    if (sequenceSteps.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(
        new EasyModelAnimationSequence(
            sequenceSteps,
            EasyModelAnimation.parse(this.fallback).orElse(EasyModelAnimation.AUTO)));
  }

  public record Step(
      String animation,
      EasyModelAnimationPlayback playback,
      EasyModelAnimationTransition transition) {

    public Step {
      animation = Objects.requireNonNull(animation, "animation").trim().toLowerCase(Locale.ROOT);
      Objects.requireNonNull(playback, "playback");
      Objects.requireNonNull(transition, "transition");
      if (animation.length() > ClientboundEasyModelAnimationPacket.MAX_ANIMATION_NAME_LENGTH) {
        throw new IllegalArgumentException("Animation name exceeds 256 characters.");
      }
    }
  }
}
