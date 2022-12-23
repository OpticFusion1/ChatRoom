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
package optic_fusion1.server.commands;

import optic_fusion1.commands.command.Command;
import optic_fusion1.commands.command.CommandPermissionLevel;
import optic_fusion1.commands.command.CommandSender;
import optic_fusion1.commands.command.CommandSide;
import optic_fusion1.common.data.Message;
import optic_fusion1.packets.OpCode;
import optic_fusion1.packets.impl.MessagePacket;
import optic_fusion1.server.ServerCommandSender;
import optic_fusion1.server.network.ClientConnection;
import optic_fusion1.server.network.SocketServer;
import optic_fusion1.server.utils.RandomString;

import java.util.List;

public class GenAccCommand extends Command {

  private final SocketServer server;

  public GenAccCommand(SocketServer server) {
    super("genacc", CommandSide.SERVER, CommandPermissionLevel.OPERATOR, true, false);
    this.server = server;
  }

  @Override
  public boolean execute(CommandSender sender, String commandLabel, List<String> args) {
    if (sender instanceof ClientConnection) {
      return false;
    }

    // TODO: allow any user with isOp permission to run the command

    RandomString randomString = new RandomString();
    String username = randomString.nextString();
    String password = randomString.nextString();
    boolean created = server.createAccount(sender, username, password);
    if (created) {
      sender.sendPacket(new MessagePacket(OpCode.MESSAGE, new Message(null, "Username: " + username + " Password: " + password).serialize(), MessagePacket.MessageChatType.SYSTEM));
    }
    return true;
  }

}
