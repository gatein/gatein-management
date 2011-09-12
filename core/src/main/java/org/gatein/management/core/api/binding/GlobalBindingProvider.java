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

package org.gatein.management.core.api.binding;

import org.gatein.management.api.ContentType;
import org.gatein.management.api.binding.BindingException;
import org.gatein.management.api.binding.BindingProvider;
import org.gatein.management.api.binding.Marshaller;
import org.gatein.management.api.operation.model.ExportResourceModel;
import org.gatein.management.api.operation.model.ReadResourceModel;
import org.gatein.management.core.api.binding.json.ReadResourceModelMarshaller;
import org.gatein.management.core.api.binding.zip.ExportResourceModelMarshaller;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class GlobalBindingProvider implements BindingProvider
{
   private static final Marshaller<ExportResourceModel> EXPORT_RESOURCE_MARSHALLER = new ExportResourceModelMarshaller();
   private static final Marshaller<ReadResourceModel> JSON_READ_RESOURCE_MODEL_MARSHALLER = new ReadResourceModelMarshaller();

   @Override
   public <T> Marshaller<T> getMarshaller(Class<T> type, ContentType contentType) throws BindingException
   {
      if (contentType == ContentType.ZIP && type == ExportResourceModel.class)
      {
         return (Marshaller<T>) EXPORT_RESOURCE_MARSHALLER;
      }
      else if (contentType == ContentType.JSON && type == ReadResourceModel.class)
      {
         return (Marshaller<T>) JSON_READ_RESOURCE_MODEL_MARSHALLER;
      }

      return null;
   }
}
