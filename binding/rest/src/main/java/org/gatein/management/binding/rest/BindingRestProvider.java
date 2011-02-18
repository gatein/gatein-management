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

package org.gatein.management.binding.rest;

import org.gatein.management.binding.api.BindingException;
import org.gatein.management.binding.api.BindingProvider;
import org.gatein.management.binding.api.Marshaller;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
@Provider
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.WILDCARD)
public class BindingRestProvider<T> implements MessageBodyReader<T>, MessageBodyWriter<T>
{
   private BindingProvider bindingProvider;

   public BindingRestProvider(BindingProvider bindingProvider)
   {
      this.bindingProvider = bindingProvider;
   }

   @Override
   public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return isWriteable(type, genericType, annotations, mediaType);
   }

   @Override
   public T readFrom(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException
   {
      Class<T> marshalType = getType(genericType);
      Marshaller<T> marshaller = createMarshaller(marshalType);
      if (marshaller == null)
      {
         throw new IOException("Cannot create marshaller for type " + marshalType);
      }

      if (Collection.class.isAssignableFrom(type))
      {
         @SuppressWarnings("unchecked")
         T objects = (T) marshaller.unmarshalObjects(entityStream);
         return objects;
      }
      else
      {
         return marshaller.unmarshal(entityStream);
      }
   }

   @Override
   public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      try
      {
         Marshaller marshaller = createMarshaller(getType(genericType));
         return (marshaller != null);
      }
      catch (BindingException be)
      {
         return false;
      }
   }

   @Override
   public long getSize(T object, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return -1;
   }

   @Override
   @SuppressWarnings("unchecked")
   public void writeTo(T object, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException
   {
      Class<T> marshalType = getType(genericType);
      Marshaller<T> marshaller = createMarshaller(marshalType);
      if (marshaller == null)
      {
         throw new IOException("Cannot create marshaller for type " + marshalType);
      }

      if (Collection.class.isAssignableFrom(type))
      {
         marshaller.marshalObjects((Collection<T>) object, entityStream);
      }
      else
      {
         marshaller.marshal(object, entityStream);
      }
   }

   @SuppressWarnings("unchecked")
   private Class<T> getType(Type genericType)
   {
      if (genericType instanceof ParameterizedType)
      {
         ParameterizedType pt = (ParameterizedType) genericType;
         Type[] arguments = pt.getActualTypeArguments();
         if (arguments != null && arguments.length == 1)
         {
            Type arg = arguments[0];
            if (arg instanceof TypeVariable)
            {
               return (Class<T>) ((TypeVariable) arg).getBounds()[0];
            }
            else
            {
               return (Class<T>) arg;
            }
         }
         else
         {
            return (Class<T>) pt.getRawType();
         }
      }
      else
      {
         return (Class<T>) genericType;
      }
   }

   private Marshaller<T> createMarshaller(Class<T> type)
   {
      return bindingProvider.createContext(type).createMarshaller();
   }
}
