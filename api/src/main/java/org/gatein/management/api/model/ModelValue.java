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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <code>ModelValue</code> represents a model value
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public interface ModelValue
{
   public enum ModelValueType
   {
      OBJECT, LIST, STRING, NUMBER, BOOLEAN, UNDEFINED
   }

   /**
    * Returns the value type of this <code>ModelValue</code>
    *
    * @return the model value type
    */
   ModelValueType getValueType();

   /**
    * Convenience method for determining if the value type is defined.
    *
    * @return true if the value type is not {@link ModelValueType#UNDEFINED}.
    */
   boolean isDefined();

   <T extends ModelValue> T asValue(Class<T> valueType);

   String toJsonString(boolean pretty);

   void toJsonStream(OutputStream outputStream, boolean pretty) throws IOException;

   ModelValue fromJsonStream(InputStream inputStream) throws IOException;

   <T extends ModelValue> T fromJsonStream(InputStream inputStream, Class<T> valueType) throws IOException;
}
