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

package de.markusbordihn.easymodelentities.network.animation;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public final class EasyModelAnimationNetwork {

  private static volatile Sender sender = Sender.NONE;

  private EasyModelAnimationNetwork() {}

  public static void setSender(Sender sender) {
    EasyModelAnimationNetwork.sender = Objects.requireNonNull(sender, "sender");
  }

  public static void send(Entity entity, ClientboundEasyModelAnimationPacket packet) {
    sender.send(entity, packet);
  }

  public static void send(
      ServerLevel level, BlockPos blockPos, ClientboundEasyModelAnimationPacket packet) {
    sender.send(level, blockPos, packet);
  }

  public interface Sender {

    Sender NONE = new Sender() {};

    default void send(Entity entity, ClientboundEasyModelAnimationPacket packet) {}

    default void send(
        ServerLevel level, BlockPos blockPos, ClientboundEasyModelAnimationPacket packet) {}
  }
}
