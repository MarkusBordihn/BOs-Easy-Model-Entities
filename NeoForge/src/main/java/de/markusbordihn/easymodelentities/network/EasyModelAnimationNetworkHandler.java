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
import de.markusbordihn.easymodelentities.network.animation.ClientboundEasyModelAnimationSequencePacket;
import de.markusbordihn.easymodelentities.network.animation.EasyModelAnimationNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class EasyModelAnimationNetworkHandler {

  private EasyModelAnimationNetworkHandler() {}

  public static void register(RegisterPayloadHandlersEvent event) {
    PayloadRegistrar registrar =
        event.registrar(String.valueOf(EasyModelAnimationNetwork.PROTOCOL_VERSION)).optional();
    registrar.playToClient(
        ClientboundEasyModelAnimationPacket.TYPE,
        ClientboundEasyModelAnimationPacket.STREAM_CODEC,
        (packet, context) -> {
          if (FMLEnvironment.getDist() == Dist.CLIENT) {
            EasyModelAnimationPacketHandler.handle(packet);
          }
        });
    registrar.playToClient(
        ClientboundEasyModelAnimationSequencePacket.TYPE,
        ClientboundEasyModelAnimationSequencePacket.STREAM_CODEC,
        (packet, context) -> {
          if (FMLEnvironment.getDist() == Dist.CLIENT) {
            EasyModelAnimationPacketHandler.handle(packet);
          }
        });
    EasyModelAnimationNetwork.setSender(new NeoForgeSender());
  }

  private static final class NeoForgeSender implements EasyModelAnimationNetwork.Sender {

    @Override
    public void send(Entity entity, ClientboundEasyModelAnimationPacket packet) {
      PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, packet);
    }

    @Override
    public void send(
        ServerLevel level, BlockPos blockPos, ClientboundEasyModelAnimationPacket packet) {
      PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(blockPos), packet);
    }

    @Override
    public void send(Entity entity, ClientboundEasyModelAnimationSequencePacket packet) {
      PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, packet);
    }

    @Override
    public void send(
        ServerLevel level, BlockPos blockPos, ClientboundEasyModelAnimationSequencePacket packet) {
      PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(blockPos), packet);
    }
  }
}
