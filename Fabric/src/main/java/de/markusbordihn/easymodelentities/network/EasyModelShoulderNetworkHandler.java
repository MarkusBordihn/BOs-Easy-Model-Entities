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
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class EasyModelShoulderNetworkHandler {

  private EasyModelShoulderNetworkHandler() {}

  public static void registerServer() {
    PayloadTypeRegistry.playS2C()
        .register(
            ClientboundEasyModelShoulderPacket.TYPE,
            ClientboundEasyModelShoulderPacket.STREAM_CODEC);
    EasyModelShoulderNetwork.setSender(new FabricSender());
    ServerTickEvents.END_SERVER_TICK.register(EasyModelShoulderNetwork::tick);
    EntityTrackingEvents.START_TRACKING.register(EasyModelShoulderNetwork::startTracking);
  }

  public static void registerClient() {
    ClientPlayNetworking.registerGlobalReceiver(
        ClientboundEasyModelShoulderPacket.TYPE,
        (packet, context) -> EasyModelShoulderPacketHandler.handle(packet));
  }

  private static final class FabricSender implements EasyModelShoulderNetwork.Sender {

    @Override
    public void sendToTrackingAndSelf(Entity entity, ClientboundEasyModelShoulderPacket packet) {
      PlayerLookup.tracking(entity).forEach(player -> ServerPlayNetworking.send(player, packet));
      if (entity instanceof ServerPlayer serverPlayer) {
        ServerPlayNetworking.send(serverPlayer, packet);
      }
    }

    @Override
    public void sendToPlayer(ServerPlayer player, ClientboundEasyModelShoulderPacket packet) {
      ServerPlayNetworking.send(player, packet);
    }
  }
}
