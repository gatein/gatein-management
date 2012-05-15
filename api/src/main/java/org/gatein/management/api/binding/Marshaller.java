/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.gatein.management.api.binding;

import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * A marshaller responsible for marshalling and unmarshalling of objects.  This marshaller should be returned from a
 * {@link BindingProvider} which ties it to a specific {@link org.gatein.management.api.ContentType} to un/marshal from/to.
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public interface Marshaller<T>
{
   /**
    * Marshal an object to a stream.
    *
    * @param object the object to marshal.
    * @param outputStream the stream to write the data to.
    * @param pretty true if the marshaled content should be formatted pretty, including new lines and spaces.
    * @throws BindingException if an exception occurs during marshalling.
    */
   public void marshal(T object, OutputStream outputStream, boolean pretty) throws BindingException;

   /**
    * Unmarshal an object from a stream.
    *
    * @param inputStream stream containing data to read.
    * @return object based on content read.
    * @throws BindingException if an exception occurs during unmarshalling.
    */
   public T unmarshal(InputStream inputStream) throws BindingException;
}