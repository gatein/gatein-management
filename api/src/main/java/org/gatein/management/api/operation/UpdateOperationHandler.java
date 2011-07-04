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

package org.gatein.management.api.operation;

import org.gatein.management.api.binding.BindingException;
import org.gatein.management.api.binding.Marshaller;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.exceptions.ResourceNotFoundException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public abstract class UpdateOperationHandler<T> implements OperationHandler
{
   @Override
   public void execute(OperationContext operationContext, ResultHandler resultHandler) throws ResourceNotFoundException, OperationException
   {
      OperationAttachment attachment = operationContext.getAttachment(0);
      Marshaller<T> marshaller = operationContext.getBindingProvider().getMarshaller(getParameterizedType(), attachment.getContentType());

      if (marshaller == null) throw new OperationException(operationContext.getOperationName(), "No marshaller found for address " + operationContext.getAddress());

      try
      {
         execute(operationContext, marshaller.unmarshal(attachment.getStream()));
      }
      catch (BindingException e)
      {
         throw new OperationException(operationContext.getOperationName(), "Exception unmarshalling data for address " + operationContext.getAddress(), e);
      }
   }

   protected abstract void execute(OperationContext operationContext, T data) throws ResourceNotFoundException, OperationException;

   @SuppressWarnings("unchecked")
   private Class<T> getParameterizedType()
   {
      Class<?> clazz = getClass();

      while (clazz != UpdateOperationHandler.class)
      {
         Type t = clazz.getGenericSuperclass();
         if (t instanceof ParameterizedType)
         {
            return (Class<T>) ((ParameterizedType) t).getActualTypeArguments()[0];
         }

         clazz = clazz.getSuperclass();
      }

      return null;
   }
}
