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

package org.gatein.management.rest.providers;

import org.gatein.management.api.controller.ManagedResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.stream.XMLStreamConstants;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
@Provider
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/zip"})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/zip"})
public class ManagedResponseWriter implements MessageBodyWriter<ManagedResponse>, XMLStreamConstants
{
   @Override
   public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType)
   {
      return (ManagedResponse.class.isAssignableFrom(aClass));
   }

   @Override
   public long getSize(ManagedResponse managedResponse, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType)
   {
      return -1;
   }

   @Override
   public void writeTo(ManagedResponse managedResponse, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> stringObjectMultivaluedMap, OutputStream outputStream) throws IOException, WebApplicationException
   {
      managedResponse.writeResult(outputStream);
   }
}
