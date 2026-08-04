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
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationSwitchTiming;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationTransition;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record ClientboundEasyModelAnimationPacket(
    EasyModelAnimationPacketTarget targetType,
    long targetValue,
    EasyModelAnimationPacketOperation operation,
    String animation,
    EasyModelAnimationPlayback playback,
    EasyModelAnimationTransition transition) {

  public static final int MAX_ANIMATION_NAME_LENGTH = 256;

  public ClientboundEasyModelAnimationPacket {
    Objects.requireNonNull(targetType, "targetType");
    Objects.requireNonNull(operation, "operation");
    animation = Objects.requireNonNull(animation, "animation").trim().toLowerCase(Locale.ROOT);
    Objects.requireNonNull(playback, "playback");
    Objects.requireNonNull(transition, "transition");
    if (animation.length() > MAX_ANIMATION_NAME_LENGTH) {
      throw new IllegalArgumentException("Animation name exceeds 256 characters.");
    }
    if (targetType == EasyModelAnimationPacketTarget.ENTITY
        && (targetValue < 0 || targetValue > Integer.MAX_VALUE)) {
      throw new IllegalArgumentException("Entity target must contain a valid entity id.");
    }
  }

  public static ClientboundEasyModelAnimationPacket playEntity(
      int entityId,
      EasyModelAnimation animation,
      EasyModelAnimationPlayback playback,
      EasyModelAnimationTransition transition) {
    return new ClientboundEasyModelAnimationPacket(
        EasyModelAnimationPacketTarget.ENTITY,
        entityId,
        EasyModelAnimationPacketOperation.PLAY,
        animation.serializedName(),
        playback,
        transition);
  }

  public static ClientboundEasyModelAnimationPacket playBlockEntity(
      BlockPos blockPos,
      EasyModelAnimation animation,
      EasyModelAnimationPlayback playback,
      EasyModelAnimationTransition transition) {
    return new ClientboundEasyModelAnimationPacket(
        EasyModelAnimationPacketTarget.BLOCK_ENTITY,
        blockPos.asLong(),
        EasyModelAnimationPacketOperation.PLAY,
        animation.serializedName(),
        playback,
        transition);
  }

  public static ClientboundEasyModelAnimationPacket controlEntity(
      int entityId,
      EasyModelAnimationPacketOperation operation,
      EasyModelAnimationTransition transition) {
    return new ClientboundEasyModelAnimationPacket(
        EasyModelAnimationPacketTarget.ENTITY,
        entityId,
        operation,
        "",
        EasyModelAnimationPlayback.DEFAULT,
        transition);
  }

  public static ClientboundEasyModelAnimationPacket controlBlockEntity(
      BlockPos blockPos,
      EasyModelAnimationPacketOperation operation,
      EasyModelAnimationTransition transition) {
    return new ClientboundEasyModelAnimationPacket(
        EasyModelAnimationPacketTarget.BLOCK_ENTITY,
        blockPos.asLong(),
        operation,
        "",
        EasyModelAnimationPlayback.DEFAULT,
        transition);
  }

  public static ClientboundEasyModelAnimationPacket decode(FriendlyByteBuf buffer) {
    EasyModelAnimationPacketTarget targetType =
        readEnum(
            buffer, EasyModelAnimationPacketTarget.values(), EasyModelAnimationPacketTarget.ENTITY);
    long rawTargetValue = buffer.readLong();
    EasyModelAnimationPacketOperation operation =
        readEnum(
            buffer,
            EasyModelAnimationPacketOperation.values(),
            EasyModelAnimationPacketOperation.STOP);
    String animation = buffer.readUtf(MAX_ANIMATION_NAME_LENGTH);
    EasyModelAnimationPlaybackMode playbackMode =
        readEnum(
            buffer, EasyModelAnimationPlaybackMode.values(), EasyModelAnimationPlaybackMode.ONCE);
    int repeatCount = buffer.readVarInt();
    float durationTicks = buffer.readFloat();
    EasyModelAnimationSwitchTiming timing =
        readEnum(
            buffer,
            EasyModelAnimationSwitchTiming.values(),
            EasyModelAnimationSwitchTiming.IMMEDIATE);
    float blendDurationTicks = buffer.readFloat();
    return new ClientboundEasyModelAnimationPacket(
        targetType,
        targetValue(targetType, rawTargetValue),
        operation,
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

  public void encode(FriendlyByteBuf buffer) {
    buffer.writeEnum(this.targetType);
    buffer.writeLong(this.targetValue);
    buffer.writeEnum(this.operation);
    buffer.writeUtf(this.animation, MAX_ANIMATION_NAME_LENGTH);
    buffer.writeEnum(this.playback.mode());
    buffer.writeVarInt(this.playback.repeatCount());
    buffer.writeFloat(this.playback.durationTicks());
    buffer.writeEnum(this.transition.timing());
    buffer.writeFloat(this.transition.blendDurationTicks());
  }
}
