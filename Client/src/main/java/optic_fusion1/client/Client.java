package optic_fusion1.client;

import optic_fusion1.client.network.SocketClient;
import optic_fusion1.client.network.listeners.PacketListener;
import optic_fusion1.packets.impl.MessagePacket;

import java.io.IOException;
import java.net.ConnectException;

public class Client {

  private SocketClient socketClient;

  public Client(String host, int port, String username, String password) throws ConnectException {
    socketClient = new SocketClient(this, host, port, username, password);
    socketClient.getPacketRegister().addPacket("message", MessagePacket.class);
    socketClient.addEventListener(new PacketListener());

    try {
      socketClient.connect();
    } catch (IOException e) {
      throw new ConnectException(e.getLocalizedMessage());
    }
  }

  public SocketClient getSocketClient() {
    return socketClient;
  }
}
