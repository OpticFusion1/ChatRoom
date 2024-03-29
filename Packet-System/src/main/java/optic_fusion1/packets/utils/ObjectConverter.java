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
package optic_fusion1.packets.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectConverter {

  public static byte[] objectToByteArray(final Object input) throws IOException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
    objectOut.writeObject(input);
    return byteOut.toByteArray();
  }

  public static <T> T byteArrayToObject(final byte[] input) throws IOException {
    ByteArrayInputStream byteIn = new ByteArrayInputStream(input);
    ObjectInputStream objectIn = new ObjectInputStream(byteIn);
    try {
      return (T) objectIn.readObject();
    } catch (Exception ignored) {
    }
    return null;
  }

}
