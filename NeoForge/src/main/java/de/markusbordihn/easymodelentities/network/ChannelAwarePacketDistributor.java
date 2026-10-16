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

package de.markusbordihn.easymodelentities.network;

import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ChannelAwarePacketDistributor {

  private ChannelAwarePacketDistributor() {}

  public static void sendToPlayersNearEntityAndSelf(Entity entity, CustomPacketPayload packet) {
    if (!(entity.level() instanceof ServerLevel serverLevel)) {
      return;
    }

    Set<ServerPlayer> recipients =
        new LinkedHashSet<>(
            serverLevel.getChunkSource().chunkMap.getPlayers(entity.chunkPosition(), false));
    if (entity instanceof ServerPlayer player) {
      recipients.add(player);
    }
    for (ServerPlayer recipient : recipients) {
      sendToPlayer(recipient, packet);
    }
  }

  public static void sendToPlayersTrackingChunk(
      ServerLevel level, ChunkPos chunkPos, CustomPacketPayload packet) {
    for (ServerPlayer player : level.getChunkSource().chunkMap.getPlayers(chunkPos, false)) {
      sendToPlayer(player, packet);
    }
  }

  public static void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
    if (player.connection.hasChannel(packet)) {
      PacketDistributor.sendToPlayer(player, packet);
    }
  }
}
