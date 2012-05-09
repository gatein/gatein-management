/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.gatein.management.api.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;

/**
 * A <code>ModelValue</code> representing an object/structure
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
//TODO: Javadoc
public interface ModelObject extends ModelValue
{
   /**
    * Method for retrieving or creating a <code>ModelValue</code>. For creating use the set methods, for retrieving
    * use the {@link ModelObject#get(String, Class)} method.
    *
    * @param name the name of the value
    * @return a <code>ModelUndefined</code> value representing the initial undefined state.
    */
   ModelUndefined get(String name);

   /**
    * Recursively retrieves model values for the given names.
    *
    * @param names the names of the values
    * @return a <code>ModelUndefined</code> value representing the initial undefined state.
    */
   ModelUndefined get(String... names);

   <T extends ModelValue> T get(String name, Class<T> valueType);

   ModelObject set(String name, String value);

   ModelObject set(String name, int value);

   ModelObject set(String name, long value);

   ModelObject set(String name, double value);

   ModelObject set(String name, BigInteger value);

   ModelObject set(String name, BigDecimal value);

   ModelObject set(String name, boolean value);

   boolean has(String name);

   boolean hasDefined(String name);

   ModelValue remove(String name);

   <T extends ModelValue> T remove(String name, Class<T> valueType);

   Set<String> getNames();
}
