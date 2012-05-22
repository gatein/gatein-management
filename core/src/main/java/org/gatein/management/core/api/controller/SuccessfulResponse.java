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

package org.gatein.management.core.api.controller;

import org.gatein.management.api.ContentType;
import org.gatein.management.api.binding.BindingProvider;
import org.gatein.management.api.binding.Marshaller;
import org.gatein.management.api.controller.ManagedResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class SuccessfulResponse<T> implements ManagedResponse
{
   private final BindingProvider bindingProvider;
   private final T result;
   private final ContentType contentType;

   SuccessfulResponse(BindingProvider bindingProvider, T result, ContentType contentType)
   {
      if (result == null) throw new IllegalArgumentException("result is null.");

      this.bindingProvider = bindingProvider;
      this.result = result;
      this.contentType = contentType;
   }

   @Override
   public Outcome getOutcome()
   {
      return success;
   }

   @Override
   public T getResult()
   {
      return result;
   }

   @SuppressWarnings("unchecked")
   public void writeResult(OutputStream outputStream, boolean pretty) throws IOException
   {
      if (bindingProvider == null) throw new IOException("Cannot write result because no binding provider was specified.");

      Class<T> type = (Class<T>) result.getClass();
      if (Collection.class.isAssignableFrom(type))
      {
         Collection collection = (Collection) result;
         if (!collection.isEmpty())
         {
            type = (Class<T>) collection.iterator().next().getClass();
         }
      }

      Marshaller<T> marshaller = bindingProvider.getMarshaller(type, contentType);
      if (marshaller == null) throw new IOException("Could not find marshaller for type " + type + " and content type " + contentType);

      marshaller.marshal(result, outputStream, pretty);
   }

   private static final Outcome success = new Outcome()
   {
      @Override
      public boolean isSuccess()
      {
         return true;
      }

      @Override
      public String getFailureDescription()
      {
         return null;
      }
   };
}
