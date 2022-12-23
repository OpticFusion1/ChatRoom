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
package optic_fusion1.server.network.listeners.event;

import net.lenni0451.asmevents.event.EventTarget;
import optic_fusion1.commands.CommandHandler;
import optic_fusion1.server.network.events.CommandEvent;

public record CommandEventListener(CommandHandler commandHandler) {

  @EventTarget()
  public void onEvent(CommandEvent event) {
    if (event.isCancelled()) {
      return;
    }
    commandHandler.executeCommand(event.getSender(), event.getCommand());
  }
}
