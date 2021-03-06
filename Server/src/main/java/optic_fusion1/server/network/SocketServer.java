/*
* Copyright (C) 2021 Optic_Fusion1
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package optic_fusion1.server.network;

import optic_fusion1.packets.IPacket;
import optic_fusion1.packets.PacketRegister;
import optic_fusion1.packets.impl.PingPacket;
import optic_fusion1.packets.utils.RSACrypter;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.security.KeyPair;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import me.legrange.haveibeenpwned.HaveIBeenPwndApi;
import me.legrange.haveibeenpwned.HaveIBeenPwndBuilder;
import me.legrange.haveibeenpwned.HaveIBeenPwndException;
import optic_fusion1.server.Database;
import optic_fusion1.server.commands.LoginCommand;
import optic_fusion1.server.commands.RegisterCommand;
import optic_fusion1.server.network.listeners.ServerEventListener;
import optic_fusion1.commandsystem.CommandHandler;
import optic_fusion1.commandsystem.command.Command;
import optic_fusion1.logging.CustomLogger;
import optic_fusion1.server.commands.GenAccCommand;
import optic_fusion1.utils.BCrypt;
import optic_fusion1.utils.Utils;

public class SocketServer {

  private int port = 25565; // Optic_Fusion1 - 0 -> 25565
  private ServerSocket socket;
  private int maxPacketSize = 32767;
  private Thread socketAcceptor;
  private Timer timer;
  private TimerTask pingTimer;
  private final Map<ClientConnection, Thread> clients;
  private final List<ServerEventListener> eventListener;
  private final PacketRegister packetRegister;
  // Optic_Fusion1 - start
  private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
  private static final CommandHandler COMMAND_HANDLER = new CommandHandler();
  private static final Properties SERVER_PROPERTIES = new Properties();
  private static final Database DATABASE = new Database();
  public static final CustomLogger LOGGER = new CustomLogger();
  private boolean loginRequired = true;
  private String serverIP = "";
  private boolean allowInsecurePasswords = false;
  // Optic_Fusion1 - end

  public SocketServer() {
    this.clients = new ConcurrentHashMap<>();
    this.eventListener = new CopyOnWriteArrayList<>();
    this.packetRegister = new PacketRegister();
    registerCommands();
    loadPropertiesFile();
  }

  public void bind() throws IOException {
    if (this.socket != null && this.socket.isBound()) {
      throw new IllegalStateException("Server socket is already bound to port " + this.port);
    }
    this.socket = new ServerSocket();
    this.socket.bind(new InetSocketAddress(serverIP, port));
    this.socketAcceptor = new Thread(() -> {
      while (!this.socketAcceptor.isInterrupted() && this.socket.isBound()) {
        try {
          Socket socket = this.socket.accept();
          ClientConnection clientConnection = new ClientConnection(this, socket);
          { //Call event
            for (ServerEventListener serverEventListener : this.eventListener.toArray(new ServerEventListener[0])) {
              try {
                serverEventListener.onSocketPreConnect(clientConnection);
              } catch (Throwable t) {
                new Exception("Unhandled exception in server event listener", t).printStackTrace();
              }
            }
          }

          Thread clientListener;
          this.clients.put(clientConnection, clientListener = new Thread() {
            @Override
            public void run() {
              DataInputStream dataInputStream = clientConnection.getInputStream();
              while (!this.isInterrupted() && socket.isConnected() && !socketAcceptor.isInterrupted()) {
                try {
                  int packetLength = dataInputStream.readInt();
                  if (packetLength > maxPacketSize) {
                    System.err.println("Client packet is over max size of " + maxPacketSize);
                    try {
                      dataInputStream.skipBytes(packetLength);
                    } catch (Exception e) {
                      new IOException("Could not skip bytes for too large packet " + clientConnection.getAddress().getHostAddress(), e).printStackTrace();
                      break;
                    }
                    continue;
                  }
                  if (packetLength < 0) {
                    throw new EOFException();
                  }
                  byte[] packet = new byte[packetLength];
                  dataInputStream.read(packet);

                  onRawPacketReceive(clientConnection, packet);
                } catch (EOFException | SocketException | SocketTimeoutException e) {
                  break;
                } catch (Throwable e) {
                  new IOException("Could not receive packet for client " + clientConnection.getAddress().getHostAddress(), e).printStackTrace();
                  break;
                }
              }
              onClientDisconnect(clientConnection);
              this.interrupt();
            }
          });
          clientListener.start();
        } catch (Exception e) {
          if (!(e instanceof EOFException) && (!(e instanceof SocketException) || !e.getMessage().equalsIgnoreCase("Socket closed"))) {
            new IOException("Unable to accept client socket", e).printStackTrace();
          }
        }
      }
    });
    this.socketAcceptor.start();

    this.timer = new Timer();
    this.timer.schedule(this.pingTimer = new TimerTask() {
      @Override
      public void run() {
        for (ClientConnection clientConnection : clients.keySet()) {
          if ((clientConnection.getEncryptionKey() != null && clientConnection.getDecryptionKey() != null) || !clientConnection.isUsingEncryption()) {
            clientConnection.sendPacket(new PingPacket(System.currentTimeMillis()));
          }
        }
      }
    }, 0, 10000);
  }

  public void stop() throws IOException {
    this.socketAcceptor.interrupt();
    this.socket.close();

    //Cleanup
    this.pingTimer.cancel();
    for (ClientConnection clientConnection : this.clients.keySet()) {
      clientConnection.terminateConnection();
    }
    this.clients.clear();

    { //Call event
      for (ServerEventListener serverEventListener : this.eventListener.toArray(new ServerEventListener[0])) {
        try {
          serverEventListener.onServerClose();
        } catch (Throwable t) {
          new Exception("Unhandled exception in server event listener", t).printStackTrace();
        }
      }
    }
  }

  public void addEventListener(final ServerEventListener serverEventListener) {
    this.eventListener.add(serverEventListener);
  }

  public void setMaxPacketSize(final int maxPacketSize) {
    this.maxPacketSize = maxPacketSize;
  }

  public int getMaxPacketSize() {
    return this.maxPacketSize;
  }

  public PacketRegister getPacketRegister() {
    return this.packetRegister;
  }

  private void onClientDisconnect(final ClientConnection clientConnection) {
    clientConnection.terminateConnection();
    this.clients.remove(clientConnection);

    { //Call event
      for (ServerEventListener serverEventListener : this.eventListener.toArray(new ServerEventListener[0])) {
        try {
          serverEventListener.onSocketDisconnect(clientConnection);
        } catch (Throwable t) {
          new Exception("Unhandled exception in server event listener", t).printStackTrace();
        }
      }
    }
  }

  private void onRawPacketReceive(final ClientConnection clientConnection, byte[] packet) {
    if (clientConnection.getEncryptionKey() == null && clientConnection.isUsingEncryption()) {
      try {
        if (packet.length == 1) {
          clientConnection.useNoEncryption();
        } else {
          ByteArrayInputStream bais = new ByteArrayInputStream(packet);
          DataInputStream dis = new DataInputStream(bais);
          int rsaKeyLength = dis.readInt();
          int aesKeyLength = dis.readInt();
          clientConnection.setAESKeyLength(aesKeyLength);
          byte[] keyBytes = new byte[dis.readInt()];
          dis.read(keyBytes);
          KeyPair keyPair = RSACrypter.generateKeyPair(rsaKeyLength);
          clientConnection.sendRawPacket(keyPair.getPublic().getEncoded());
          clientConnection.setDecryptionKey(keyPair.getPrivate());
          clientConnection.setEncryptionKey(RSACrypter.initPublicKey(keyBytes));
        }

        { //Call event
          for (ServerEventListener serverEventListener : this.eventListener.toArray(new ServerEventListener[0])) {
            try {
              serverEventListener.onSocketConnectionEstablished(clientConnection);
            } catch (Throwable t) {
              new Exception("Unhandled exception in server event listener", t).printStackTrace();
            }
          }
        }
      } catch (Exception e) {
        new IOException("Could not create encryption key from/for client " + clientConnection.getAddress().getHostAddress(), e).printStackTrace();
        clientConnection.terminateConnection();
      }
      return;
    }

    if (clientConnection.getDecryptionKey() != null && clientConnection.isUsingEncryption()) {
      try {
        packet = RSACrypter.decrypt(clientConnection.getDecryptionKey(), packet);
      } catch (Exception e) {
        new IOException("Could not decrypt packet data for client " + clientConnection.getAddress().getHostAddress(), e).printStackTrace();
      }
    }

    try {
      DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet));
      String packetLabel = dis.readUTF();
      Class<? extends IPacket> packetClass = this.packetRegister.getPacketClass(packetLabel);
      IPacket packetObject = packetClass.newInstance();
      packetObject.readPacketData(dis);

      if (packetObject instanceof PingPacket) {
        clientConnection.updatePing(System.currentTimeMillis() - ((PingPacket) packetObject).getSystemTime());
        return;
      }

      { //Call event
        for (ServerEventListener serverEventListener : this.eventListener.toArray(new ServerEventListener[0])) {
          try {
            serverEventListener.onPacketReceive(clientConnection, packetObject);
          } catch (Throwable t) {
            new Exception("Unhandled exception in server event listener", t).printStackTrace();
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      { //Call event
        for (ServerEventListener serverEventListener : this.eventListener.toArray(new ServerEventListener[0])) {
          try {
            serverEventListener.onRawPacketReceive(clientConnection, packet);
          } catch (Throwable t) {
            new Exception("Unhandled exception in server event listener", t).printStackTrace();
          }
        }
      }
    }
  }

  public ClientConnection[] getClients() {
    return this.clients.keySet().toArray(new ClientConnection[0]);
  }

  public void broadcastRawPacket(final byte[] packet) {
    for (ClientConnection clientConnection : this.clients.keySet()) {
      try {
        clientConnection.sendRawPacket(packet);
      } catch (Exception e) {
        new Exception("Could not broadcast raw packet to client " + clientConnection.getAddress().getHostAddress(), e).printStackTrace();
      }
    }
  }

  public void broadcastPacket(final IPacket packet) {
    for (ClientConnection clientConnection : this.clients.keySet()) {
      try {
        clientConnection.sendPacket(packet);
      } catch (Exception e) {
        new Exception("Could not broadcast packet to client " + clientConnection.getAddress().getHostAddress(), e).printStackTrace();
      }
    }
  }

  // Optic_Fusion1 - start
  public boolean isLoginRequired() {
    return loginRequired;
  }

  public void registerCommands() {
    registerCommand(new LoginCommand(this));
    registerCommand(new RegisterCommand(this));
    registerCommand(new GenAccCommand(this));
  }

  public void registerCommand(Command command) {
    COMMAND_HANDLER.addCommand(command);
  }

  public CommandHandler getCommandHandler() {
    return COMMAND_HANDLER;
  }

  public Database getDatabase() {
    return DATABASE;
  }

  public ScheduledExecutorService getExecutorService() {
    return EXECUTOR_SERVICE;
  }

  public boolean createAccount(ClientConnection client, String userName, String password) {
    if (DATABASE.containsUser(userName)) {
      client.sendMessage("The username '" + userName + "' is already taken");
      LOGGER.info(userName + " is already set");
      return false;
    }
    if (!allowInsecurePasswords) {
      HaveIBeenPwndApi hibp = HaveIBeenPwndBuilder.create("HaveIBeenPwnd").build();
      try {
        if (hibp.isPlainPasswordPwned(password)) {
          client.sendMessage("The password is insecure use something else");
          return false;
        }
      } catch (HaveIBeenPwndException ex) {
        return false;
      }
    }
    DATABASE.insertUser(userName, UUID.randomUUID(), BCrypt.hashpw(password, BCrypt.gensalt()));
    client.sendMessage("Registed the username " + userName);
    LOGGER.info("Registered username " + userName);
    return true;
  }

  private void loadPropertiesFile() {
    File file = new File("server", "server.properties");
    if (!file.exists()) {
      Utils.saveResource(new File("server"), "server.properties", false);
    }
    try {
      SERVER_PROPERTIES.load(new FileInputStream(file));
      serverIP = SERVER_PROPERTIES.getProperty("server-ip");
      port = Integer.parseInt(SERVER_PROPERTIES.getProperty("server-port", "25565"));
      allowInsecurePasswords = Boolean.parseBoolean(SERVER_PROPERTIES.getProperty("allow-insecure-properties", "false"));
    } catch (FileNotFoundException ex) {
      LOGGER.exception(ex);
    } catch (IOException ex) {
      LOGGER.exception(ex);
    }
  }

  public boolean allowInsecurePasswords() {
    return allowInsecurePasswords;
  }
  // Optic_Fusion1 - end
}
