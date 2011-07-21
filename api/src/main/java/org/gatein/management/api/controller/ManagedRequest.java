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

package org.gatein.management.api.controller;

import org.gatein.management.api.ContentType;
import org.gatein.management.api.PathAddress;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public interface ManagedRequest
{
   String getOperationName();

   PathAddress getAddress();

   Map<String, List<String>> getAttributes();

   InputStream getDataStream();

   ContentType getContentType();

   //TODO: Add request builder instead of continually adding methods to factory

   public static class Factory
   {
      public static ManagedRequest create(final String operationName, final PathAddress address, final ContentType contentType)
      {
         return create(operationName, address, Collections.<String, List<String>>emptyMap(), contentType);
      }

      public static ManagedRequest create(final String operationName, final PathAddress address, final Map<String, List<String>> attributes, final ContentType contentType)
      {
         return create(operationName, address, attributes, null, contentType);
      }

      public static ManagedRequest create(String operationName, PathAddress address, InputStream data, ContentType contentType)
      {
         return create(operationName, address, Collections.<String, List<String>>emptyMap(), data, contentType);
      }

      public static ManagedRequest create(final String operationName, final PathAddress address, final Map<String, List<String>> attributes, final InputStream dataStream, final ContentType contentType)
      {
         return new ManagedRequest()
         {
            @Override
            public String getOperationName()
            {
               return operationName;
            }

            @Override
            public PathAddress getAddress()
            {
               return address;
            }

            @Override
            public Map<String, List<String>> getAttributes()
            {
               return attributes;
            }

            @Override
            public InputStream getDataStream()
            {
               return dataStream;
            }

            @Override
            public ContentType getContentType()
            {
               return contentType;
            }
         };
      }
   }
}
