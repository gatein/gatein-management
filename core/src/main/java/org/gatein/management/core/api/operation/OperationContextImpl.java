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

package org.gatein.management.core.api.operation;

import org.gatein.management.api.ContentType;
import org.gatein.management.api.ManagedResource;
import org.gatein.management.api.ManagedUser;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.RuntimeContext;
import org.gatein.management.api.binding.BindingProvider;
import org.gatein.management.api.controller.AuthenticatedManagedRequest;
import org.gatein.management.api.controller.ManagedRequest;
import org.gatein.management.api.model.Model;
import org.gatein.management.api.model.ModelProvider;
import org.gatein.management.api.model.ModelValue;
import org.gatein.management.api.operation.OperationAttachment;
import org.gatein.management.api.operation.OperationAttributes;
import org.gatein.management.api.operation.OperationContext;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class OperationContextImpl implements OperationContext
{
   private final ManagedRequest request;
   private final ManagedResource resource;
   private final RuntimeContext runtimeContext;
   private final BindingProvider bindingProvider;
   private final Deque<OperationAttachment> attachments;
   private final OperationAttributes attributes;
   private final Locale locale;
   private final ModelProvider modelProvider;


   public OperationContextImpl(final ManagedRequest request, final ManagedResource resource, final RuntimeContext runtimeContext, final BindingProvider bindingProvider, final ModelProvider modelProvider)
   {
      Deque<OperationAttachment> list = new ArrayDeque<OperationAttachment>();

      list.push(new OperationAttachment()
      {
         @Override
         public InputStream getStream()
         {
            return request.getDataStream();
         }
      });

      this.request = request;
      this.resource = resource;
      this.runtimeContext = runtimeContext;
      this.bindingProvider = bindingProvider;
      this.modelProvider = modelProvider;
      this.attachments = list;
      this.attributes = new OperationAttributesImpl(request.getAttributes());
      this.locale = request.getLocale();
   }

   @Override
   public ManagedUser getUser()
   {
      if (request instanceof AuthenticatedManagedRequest)
      {
         return ((AuthenticatedManagedRequest) request).getUser();
      }

      return null;
   }

   @Override
   public boolean isUserInRole(String role)
   {
      return runtimeContext.isUserInRole(role);
   }

   @Override
   public ManagedResource getManagedResource()
   {
      return resource;
   }

   @Override
   public PathAddress getAddress()
   {
      return request.getAddress();
   }

   @Override
   public String getOperationName()
   {
      return request.getOperationName();
   }

   @Override
   public RuntimeContext getRuntimeContext()
   {
      return runtimeContext;
   }

   @Override
   public Model newModel()
   {
      return modelProvider.newModel();
   }

   @Override
   public <T extends ModelValue> T newModel(Class<T> modelType)
   {
      return modelProvider.newModel(modelType);
   }

   @Override
   public OperationAttributes getAttributes()
   {
      return attributes;
   }

   @Override
   public Locale getLocale()
   {
      return locale;
   }

   @Override
   public OperationAttachment getAttachment(boolean remove)
   {
      if (remove)
      {
         return attachments.pop();
      }
      else
      {
         return attachments.peek();
      }
   }

   @Override
   public BindingProvider getBindingProvider()
   {
      return bindingProvider;
   }

   @Override
   public ContentType getContentType()
   {
      return request.getContentType();
   }
}
