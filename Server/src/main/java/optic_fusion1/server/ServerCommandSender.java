package optic_fusion1.server;

import net.lenni0451.asmevents.EventManager;
import optic_fusion1.commands.command.CommandSender;
import optic_fusion1.common.data.Message;
import optic_fusion1.common.data.User;
import optic_fusion1.packets.IPacket;
import optic_fusion1.packets.OpCode;
import optic_fusion1.packets.impl.MessagePacket;
import optic_fusion1.server.network.events.CommandEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerCommandSender implements CommandSender {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void sendMessage(final String s) {
        LOGGER.info(s);
    }

    @Override
    public void sendPacket(final IPacket packet) {
        if(packet instanceof MessagePacket messagePacket) {
            OpCode opCode = messagePacket.getOpCode();
            if(opCode.equals(OpCode.MESSAGE)) {
                Message message = Message.deserialize(messagePacket.getMessage());
                LOGGER.info(message.getContent());
            }
        }
    }
}
