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
package optic_fusion1.commands.command;

import java.util.List;
import java.util.Objects;

public abstract class Command {

  private final String name;
  private final CommandSide side;
  private final CommandPermissionLevel commandPermissionLevel;
  private final boolean serverCanExecute, usersCanExecute;
  private int minimumArgLength = -1;
  private int maximumArgLength = -1;

  /**
   * Base command with default permissions to everyone
   *
   * @param name name of the command
   * @param side side the command runs on
   */
  public Command(String name, CommandSide side) {
    this.name = name;
    this.side = side;
    this.commandPermissionLevel = CommandPermissionLevel.EVERYONE;
    this.serverCanExecute = false;
    this.usersCanExecute = true;
  }

  /**
   * Base command
   *
   * @param name name of the command
   * @param side side the command runs on
   * @param commandPermissionLevel permission level of who should be able to run this command
   */
  public Command(String name, CommandSide side, CommandPermissionLevel commandPermissionLevel) {
    this.name = name;
    this.side = side;
    this.commandPermissionLevel = commandPermissionLevel;
    this.serverCanExecute = false;
    this.usersCanExecute = true;
  }

  /**
   * Base command with default permissions to everyone
   *
   * @param name name of the command
   * @param side side the command runs on
   * @param serverCanExecute whether the server can execute the command or not
   */
  public Command(String name, CommandSide side, boolean serverCanExecute, boolean usersCanExecute) {
    this.name = name;
    this.side = side;
    this.commandPermissionLevel = CommandPermissionLevel.EVERYONE;
    this.serverCanExecute = serverCanExecute;
    this.usersCanExecute = usersCanExecute;
  }

  /**
   * Base command
   *
   * @param name name of the command
   * @param side side the command runs on
   * @param commandPermissionLevel permission level of who should be able to run this command
   * @param serverCanExecute whether the server can execute the command or not
   */
  public Command(String name, CommandSide side, CommandPermissionLevel commandPermissionLevel, boolean serverCanExecute, boolean usersCanExecute) {
    this.name = name;
    this.side = side;
    this.commandPermissionLevel = commandPermissionLevel;
    this.serverCanExecute = serverCanExecute;
    this.usersCanExecute = usersCanExecute;
  }

  public abstract boolean execute(CommandSender sender, String commandLabel, List<String> args);

  public int getMinimumArgLength() {
    return minimumArgLength;
  }

  public void setArgLength(int minimumArgLength, int maximumArgLength) {
    this.minimumArgLength = minimumArgLength;
    this.maximumArgLength = maximumArgLength;
  }

  public void setMinimumArgLength(int minimumArgLength) {
    this.minimumArgLength = minimumArgLength;
  }

  public int getMaximumArgLength() {
    return maximumArgLength;
  }

  public void setMaximumArgLength(int maximumArgLength) {
    this.maximumArgLength = maximumArgLength;
  }

  public String getName() {
    return name;
  }

  public CommandSide getSide() {
    return side;
  }

  public CommandPermissionLevel getCommandPermissionLevel() {
    return commandPermissionLevel;
  }

  public boolean checkArgLength(final List<String> args) {
    return (this.minimumArgLength == -1 || this.minimumArgLength <= args.size()) && (args.size() <= this.maximumArgLength || this.maximumArgLength == -1);
  }

  public boolean isServerCanExecute() {
    return serverCanExecute;
  }

  public boolean canExecuteServer(CommandSender sender) {
    return Objects.isNull(sender) && !this.serverCanExecute;
  }

}
