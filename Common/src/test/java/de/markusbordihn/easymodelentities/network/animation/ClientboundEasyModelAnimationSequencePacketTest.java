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
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationSequence;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationStep;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationTransition;
import io.netty.buffer.Unpooled;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

class ClientboundEasyModelAnimationSequencePacketTest {

  private static final EasyModelAnimation IDLE = EasyModelAnimation.named("idle");
  private static final EasyModelAnimation WAVE = EasyModelAnimation.named("wave");

  @Test
  void entitySequenceRoundTripsEveryStep() {
    ClientboundEasyModelAnimationSequencePacket packet =
        ClientboundEasyModelAnimationSequencePacket.forEntity(
            42, EasyModelAnimationSequence.of(WAVE, IDLE).withFallback(EasyModelAnimation.IDLE));
    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

    packet.encode(buffer);

    assertEquals(packet, ClientboundEasyModelAnimationSequencePacket.decode(buffer));
  }

  @Test
  void blockEntitySequenceRoundTrips() {
    ClientboundEasyModelAnimationSequencePacket packet =
        ClientboundEasyModelAnimationSequencePacket.forBlockEntity(
            new BlockPos(1, 64, -3), EasyModelAnimationSequence.of(WAVE));
    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

    packet.encode(buffer);

    assertEquals(packet, ClientboundEasyModelAnimationSequencePacket.decode(buffer));
  }

  @Test
  void decodeClampsAnOversizedStepCount() {
    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
    buffer.writeEnum(EasyModelAnimationPacketTarget.ENTITY);
    buffer.writeLong(7L);
    buffer.writeUtf(
        EasyModelAnimation.AUTO.serializedName(),
        ClientboundEasyModelAnimationPacket.MAX_ANIMATION_NAME_LENGTH);
    buffer.writeVarInt(EasyModelAnimationSequence.MAX_STEPS + 8);
    for (int i = 0; i < EasyModelAnimationSequence.MAX_STEPS + 8; i++) {
      buffer.writeUtf(
          WAVE.serializedName(), ClientboundEasyModelAnimationPacket.MAX_ANIMATION_NAME_LENGTH);
      buffer.writeEnum(EasyModelAnimationPlayback.DEFAULT.mode());
      buffer.writeVarInt(1);
      buffer.writeFloat(0.0f);
      buffer.writeEnum(EasyModelAnimationTransition.DEFAULT.timing());
      buffer.writeFloat(5.0f);
    }

    ClientboundEasyModelAnimationSequencePacket packet =
        ClientboundEasyModelAnimationSequencePacket.decode(buffer);

    assertEquals(EasyModelAnimationSequence.MAX_STEPS, packet.steps().size());
  }

  @Test
  void unknownStepNamesAreSkipped() {
    ClientboundEasyModelAnimationSequencePacket packet =
        new ClientboundEasyModelAnimationSequencePacket(
            EasyModelAnimationPacketTarget.ENTITY,
            7,
            List.of(
                new ClientboundEasyModelAnimationSequencePacket.Step(
                    "definitely-not-an-animation",
                    EasyModelAnimationPlayback.DEFAULT,
                    EasyModelAnimationTransition.DEFAULT),
                new ClientboundEasyModelAnimationSequencePacket.Step(
                    WAVE.serializedName(),
                    EasyModelAnimationPlayback.DEFAULT,
                    EasyModelAnimationTransition.DEFAULT)),
            EasyModelAnimation.AUTO.serializedName());

    Optional<EasyModelAnimationSequence> sequence = packet.toSequence();

    assertTrue(sequence.isPresent());
    assertEquals(
        List.of(WAVE),
        sequence.get().steps().stream().map(EasyModelAnimationStep::animation).toList());
  }

  @Test
  void rejectsInvalidPacketValues() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ClientboundEasyModelAnimationSequencePacket(
                EasyModelAnimationPacketTarget.ENTITY,
                -1,
                List.of(),
                EasyModelAnimation.AUTO.serializedName()));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ClientboundEasyModelAnimationSequencePacket(
                EasyModelAnimationPacketTarget.ENTITY,
                1,
                Collections.nCopies(
                    EasyModelAnimationSequence.MAX_STEPS + 1,
                    new ClientboundEasyModelAnimationSequencePacket.Step(
                        WAVE.serializedName(),
                        EasyModelAnimationPlayback.DEFAULT,
                        EasyModelAnimationTransition.DEFAULT)),
                EasyModelAnimation.AUTO.serializedName()));
  }
}
