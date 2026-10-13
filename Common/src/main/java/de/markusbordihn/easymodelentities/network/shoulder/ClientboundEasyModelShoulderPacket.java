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

package de.markusbordihn.easymodelentities.network.shoulder;

import de.markusbordihn.easymodelentities.registry.ModelResourcePaths;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundEasyModelShoulderPacket(
    int playerId, CompoundTag leftShoulderEntity, CompoundTag rightShoulderEntity)
    implements CustomPacketPayload {

  public static final CustomPacketPayload.Type<ClientboundEasyModelShoulderPacket> TYPE =
      new CustomPacketPayload.Type<>(ModelResourcePaths.modIdentifier("shoulder"));
  public static final StreamCodec<FriendlyByteBuf, ClientboundEasyModelShoulderPacket>
      STREAM_CODEC =
          StreamCodec.of(
              (buffer, packet) -> packet.encode(buffer), ClientboundEasyModelShoulderPacket::decode);

  public ClientboundEasyModelShoulderPacket {
    Objects.requireNonNull(leftShoulderEntity, "leftShoulderEntity");
    Objects.requireNonNull(rightShoulderEntity, "rightShoulderEntity");
  }

  public static ClientboundEasyModelShoulderPacket decode(FriendlyByteBuf buffer) {
    return new ClientboundEasyModelShoulderPacket(
        buffer.readVarInt(), readShoulderEntity(buffer), readShoulderEntity(buffer));
  }

  private static CompoundTag readShoulderEntity(FriendlyByteBuf buffer) {
    CompoundTag shoulderEntity = buffer.readNbt();
    return shoulderEntity == null ? new CompoundTag() : shoulderEntity;
  }

  @Override
  public CustomPacketPayload.Type<ClientboundEasyModelShoulderPacket> type() {
    return TYPE;
  }

  public void encode(FriendlyByteBuf buffer) {
    buffer.writeVarInt(this.playerId);
    buffer.writeNbt(this.leftShoulderEntity);
    buffer.writeNbt(this.rightShoulderEntity);
  }
}
