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
import optic_fusion1.common.utils.BCrypt;
import optic_fusion1.packets.OpCode;
import optic_fusion1.packets.impl.MessagePacket;
import optic_fusion1.server.network.ClientConnection;
import optic_fusion1.server.network.SocketServer;
import optic_fusion1.server.utils.RandomString;

import java.util.List;
import java.util.UUID;

public class ResetPassword extends Command {

  private final SocketServer server;

  public ResetPassword(SocketServer server) {
    super("resetpasswd", CommandSide.SERVER, CommandPermissionLevel.OPERATOR, true, false);
    this.server = server;
  }

  @Override
  public boolean execute(CommandSender sender, String commandLabel, List<String> args) {
    if (sender instanceof ClientConnection) {
      return false;
    }

    if(args.size() != 2) {
      sender.sendMessage("Usage: /resetpasswd <uuid> <new password>");
      return false;
    }

    // TODO: allow any user with isOp permission to run the command
    UUID uuid = UUID.fromString(args.get(0));
    String password = args.get(1);
    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
    server.getDatabase().updatePassword(uuid, hashedPassword);
    // TODO: if the user is currently connected, disconnect them and have them login again?
    sender.sendMessage(String.format("Password updated for user with UUID %s", uuid));
    return true;
  }
}
