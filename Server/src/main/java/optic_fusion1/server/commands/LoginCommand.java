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
import optic_fusion1.commands.command.CommandSender;
import optic_fusion1.commands.command.CommandSide;
import optic_fusion1.common.data.Message;
import optic_fusion1.packets.OpCode;
import optic_fusion1.packets.impl.MessagePacket;
import optic_fusion1.server.Database;
import optic_fusion1.server.ServerCommandSender;
import optic_fusion1.server.network.ClientConnection;
import optic_fusion1.server.network.SocketServer;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class LoginCommand extends Command {

    private int loginAttempts = 0;
    private final SocketServer server;
    private final Database database;

    public LoginCommand(SocketServer server) {
        super("login", CommandSide.SERVER, false, true);
        this.server = server;
        database = server.getDatabase();
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, List<String> args) {
        if(sender instanceof ServerCommandSender) {
            sender.sendMessage("The server cannot use this command");
            return false;
        }

        ClientConnection clientConnection = (ClientConnection) sender;
        if (loginAttempts == 3) {
            sendMessage(clientConnection, "You need to wait 10 seconds before trying to login again");
            return false;
        }
        if (args.size() != 2) {
            sendMessage(clientConnection, "Usage: /login <username> <password>");
            return false;
        }
        if (clientConnection.isLoggedIn()) {
            sendMessage(clientConnection, "You are already logged in");
            return false;
        }
        String username = args.get(0);
        String password = args.get(1);
        if (!database.containsUser(username)) {
            sendMessage(clientConnection, "Invalid username or password");
            ratelimit(clientConnection);
            return false;
        }
        if (!database.isPasswordCorrect(username, password)) {
            sendMessage(clientConnection, "Invalid username or password");
            ratelimit(clientConnection);
            return false;
        }
        clientConnection.login(username);
        return true;
    }

    private void ratelimit(ClientConnection clientConnection) {
        loginAttempts++;
        if (loginAttempts == 3) {
            server.getExecutorService().schedule(() -> {
                loginAttempts = 0;
                sendMessage(clientConnection, "You can try to login again");
            }, 10, TimeUnit.SECONDS);
        }
    }

    private void sendMessage(ClientConnection clientConnection, String msg) {
        clientConnection.sendPacket(new MessagePacket(OpCode.MESSAGE, new Message(null, msg).serialize(), MessagePacket.MessageChatType.SYSTEM));
    }
}
