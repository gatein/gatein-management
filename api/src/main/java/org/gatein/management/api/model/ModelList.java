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
import java.util.List;

/**
 * A <code>ModelValue</code> representing a list/array
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public interface ModelList extends ModelValue, Iterable<ModelValue>
{
   ModelValue get(int index);

   <T extends ModelValue> T get(int index, Class<T> valueType);

   List<ModelValue> getValues();

   <T extends ModelValue> List<T> getValues(Class<T> valueType);

   ModelUndefined add();

   ModelList add(String value);

   ModelList add(int value);

   ModelList add(long value);

   ModelList add(double value);

   ModelList add(BigInteger value);

   ModelList add(BigDecimal value);

   ModelList add(boolean value);

   int size();
}
