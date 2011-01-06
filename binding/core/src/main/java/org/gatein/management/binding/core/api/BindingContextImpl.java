/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.gatein.management.binding.core.api;

import org.gatein.management.binding.api.BindingContext;
import org.gatein.management.binding.api.BindingException;
import org.gatein.management.binding.api.Bindings;
import org.gatein.management.binding.api.Marshaller;

import java.util.Set;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class BindingContextImpl<T> implements BindingContext<T>
{
   private final Class<T> bindingType;
   private final Set<Class<? extends Marshaller>> marshallers;

   public BindingContextImpl(Class<T> bindingType, Set<Class<? extends Marshaller>> marshallers)
   {
      if (bindingType == null) throw new IllegalArgumentException("bindingType cannot be null.");
      if (marshallers == null) throw new IllegalArgumentException("marshallers cannot be null.");

      this.bindingType = bindingType;
      this.marshallers = marshallers;
   }

   @Override
   public Marshaller<T> createMarshaller() throws BindingException
   {
      Marshaller<T> marshaller;
      for (Class<? extends Marshaller> marshallerClass : marshallers)
      {
         marshaller = _createMarshaller(marshallerClass);
         if (marshaller != null) return marshaller;
      }

      throw new BindingException("Could not find a suitable marshaller responsible for the marshalling of type " + bindingType);
   }

   @Override
   public Marshaller<T> createMarshaller(Class<? extends Marshaller> marshallerClass) throws BindingException
   {
      if (!marshallers.contains(marshallerClass)) throw new BindingException(marshallerClass + " is not a registered marshaller.");

      Marshaller<T> marshaller = _createMarshaller(marshallerClass);
      if (marshaller == null) throw new BindingException(marshallerClass + " does not support the marshalling of type " + bindingType);

      return marshaller;
   }

   private Marshaller<T> _createMarshaller(Class<? extends Marshaller> marshallerClass) throws BindingException
   {
      Bindings supportedBindings = marshallerClass.getAnnotation(Bindings.class);
      if (supportedBindings == null) throw new BindingException("Marshaller class " + marshallerClass + " does not have a Bindings annotation.");

      if (supported(bindingType, supportedBindings))
      {
         return instantiateMarshaller(marshallerClass);
      }
      else
      {
         return null;
      }
   }

   private boolean supported(Class<?> type, Bindings bindings)
   {
      Class<?>[] classes = bindings.classes();

      for (Class<?> c : classes)
      {
         if (c == type)
         {
            return true;
         }
      }

      return false;
   }

   @SuppressWarnings("unchecked")
   private <T> Marshaller<T> instantiateMarshaller(Class<? extends Marshaller> marshaller)
   {
      try
      {
         return marshaller.newInstance();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not create marshaller.", e);
      }
   }
}
