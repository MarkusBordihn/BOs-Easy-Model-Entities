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

package de.markusbordihn.easymodelentities.client.network;

import de.markusbordihn.easymodelentities.api.client.EasyModelEntitiesClientApi;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationSequence;
import de.markusbordihn.easymodelentities.network.animation.ClientboundEasyModelAnimationPacket;
import de.markusbordihn.easymodelentities.network.animation.ClientboundEasyModelAnimationSequencePacket;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class EasyModelAnimationPacketHandler {

  private EasyModelAnimationPacketHandler() {}

  public static void handle(ClientboundEasyModelAnimationPacket packet) {
    ClientLevel level = Minecraft.getInstance().level;
    if (level == null) {
      return;
    }

    switch (packet.targetType()) {
      case ENTITY -> handleEntity(level.getEntity((int) packet.targetValue()), packet);
      case BLOCK_ENTITY ->
          handleBlockEntity(level.getBlockEntity(BlockPos.of(packet.targetValue())), packet);
    }
  }

  private static void handleEntity(Entity entity, ClientboundEasyModelAnimationPacket packet) {
    if (entity == null) {
      return;
    }

    switch (packet.operation()) {
      case PLAY ->
          animation(packet)
              .ifPresent(
                  animation ->
                      EasyModelEntitiesClientApi.playAnimation(
                          entity, animation, packet.playback(), packet.transition()));
      case STOP -> EasyModelEntitiesClientApi.stopAnimation(entity, packet.transition());
      case RESTART -> EasyModelEntitiesClientApi.restartAnimation(entity);
    }
  }

  private static void handleBlockEntity(
      BlockEntity blockEntity, ClientboundEasyModelAnimationPacket packet) {
    if (blockEntity == null) {
      return;
    }

    switch (packet.operation()) {
      case PLAY ->
          animation(packet)
              .ifPresent(
                  animation ->
                      EasyModelEntitiesClientApi.playAnimation(
                          blockEntity, animation, packet.playback(), packet.transition()));
      case STOP -> EasyModelEntitiesClientApi.stopAnimation(blockEntity, packet.transition());
      case RESTART -> EasyModelEntitiesClientApi.restartAnimation(blockEntity);
    }
  }

  private static Optional<EasyModelAnimation> animation(
      ClientboundEasyModelAnimationPacket packet) {
    return EasyModelAnimation.parse(packet.animation());
  }

  public static void handle(ClientboundEasyModelAnimationSequencePacket packet) {
    ClientLevel level = Minecraft.getInstance().level;
    if (level == null) {
      return;
    }

    Optional<EasyModelAnimationSequence> sequence = packet.toSequence();
    if (sequence.isEmpty()) {
      return;
    }

    switch (packet.targetType()) {
      case ENTITY -> {
        Entity entity = level.getEntity((int) packet.targetValue());
        if (entity != null) {
          EasyModelEntitiesClientApi.playAnimationSequence(entity, sequence.get());
        }
      }
      case BLOCK_ENTITY -> {
        BlockEntity blockEntity = level.getBlockEntity(BlockPos.of(packet.targetValue()));
        if (blockEntity != null) {
          EasyModelEntitiesClientApi.playAnimationSequence(blockEntity, sequence.get());
        }
      }
    }
  }
}
