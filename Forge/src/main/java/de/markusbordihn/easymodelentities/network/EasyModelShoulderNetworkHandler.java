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
import de.markusbordihn.easymodelentities.registry.ModelResourcePaths;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public final class EasyModelShoulderNetworkHandler {

  private static final SimpleChannel CHANNEL =
      ChannelBuilder.named(ModelResourcePaths.modIdentifier("shoulder"))
          .networkProtocolVersion(EasyModelShoulderNetwork.PROTOCOL_VERSION)
          .optional()
          .simpleChannel();

  private EasyModelShoulderNetworkHandler() {}

  public static void register() {
    CHANNEL
        .messageBuilder(ClientboundEasyModelShoulderPacket.class, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(ClientboundEasyModelShoulderPacket::encode)
        .decoder(ClientboundEasyModelShoulderPacket::decode)
        .consumerMainThread(EasyModelShoulderNetworkHandler::handle)
        .add();
    EasyModelShoulderNetwork.setSender(new ForgeSender());
    TickEvent.ServerTickEvent.Post.BUS.addListener(
        event -> EasyModelShoulderNetwork.tick(event.server()));
    PlayerEvent.StartTracking.BUS.addListener(EasyModelShoulderNetworkHandler::startTracking);
  }

  private static void startTracking(PlayerEvent.StartTracking event) {
    if (event.getEntity() instanceof ServerPlayer tracker) {
      EasyModelShoulderNetwork.startTracking(event.getTarget(), tracker);
    }
  }

  private static void handle(
      ClientboundEasyModelShoulderPacket packet, CustomPayloadEvent.Context context) {
    if (FMLEnvironment.dist.isClient()) {
      EasyModelShoulderPacketHandler.handle(packet);
    }
    context.setPacketHandled(true);
  }

  private static final class ForgeSender implements EasyModelShoulderNetwork.Sender {

    @Override
    public void sendToTrackingAndSelf(Entity entity, ClientboundEasyModelShoulderPacket packet) {
      CHANNEL.send(packet, PacketDistributor.TRACKING_ENTITY_AND_SELF.with(entity));
    }

    @Override
    public void sendToPlayer(ServerPlayer player, ClientboundEasyModelShoulderPacket packet) {
      CHANNEL.send(packet, PacketDistributor.PLAYER.with(player));
    }
  }
}
