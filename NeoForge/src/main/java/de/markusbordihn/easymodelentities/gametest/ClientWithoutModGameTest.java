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

import de.markusbordihn.easymodelentities.Constants;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationPlayback;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationSequence;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationTransition;
import de.markusbordihn.easymodelentities.network.animation.ClientboundEasyModelAnimationPacket;
import de.markusbordihn.easymodelentities.network.animation.ClientboundEasyModelAnimationSequencePacket;
import de.markusbordihn.easymodelentities.network.animation.EasyModelAnimationNetwork;
import java.util.Collection;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.network.registration.ChannelAttributes;
import net.neoforged.neoforge.network.registration.NetworkPayloadSetup;

@SuppressWarnings("unused")
@GameTestHolder(Constants.MOD_ID)
public class ClientWithoutModGameTest {

  @GameTestGenerator
  public Collection<TestFunction> clientWithoutModTests() {
    return GameTestHelpers.testFunctions(ClientWithoutModGameTest.class);
  }

  public static void animationPacketsSkipClientWithoutMod(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    ServerPlayer player = makeMockPlayerWithoutMod(helper);
    helper.succeedWhen(
        () -> {
          helper.assertTrue(
              isChunkViewer(level, player), "The mock player has not received its chunk yet!");
          helper.assertFalse(
              player.connection.hasChannel(ClientboundEasyModelAnimationPacket.TYPE)
                  || player.connection.hasChannel(ClientboundEasyModelAnimationSequencePacket.TYPE),
              "The mock player must not have the Easy Model Entities channels!");
          sendAnimationPackets(level, player);
          level.getServer().getPlayerList().remove(player);
        });
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
        .getPlayers(new ChunkPos(player.blockPosition()), false)
        .contains(player);
  }
}
