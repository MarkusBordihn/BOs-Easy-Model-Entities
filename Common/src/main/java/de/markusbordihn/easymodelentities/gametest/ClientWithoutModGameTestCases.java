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

package de.markusbordihn.easymodelentities.gametest;

import com.mojang.authlib.GameProfile;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationPlayback;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationSequence;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationTransition;
import de.markusbordihn.easymodelentities.network.animation.ClientboundEasyModelAnimationPacket;
import de.markusbordihn.easymodelentities.network.animation.ClientboundEasyModelAnimationSequencePacket;
import de.markusbordihn.easymodelentities.network.animation.EasyModelAnimationNetwork;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.AttributeKey;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public final class ClientWithoutModGameTestCases {

  private static final AttributeKey<String> FORGE_NETWORK_VERSION =
      AttributeKey.valueOf("fml:netversion");
  private static final String FORGE_VANILLA_CLIENT_NETWORK_VERSION = "NONE";

  private ClientWithoutModGameTestCases() {}

  public static void animationPacketsReachClientWithoutMod(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    ServerPlayer player = makeMockPlayerWithoutMod(level);
    helper.succeedWhen(
        () -> {
          helper.assertTrue(
              isChunkViewer(level, player), "The mock player has not received its chunk yet!");
          sendAnimationPackets(level, player);
          level.getServer().getPlayerList().remove(player);
        });
  }

  private static ServerPlayer makeMockPlayerWithoutMod(ServerLevel level) {
    ServerPlayer player =
        new ServerPlayer(
            level.getServer(), level, new GameProfile(UUID.randomUUID(), "test-mock-player"));
    Connection connection = new Connection(PacketFlow.SERVERBOUND);
    new EmbeddedChannel(
        new ChannelInitializer<>() {
          @Override
          protected void initChannel(Channel channel) {
            channel.attr(FORGE_NETWORK_VERSION).set(FORGE_VANILLA_CLIENT_NETWORK_VERSION);
            channel.pipeline().addLast("packet_handler", connection);
          }
        });
    level.getServer().getPlayerList().placeNewPlayer(connection, player);
    return player;
  }

  private static void sendAnimationPackets(ServerLevel level, ServerPlayer player) {
    BlockPos blockPos = player.blockPosition();
    EasyModelAnimationSequence sequence =
        EasyModelAnimationSequence.of(EasyModelAnimation.WALK, EasyModelAnimation.ATTACK);
    EasyModelAnimationNetwork.send(
        player,
        ClientboundEasyModelAnimationPacket.playEntity(
            player.getId(),
            EasyModelAnimation.WALK,
            EasyModelAnimationPlayback.DEFAULT,
            EasyModelAnimationTransition.DEFAULT));
    EasyModelAnimationNetwork.send(
        level,
        blockPos,
        ClientboundEasyModelAnimationPacket.playBlockEntity(
            blockPos,
            EasyModelAnimation.WALK,
            EasyModelAnimationPlayback.DEFAULT,
            EasyModelAnimationTransition.DEFAULT));
    EasyModelAnimationNetwork.send(
        player, ClientboundEasyModelAnimationSequencePacket.forEntity(player.getId(), sequence));
    EasyModelAnimationNetwork.send(
        level,
        blockPos,
        ClientboundEasyModelAnimationSequencePacket.forBlockEntity(blockPos, sequence));
  }

  private static boolean isChunkViewer(ServerLevel level, ServerPlayer player) {
    return level
        .getChunkSource()
        .chunkMap
        .getPlayers(new ChunkPos(player.blockPosition()), false)
        .contains(player);
  }
}
