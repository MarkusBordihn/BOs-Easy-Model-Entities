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
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class EasyModelAnimationNetworkHandler {

  private static final String PROTOCOL_VERSION = "1";
  private static final SimpleChannel CHANNEL =
      NetworkRegistry.newSimpleChannel(
          ModelResourcePaths.modResourceLocation("animation_playback"),
          () -> PROTOCOL_VERSION,
          PROTOCOL_VERSION::equals,
          PROTOCOL_VERSION::equals);

  private EasyModelAnimationNetworkHandler() {}

  public static void register(FMLCommonSetupEvent event) {
    CHANNEL
        .messageBuilder(
            ClientboundEasyModelAnimationPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(ClientboundEasyModelAnimationPacket::encode)
        .decoder(ClientboundEasyModelAnimationPacket::decode)
        .consumerMainThread(EasyModelAnimationNetworkHandler::handle)
        .add();
    EasyModelAnimationNetwork.setSender(new ForgeSender());
  }

  private static void handle(
      ClientboundEasyModelAnimationPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
    NetworkEvent.Context context = contextSupplier.get();
    DistExecutor.unsafeRunWhenOn(
        Dist.CLIENT, () -> () -> EasyModelAnimationPacketHandler.handle(packet));
    context.setPacketHandled(true);
  }

  private static final class ForgeSender implements EasyModelAnimationNetwork.Sender {

    @Override
    public void send(Entity entity, ClientboundEasyModelAnimationPacket packet) {
      CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), packet);
    }

    @Override
    public void send(
        ServerLevel level, BlockPos blockPos, ClientboundEasyModelAnimationPacket packet) {
      CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(blockPos)), packet);
    }
  }
}
