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
package optic_fusion1.server.network.listeners;

import optic_fusion1.packets.IPacket;
import optic_fusion1.server.network.ClientConnection;

public interface ServerEventListener {

  default void onSocketPreConnect(final ClientConnection client) {
  }

  default void onSocketConnectionEstablished(final ClientConnection client) {
  }

  default void onSocketDisconnect(final ClientConnection client) {
  }

  default void onRawPacketReceive(final ClientConnection client, final byte[] packet) {
  }

  default void onPacketReceive(final ClientConnection client, final IPacket packet) {
  }

  default void onServerClose() {
  }

}
