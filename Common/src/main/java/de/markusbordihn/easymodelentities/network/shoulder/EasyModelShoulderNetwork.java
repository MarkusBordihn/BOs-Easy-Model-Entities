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

package de.markusbordihn.easymodelentities.network.shoulder;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

// Minecraft 1.21.11 syncs only the parrot variant of a shoulder entity to clients, never its NBT.
public final class EasyModelShoulderNetwork {

  public static final int PROTOCOL_VERSION = 1;

  private static final Map<ServerPlayer, ClientboundEasyModelShoulderPacket> SENT_PACKETS =
      new WeakHashMap<>();

  private static volatile Sender sender = Sender.NONE;

  private EasyModelShoulderNetwork() {}

  public static void setSender(Sender sender) {
    EasyModelShoulderNetwork.sender = Objects.requireNonNull(sender, "sender");
  }

  public static void tick(MinecraftServer server) {
    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
      ClientboundEasyModelShoulderPacket packet = packet(player);
      ClientboundEasyModelShoulderPacket sentPacket = SENT_PACKETS.get(player);
      if (packet.equals(sentPacket) || (sentPacket == null && isEmpty(packet))) {
        continue;
      }

      SENT_PACKETS.put(player, packet);
      sender.sendToTrackingAndSelf(player, packet);
    }
  }

  public static void startTracking(Entity trackedEntity, ServerPlayer tracker) {
    if (!(trackedEntity instanceof ServerPlayer player)) {
      return;
    }

    ClientboundEasyModelShoulderPacket packet = packet(player);
    if (!isEmpty(packet)) {
      sender.sendToPlayer(tracker, packet);
    }
  }

  private static ClientboundEasyModelShoulderPacket packet(ServerPlayer player) {
    return new ClientboundEasyModelShoulderPacket(
        player.getId(), player.getShoulderEntityLeft(), player.getShoulderEntityRight());
  }

  private static boolean isEmpty(ClientboundEasyModelShoulderPacket packet) {
    return packet.leftShoulderEntity().isEmpty() && packet.rightShoulderEntity().isEmpty();
  }

  public interface Sender {

    Sender NONE = new Sender() {};

    default void sendToTrackingAndSelf(Entity entity, ClientboundEasyModelShoulderPacket packet) {}

    default void sendToPlayer(ServerPlayer player, ClientboundEasyModelShoulderPacket packet) {}
  }
}
