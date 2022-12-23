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
package optic_fusion1.commands.shellparser;

import java.util.ArrayList;
import java.util.List;
import optic_fusion1.commands.shellparser.states.StartState;

public class ShellParser {

  public static List<String> parseString(String string) throws ParseException {
    return (new StartState()).parse(string, "", new ArrayList<>(), null);
  }

  public static List<String> safeParseString(String string) {
    try {
      return ShellParser.parseString(string);
    } catch (ParseException e) {
      return null;
    }
  }
}
