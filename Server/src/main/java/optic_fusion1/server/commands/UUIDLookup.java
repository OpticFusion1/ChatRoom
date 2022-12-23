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
import optic_fusion1.server.network.ClientConnection;
import optic_fusion1.server.network.SocketServer;

import java.util.List;
import java.util.UUID;

public class UUIDLookup extends Command {

  private final SocketServer server;

  public UUIDLookup(SocketServer server) {
    super("uuidlookup", CommandSide.SERVER, CommandPermissionLevel.OPERATOR, true, false);
    this.server = server;
  }

  @Override
  public boolean execute(CommandSender sender, String commandLabel, List<String> args) {
    if (sender instanceof ClientConnection) {
      return false;
    }

    if(args.size() != 1) {
      sender.sendMessage("Usage: /uuidlookup <username>");
      return false;
    }

    // TODO: allow any user with isOp permission to run the command
    String username = args.get(0);
    UUID uuid = server.getDatabase().getUUID(username);
    if(uuid != null) {
      sender.sendMessage(String.format("UUID for user %s is %s", username, uuid));
    } else {
      sender.sendMessage("Failed to find a user by that username");
    }
    return true;
  }
}
