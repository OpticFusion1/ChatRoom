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
package net.lenni0451.asmevents.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class ReflectUtils {

  public static Method getMethodByArgs(final Class<?> clazz, final Class<?>... args) {
    for (Method method : clazz.getDeclaredMethods()) {
      if (Arrays.equals(method.getParameterTypes(), args)) {
        return method;
      }
    }
    throw new RuntimeException("Unable to find method in " + clazz.getName() + " with arguments " + Arrays.toString(args));
  }

  public static Field getEnumField(final Enum<?> value) {
    for (Field field : value.getClass().getDeclaredFields()) {
      if (!Modifier.isStatic(field.getModifiers())) {
        continue;
      }
      if (!field.getType().equals(value.getClass())) {
        continue;
      }

      field.setAccessible(true);
      try {
        if (value.equals(field.get(null))) {
          return field;
        }
      } catch (Throwable ignored) {
      }
    }
    throw new RuntimeException("Unable to find enum field for " + value.getClass().getName() + " " + value);
  }

}
