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
package net.lenni0451.asmevents.event;

import net.lenni0451.asmevents.event.enums.EnumEventPriority;
import net.lenni0451.asmevents.event.enums.EnumEventType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventTarget {

  /**
   * The priority of the event in the pipeline
   */
  EnumEventPriority priority() default EnumEventPriority.NORMAL;

  /**
   * The type of event which should handled by the method
   */
  EnumEventType type() default EnumEventType.ALL;

  /**
   * The method also targets cancelled methods
   */
  boolean skipCancelled() default false;

  /**
   * Events to listen to which do not need a parameter passed
   */
  Class<? extends IEvent>[] noParamEvents() default {};

}
