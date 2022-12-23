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
package optic_fusion1.commands.shellparser.states;

import java.util.List;
import optic_fusion1.commands.shellparser.ParseException;

public class QuoteState extends State {

  char quote;

  public QuoteState(char quote) {
    this.quote = quote;
  }

  @Override
  public List<String> parse(String parsing, String accumulator, List<String> parsed, State referrer) throws ParseException {
    if (parsing.length() == 0) {
      throw new ParseException("Mismatched quote character: " + this.quote);
    }

    char c = (char) parsing.getBytes()[0];

    if (c == '\\') {
      return (new EscapeState()).parse(parsing.substring(1), accumulator, parsed, this);
    } else if (c == this.quote) {
      return (new StartState()).parse(parsing.substring(1), accumulator, parsed, this);
    } else {
      return (new QuoteState(this.quote)).parse(parsing.substring(1), accumulator + c, parsed, this);
    }
  }

}
