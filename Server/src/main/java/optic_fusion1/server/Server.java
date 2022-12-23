package optic_fusion1.server;

import net.lenni0451.asmevents.EventManager;
import optic_fusion1.packets.impl.MessagePacket;
import optic_fusion1.server.network.SocketServer;
import optic_fusion1.server.network.listeners.ConnectionListener;
import optic_fusion1.server.network.listeners.PacketListener;
import optic_fusion1.server.network.listeners.event.CommandEventListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class Server {

  private static final Logger LOGGER = LogManager.getLogger();

  public void start() throws IOException {
    SocketServer socketServer = new SocketServer(this);
    socketServer.getPacketRegister().addPacket("message", MessagePacket.class);
    socketServer.addEventListener(new PacketListener(socketServer));
    socketServer.addEventListener(new ConnectionListener(socketServer));
    EventManager.register(new CommandEventListener(socketServer.getCommandHandler()));
    socketServer.bind();
  }
}
