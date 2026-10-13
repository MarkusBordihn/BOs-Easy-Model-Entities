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

import de.markusbordihn.easymodelentities.client.network.EasyModelShoulderPacketHandler;
import de.markusbordihn.easymodelentities.network.shoulder.ClientboundEasyModelShoulderPacket;
import de.markusbordihn.easymodelentities.network.shoulder.EasyModelShoulderNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class EasyModelShoulderNetworkHandler {

  private EasyModelShoulderNetworkHandler() {}

  public static void register(RegisterPayloadHandlersEvent event) {
    event
        .registrar(String.valueOf(EasyModelShoulderNetwork.PROTOCOL_VERSION))
        .optional()
        .playToClient(
            ClientboundEasyModelShoulderPacket.TYPE,
            ClientboundEasyModelShoulderPacket.STREAM_CODEC,
            (packet, context) -> {
              if (FMLEnvironment.getDist() == Dist.CLIENT) {
                EasyModelShoulderPacketHandler.handle(packet);
              }
            });
    EasyModelShoulderNetwork.setSender(new NeoForgeSender());
  }

  public static void serverTick(ServerTickEvent.Post event) {
    EasyModelShoulderNetwork.tick(event.getServer());
  }

  public static void startTracking(PlayerEvent.StartTracking event) {
    if (event.getEntity() instanceof ServerPlayer tracker) {
      EasyModelShoulderNetwork.startTracking(event.getTarget(), tracker);
    }
  }

  private static final class NeoForgeSender implements EasyModelShoulderNetwork.Sender {

    @Override
    public void sendToTrackingAndSelf(Entity entity, ClientboundEasyModelShoulderPacket packet) {
      PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, packet);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, ClientboundEasyModelShoulderPacket packet) {
      PacketDistributor.sendToPlayer(player, packet);
    }
  }
}
