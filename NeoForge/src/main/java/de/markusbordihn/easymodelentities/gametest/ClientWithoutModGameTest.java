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

import de.markusbordihn.easymodelentities.api.EasyModelEntitiesApi;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationPlayback;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationSequence;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationTransition;
import de.markusbordihn.easymodelentities.entity.EasyModelHostEntity;
import de.markusbordihn.easymodelentities.network.animation.ClientboundEasyModelAnimationPacket;
import de.markusbordihn.easymodelentities.network.animation.ClientboundEasyModelAnimationSequencePacket;
import de.markusbordihn.easymodelentities.network.animation.EasyModelAnimationNetwork;
import de.markusbordihn.easymodelentities.network.shoulder.ClientboundEasyModelShoulderPacket;
import de.markusbordihn.easymodelentities.network.shoulder.EasyModelShoulderNetwork;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.registration.ChannelAttributes;
import net.neoforged.neoforge.network.registration.NetworkPayloadSetup;

public final class ClientWithoutModGameTest {

  private ClientWithoutModGameTest() {}

  public static void animationPacketsSkipClientWithoutMod(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    ServerPlayer player = makeMockPlayerWithoutMod(helper);
    try {
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

      GameTestHelpers.assertTrue(
          helper,
          "Animation packets must skip a nearby client without Easy Model Entities!",
          isChunkViewer(level, player)
              && !player.connection.hasChannel(ClientboundEasyModelAnimationPacket.TYPE)
              && !player.connection.hasChannel(ClientboundEasyModelAnimationSequencePacket.TYPE));
    } finally {
      level.getServer().getPlayerList().remove(player);
    }
  }

  public static void shoulderPacketsSkipClientWithoutMod(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    Optional<EasyModelHostEntity> spawnedEntity = HostEntityGameTestCases.spawnHostEntity(helper);
    if (spawnedEntity.isEmpty()) {
      helper.fail("Could not create host entity for the shoulder packet.");
      return;
    }

    ServerPlayer player = makeMockPlayerWithoutMod(helper);
    try {
      boolean seated = EasyModelEntitiesApi.sitOnShoulder(spawnedEntity.get(), player);
      EasyModelShoulderNetwork.tick(level.getServer());
      EasyModelShoulderNetwork.startTracking(player, player);

      GameTestHelpers.assertTrue(
          helper,
          "Shoulder packets must skip a client without Easy Model Entities!",
          seated && !player.connection.hasChannel(ClientboundEasyModelShoulderPacket.TYPE));
    } finally {
      level.getServer().getPlayerList().remove(player);
    }
  }

  private static ServerPlayer makeMockPlayerWithoutMod(GameTestHelper helper) {
    ServerPlayer player = helper.makeMockServerPlayerInLevel();
    ChannelAttributes.setPayloadSetup(
        player.connection.getConnection(), NetworkPayloadSetup.empty());
    player.setOnGround(true);
    return player;
  }

  private static boolean isChunkViewer(ServerLevel level, ServerPlayer player) {
    return level
        .getChunkSource()
        .chunkMap
        .getPlayers(ChunkPos.containing(player.blockPosition()), false)
        .contains(player);
  }
}
