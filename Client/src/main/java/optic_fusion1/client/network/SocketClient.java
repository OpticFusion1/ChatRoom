package optic_fusion1.client.network;

import optic_fusion1.client.Client;
import optic_fusion1.client.network.listeners.ClientEventListener;
import optic_fusion1.commands.command.CommandSender;
import optic_fusion1.common.data.Message;
import optic_fusion1.common.data.User;
import optic_fusion1.packets.IPacket;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import optic_fusion1.packets.OpCode;
import optic_fusion1.packets.PacketRegister;
import optic_fusion1.packets.impl.MessagePacket;
import optic_fusion1.packets.impl.PingPacket;
import optic_fusion1.packets.utils.RSACrypter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SocketClient implements CommandSender {

  private static final Logger LOGGER = LogManager.getLogger();

  private final Client client;
  private final String ip;
  private final int port;
  private Socket socket;
  private DataInputStream dataInputStream;
  private DataOutputStream dataOutputStream;
  private int maxPacketSize = 32767;
  private User user;

  private Thread packetListener;
  private final List<ClientEventListener> eventListener;

  private PublicKey encryptionKey;
  private PrivateKey decryptionKey;

  private final PacketRegister packetRegister;

  private final String username;
  private final String password;

  public SocketClient(final Client client, final String ip, final int port, final String username,
      final String password) {
    this.client = client;
    this.ip = ip;
    this.port = port;
    this.username = username;
    this.password = password;

    this.eventListener = new CopyOnWriteArrayList<>();
    this.packetRegister = new PacketRegister();
  }

  public void connect() throws IOException {
    if (this.isConnected()) {
      throw new IllegalStateException("Client socket is already connected to address " + this.ip);
    }

    this.socket = new Socket();
    this.socket.setTcpNoDelay(true);
    this.socket.connect(new InetSocketAddress(this.ip, this.port));
    this.dataInputStream = new DataInputStream(this.socket.getInputStream());
    this.dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
    this.packetListener = new Thread(() -> {
      while (!this.packetListener.isInterrupted() && this.socket.isConnected()) {
        try {
          int packetLength = this.dataInputStream.readInt();
          if (packetLength > this.maxPacketSize) {
            LOGGER.warn("Server packet is over max size of " + maxPacketSize);
            try {
              dataInputStream.skipBytes(packetLength);
            } catch (Exception e) {
              new IOException("Could not skip bytes for too large packet", e).printStackTrace();
              break;
            }
            continue;
          }
          if (packetLength < 0) {
            throw new EOFException();
          }
          byte[] packet = new byte[packetLength];
          dataInputStream.read(packet);

          this.onPacketReceive(packet);
        } catch (EOFException | SocketException | SocketTimeoutException e) {
          break;
        } catch (Throwable e) {
          new IOException("Could not receive packet", e).printStackTrace();
          break;
        }
      }
      this.onDisconnect();
    });
    this.packetListener.start();

    { // Encryption
      int rsaKeyLength = RSACrypter.getRSAKeyLength();

      try {
        KeyPair keyPair = RSACrypter.generateKeyPair(rsaKeyLength);
        this.decryptionKey = keyPair.getPrivate();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(rsaKeyLength);
        dos.writeInt(RSACrypter.getAESKeyLength());
        dos.writeInt(keyPair.getPublic().getEncoded().length);
        dos.write(keyPair.getPublic().getEncoded());
        this.sendRawPacket(baos.toByteArray());
      } catch (Exception e) {
        new IOException("Could not create encryption key for server", e).printStackTrace();
        this.disconnect();
      }
    }

    { // Call event
      for (ClientEventListener clientEventListener : this.eventListener.toArray(new ClientEventListener[0])) {
        try {
          clientEventListener.onPreConnect();
        } catch (Throwable t) {
          new Exception("Unhandled exception in client event listener", t).printStackTrace();
        }
      }
    }
    handleInput();
  }

  public void disconnect() {
    try {
      this.socket.shutdownInput();
      this.socket.close();
    } catch (Exception ignored) {
    }
  }

  public boolean isConnected() {
    return this.socket != null && this.socket.isConnected() && this.packetListener.isAlive()
        && !this.packetListener.isInterrupted();
  }

  public void addEventListener(final ClientEventListener clientEventListener) {
    this.eventListener.add(clientEventListener);
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

  private void onDisconnect() {
    try {
      this.socket.close();
    } catch (Exception ignored) {
    }
    this.packetListener.interrupt();

    this.encryptionKey = null;
    this.decryptionKey = null;

    { // Call event
      for (ClientEventListener clientEventListener : this.eventListener.toArray(new ClientEventListener[0])) {
        try {
          clientEventListener.onDisconnect();
        } catch (Throwable t) {
          new Exception("Unhandled exception in client event listener", t).printStackTrace();
        }
      }
    }
  }

  private void onPacketReceive(byte[] packet) {
    if (this.encryptionKey == null) {
      try {
        this.encryptionKey = RSACrypter.initPublicKey(packet);

        { // Call event
          for (ClientEventListener clientEventListener : this.eventListener.toArray(new ClientEventListener[0])) {
            try {
              clientEventListener.onConnectionEstablished();
            } catch (Throwable t) {
              new Exception("Unhandled exception in client event listener", t).printStackTrace();
            }
          }
        }
      } catch (Exception e) {
        new IOException("Could not create encryption key", e).printStackTrace();
        this.disconnect();
      }
      return;
    }

    if (this.decryptionKey != null) {
      try {
        packet = RSACrypter.decrypt(this.decryptionKey, packet);
      } catch (Exception e) {
        new IOException("Could not decrypt packet data", e).printStackTrace();
      }
    }

    try {
      DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet));
      String packetLabel = dis.readUTF();
      Class<? extends IPacket> packetClass = this.packetRegister.getPacketClass(packetLabel);
      IPacket packetObject = packetClass.newInstance();
      packetObject.readPacketData(dis);

      if (packetObject instanceof PingPacket) {
        this.sendPacket(packetObject);
        return;
      }

      { // Call event
        for (ClientEventListener clientEventListener : this.eventListener.toArray(new ClientEventListener[0])) {
          try {
            clientEventListener.onPacketReceive(this, packetObject);
          } catch (Throwable t) {
            new Exception("Unhandled exception in client event listener", t).printStackTrace();
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      { // Call event
        for (ClientEventListener clientEventListener : this.eventListener.toArray(new ClientEventListener[0])) {
          try {
            clientEventListener.onRawPacketReceive(packet);
          } catch (Throwable t) {
            new Exception("Unhandled exception in client event listener", t).printStackTrace();
          }
        }
      }
    }
  }

  public void sendRawPacket(byte[] data) throws IOException {
    if (!this.isConnected()) {
      throw new IllegalStateException("Client is not connected to a server");
    }

    if (this.encryptionKey != null) {
      try {
        data = RSACrypter.encrypt(this.encryptionKey, data);
      } catch (Exception e) {
        new IOException("Could not decrypt packet data", e).printStackTrace();
      }
    }
    if (data.length > this.maxPacketSize) {
      throw new RuntimeException("Packet size over maximum: " + data.length + " > " + this.maxPacketSize);
    }
    this.dataOutputStream.writeInt(data.length);
    this.dataOutputStream.write(data);
  }

  @Override
  public void sendPacket(final IPacket packet) {
    if (!this.isConnected()) {
      throw new IllegalStateException("Client is not connected to a server");
    }

    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      DataOutputStream dos = new DataOutputStream(baos);
      dos.writeUTF(this.packetRegister.getPacketLabel(packet.getClass()));
      packet.writePacketData(dos);
      this.sendRawPacket(baos.toByteArray());
    } catch (Exception e) {
      new IOException("Could not serialize packet", e).printStackTrace();
      disconnect();
    }
  }

  @Override
  public void sendMessage(String message) {
    LOGGER.info(message);
  }

  public void handleInput() {
    Scanner scanner = new Scanner(System.in);
    while (isConnected()) {
      String msg = scanner.nextLine();
      // prevent sending empty messages
      if (msg.isEmpty() || msg.startsWith(" ")) {
        continue;
      }
      sendPacket(new MessagePacket(OpCode.MESSAGE, new Message(this.user, msg).serialize(),
          MessagePacket.MessageChatType.USER));
    }
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

}
