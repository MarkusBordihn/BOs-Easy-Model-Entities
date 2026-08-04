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

import de.markusbordihn.easymodelentities.client.network.EasyModelAnimationPacketHandler;
import de.markusbordihn.easymodelentities.network.animation.ClientboundEasyModelAnimationPacket;
import de.markusbordihn.easymodelentities.network.animation.EasyModelAnimationNetwork;
import de.markusbordihn.easymodelentities.registry.ModelResourcePaths;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

public final class EasyModelAnimationNetworkHandler {

  public static final ResourceLocation PACKET_ID =
      ModelResourcePaths.modResourceLocation("animation_playback");

  private EasyModelAnimationNetworkHandler() {}

  public static void registerServer() {
    EasyModelAnimationNetwork.setSender(new FabricSender());
  }

  public static void registerClient() {
    ClientPlayNetworking.registerGlobalReceiver(
        PACKET_ID,
        (client, handler, buffer, responseSender) -> {
          ClientboundEasyModelAnimationPacket packet =
              ClientboundEasyModelAnimationPacket.decode(buffer);
          client.execute(() -> EasyModelAnimationPacketHandler.handle(packet));
        });
  }

  private static void send(ServerPlayer player, ClientboundEasyModelAnimationPacket packet) {
    FriendlyByteBuf buffer = PacketByteBufs.create();
    packet.encode(buffer);
    ServerPlayNetworking.send(player, PACKET_ID, buffer);
  }

  private static final class FabricSender implements EasyModelAnimationNetwork.Sender {

    @Override
    public void send(Entity entity, ClientboundEasyModelAnimationPacket packet) {
      Set<ServerPlayer> recipients = new HashSet<>(PlayerLookup.tracking(entity));
      if (entity instanceof ServerPlayer serverPlayer) {
        recipients.add(serverPlayer);
      }
      recipients.forEach(player -> EasyModelAnimationNetworkHandler.send(player, packet));
    }

    @Override
    public void send(
        ServerLevel level, BlockPos blockPos, ClientboundEasyModelAnimationPacket packet) {
      PlayerLookup.tracking(level, new ChunkPos(blockPos))
          .forEach(player -> EasyModelAnimationNetworkHandler.send(player, packet));
    }
  }
}
