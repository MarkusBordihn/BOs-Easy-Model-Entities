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
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public final class EasyModelAnimationNetworkHandler {

  private static final SimpleChannel CHANNEL =
      ChannelBuilder.named(ModelResourcePaths.modIdentifier("animation_playback"))
          .networkProtocolVersion(EasyModelAnimationNetwork.PROTOCOL_VERSION)
          .optional()
          .simpleChannel();

  private EasyModelAnimationNetworkHandler() {}

  public static void register() {
    CHANNEL
        .messageBuilder(ClientboundEasyModelAnimationPacket.class, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(ClientboundEasyModelAnimationPacket::encode)
        .decoder(ClientboundEasyModelAnimationPacket::decode)
        .consumerMainThread(EasyModelAnimationNetworkHandler::handle)
        .add();
    EasyModelAnimationNetwork.setSender(new ForgeSender());
  }

  private static void handle(
      ClientboundEasyModelAnimationPacket packet, CustomPayloadEvent.Context context) {
    if (FMLEnvironment.dist.isClient()) {
      EasyModelAnimationPacketHandler.handle(packet);
    }
    context.setPacketHandled(true);
  }

  private static final class ForgeSender implements EasyModelAnimationNetwork.Sender {

    @Override
    public void send(Entity entity, ClientboundEasyModelAnimationPacket packet) {
      CHANNEL.send(packet, PacketDistributor.TRACKING_ENTITY_AND_SELF.with(entity));
    }

    @Override
    public void send(
        ServerLevel level, BlockPos blockPos, ClientboundEasyModelAnimationPacket packet) {
      CHANNEL.send(packet, PacketDistributor.TRACKING_CHUNK.with(level.getChunkAt(blockPos)));
    }
  }
}
