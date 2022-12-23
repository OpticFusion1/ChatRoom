package optic_fusion1.client.network.listeners;

import optic_fusion1.client.network.SocketClient;
import optic_fusion1.common.data.Message;
import optic_fusion1.common.data.User;
import optic_fusion1.packets.IPacket;
import optic_fusion1.packets.OpCode;
import optic_fusion1.packets.impl.MessagePacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PacketListener implements ClientEventListener {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onPacketReceive(SocketClient socketClient, IPacket packet) {
        if (packet instanceof MessagePacket messagePacket) {
            OpCode opCode = messagePacket.getOpCode();

            switch (opCode) {
                case LOGIN_REQUIRED -> {
                    if (socketClient.getUsername() != null && socketClient.getPassword() != null) {
                        LOGGER.info("Trying to login...");
                        socketClient.sendPacket(new MessagePacket(OpCode.MESSAGE, new Message(socketClient.getUser(), String.format("/login %s %s", socketClient.getUsername(), socketClient.getPassword())).serialize(), MessagePacket.MessageChatType.USER));
                    } else {
                        LOGGER.info("This server requires you to login before you can chat.");
                    }
                }
                case LOGIN -> {
                    User user = User.deserialize(messagePacket.getMessage());
                    // TODO: we should probably track the known clients in a hashmap somewhere
                    LOGGER.info(String.format("== %s has joined ==", user.getUsername()));
                }
                case LOGGED_IN -> {
                    User user = User.deserialize(messagePacket.getMessage());
                    socketClient.setUser(user);
                    LOGGER.info(String.format("== Logged in as %s ==", user.getUsername()));
                }
                case DISCONNECT -> {
                    User user = User.deserialize(messagePacket.getMessage());
                    LOGGER.info(String.format("== %s has disconnected ==", user.getUsername()));
                }
                case MESSAGE -> {
                    Message message = Message.deserialize(messagePacket.getMessage());
                    switch (messagePacket.getChatType()) {
                        case USER -> {
                            if (message.getUser().getUuid().equals(socketClient.getUser().getUuid())) {
                                // the client receiving the message is also the client that sent the message
                                LOGGER.info(String.format("* You: %s", message.getContent()));
                            } else {
                                LOGGER.info(String.format("%s: %s", message.getUser().getUsername(), message.getContent()));
                                // play notification sound
//                                try {
//                                    Utils.playSound("ping");
//                                } catch (UnsupportedAudioFileException | LineUnavailableException | IOException | InterruptedException e) {
//                                    e.printStackTrace();
//                                }
                            }
                        }
                        case SYSTEM -> {
                            LOGGER.info(String.format("[System]: %s", message.getContent()));
                        }
                    }
                }
                case CONNECT -> LOGGER.info("CONNECT");
                case UNKNOWN -> LOGGER.info("UNKNOWN");
            }
        }
    }

    @Override
    public void onConnectionEstablished() {
        LOGGER.info("=== Connected to server ===");
    }

    @Override
    public void onDisconnect() {
        LOGGER.info("=== Disconnected ===");
    }
}