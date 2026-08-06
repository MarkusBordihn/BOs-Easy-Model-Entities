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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationPlayback;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationPlaybackMode;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationSwitchTiming;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationTransition;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

class ClientboundEasyModelAnimationPacketTest {

  @Test
  void entityPacketRoundTripsAllPlaybackFields() {
    ClientboundEasyModelAnimationPacket packet =
        ClientboundEasyModelAnimationPacket.playEntity(
            42,
            EasyModelAnimation.named("wave"),
            new EasyModelAnimationPlayback(EasyModelAnimationPlaybackMode.REPEAT, 3, 40.0f),
            EasyModelAnimationTransition.afterCurrent(7.5f));
    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

    packet.encode(buffer);

    assertEquals(packet, ClientboundEasyModelAnimationPacket.decode(buffer));
  }

  @Test
  void blockControlPacketRoundTrips() {
    ClientboundEasyModelAnimationPacket packet =
        ClientboundEasyModelAnimationPacket.controlBlockEntity(
            new BlockPos(1, 64, -3),
            EasyModelAnimationPacketOperation.STOP,
            EasyModelAnimationTransition.IMMEDIATE);
    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

    packet.encode(buffer);

    assertEquals(packet, ClientboundEasyModelAnimationPacket.decode(buffer));
  }

  @Test
  void rejectsInvalidPacketValues() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ClientboundEasyModelAnimationPacket(
                EasyModelAnimationPacketTarget.ENTITY,
                -1,
                EasyModelAnimationPacketOperation.PLAY,
                "idle",
                EasyModelAnimationPlayback.DEFAULT,
                EasyModelAnimationTransition.DEFAULT));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ClientboundEasyModelAnimationPacket(
                EasyModelAnimationPacketTarget.ENTITY,
                1,
                EasyModelAnimationPacketOperation.PLAY,
                "x".repeat(ClientboundEasyModelAnimationPacket.MAX_ANIMATION_NAME_LENGTH + 1),
                EasyModelAnimationPlayback.DEFAULT,
                EasyModelAnimationTransition.DEFAULT));
  }

  @Test
  void decodeSanitizesOutOfRangeEnumOrdinals() {
    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
    buffer.writeVarInt(99);
    buffer.writeLong(7L);
    buffer.writeVarInt(99);
    buffer.writeUtf("named:wave", ClientboundEasyModelAnimationPacket.MAX_ANIMATION_NAME_LENGTH);
    buffer.writeVarInt(99);
    buffer.writeVarInt(2);
    buffer.writeFloat(10.0f);
    buffer.writeVarInt(99);
    buffer.writeFloat(4.0f);

    ClientboundEasyModelAnimationPacket packet = ClientboundEasyModelAnimationPacket.decode(buffer);

    assertEquals(EasyModelAnimationPacketTarget.ENTITY, packet.targetType());
    assertEquals(EasyModelAnimationPacketOperation.STOP, packet.operation());
    assertEquals(EasyModelAnimationPlaybackMode.ONCE, packet.playback().mode());
    assertEquals(EasyModelAnimationSwitchTiming.IMMEDIATE, packet.transition().timing());
  }

  @Test
  void decodeClampsInvalidNumbersInsteadOfThrowing() {
    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
    buffer.writeEnum(EasyModelAnimationPacketTarget.ENTITY);
    buffer.writeLong(Long.MIN_VALUE);
    buffer.writeEnum(EasyModelAnimationPacketOperation.PLAY);
    buffer.writeUtf("named:wave", ClientboundEasyModelAnimationPacket.MAX_ANIMATION_NAME_LENGTH);
    buffer.writeEnum(EasyModelAnimationPlaybackMode.REPEAT);
    buffer.writeVarInt(-5);
    buffer.writeFloat(Float.NaN);
    buffer.writeEnum(EasyModelAnimationSwitchTiming.AFTER_CURRENT);
    buffer.writeFloat(Float.NEGATIVE_INFINITY);

    ClientboundEasyModelAnimationPacket packet = ClientboundEasyModelAnimationPacket.decode(buffer);

    assertEquals(0L, packet.targetValue());
    assertEquals(1, packet.playback().repeatCount());
    assertEquals(0.0f, packet.playback().durationTicks());
    assertEquals(0.0f, packet.transition().blendDurationTicks());
  }

  @Test
  void decodeKeepsUnknownAnimationNamesForTheHandlerToIgnore() {
    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
    buffer.writeEnum(EasyModelAnimationPacketTarget.ENTITY);
    buffer.writeLong(1L);
    buffer.writeEnum(EasyModelAnimationPacketOperation.PLAY);
    buffer.writeUtf(
        "missing-prefix", ClientboundEasyModelAnimationPacket.MAX_ANIMATION_NAME_LENGTH);
    buffer.writeEnum(EasyModelAnimationPlaybackMode.ONCE);
    buffer.writeVarInt(1);
    buffer.writeFloat(0.0f);
    buffer.writeEnum(EasyModelAnimationSwitchTiming.IMMEDIATE);
    buffer.writeFloat(0.0f);

    ClientboundEasyModelAnimationPacket packet = ClientboundEasyModelAnimationPacket.decode(buffer);

    assertEquals("missing-prefix", packet.animation());
    assertTrue(EasyModelAnimation.parse(packet.animation()).isEmpty());
  }
}
