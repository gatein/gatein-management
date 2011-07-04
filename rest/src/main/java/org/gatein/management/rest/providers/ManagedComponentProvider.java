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

package org.gatein.management.rest.providers;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.api.binding.ContentType;
import org.gatein.management.api.binding.Marshaller;
import org.gatein.management.rest.ContentTypeUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import javax.xml.stream.XMLStreamConstants;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
@Provider
@Consumes
@Produces
public class ManagedComponentProvider<T> implements MessageBodyReader<T>, MessageBodyWriter<T>, XMLStreamConstants
{
   private static final Logger log = LoggerFactory.getLogger(ManagedComponentProvider.class);

   @Context
   private Providers providers;

   @Context
   private UriInfo uriInfo;

   @Override
   public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return getMarshaller(type, mediaType) != null;
   }

   @Override
   public T readFrom(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException
   {
      return getMarshaller(type, mediaType).unmarshal(entityStream);
   }

   @Override
   public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return getMarshaller(type, mediaType) != null;
   }

   @Override
   public long getSize(T entity, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return -1;
   }

   @Override
   public void writeTo(T entity, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException
   {
      getMarshaller(type, mediaType).marshal(entity, entityStream);
   }

   private Marshaller<T> getMarshaller(Type genericType, MediaType mediaType)
   {
      Class<T> type = getType(genericType);

      ContentType ct = ContentTypeUtils.getContentType(uriInfo);
      if (ct == null)
      {
         ct = ContentTypeUtils.getContentType(mediaType);
      }

      if (ct == null)
      {
         log.warn("No content type found for media type " + mediaType);
         return null;
      }

      ContextResolver<BindingProviderResolver> resolver = providers.getContextResolver(BindingProviderResolver.class, mediaType);
      if (resolver == null) throw new RuntimeException("Could not find marshaller resolver for media type " + mediaType);

      return resolver.getContext(type).getMarshaller(type, ct, uriInfo);
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
}
