package optic_fusion1.client.network.listeners;

import optic_fusion1.client.network.SocketClient;
import optic_fusion1.packets.IPacket;

public interface ClientEventListener {

  default void onPreConnect() {
  }

  default void onConnectionEstablished() {
  }

  default void onDisconnect() {
  }

  default void onRawPacketReceive(final byte[] packet) {
  }

  default void onPacketReceive(final SocketClient socketClient, final IPacket packet) {
  }
}
