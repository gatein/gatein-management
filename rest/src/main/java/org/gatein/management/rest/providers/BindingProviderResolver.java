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

import org.gatein.management.api.ContentType;
import org.gatein.management.api.ManagementService;
import org.gatein.management.api.binding.BindingProvider;
import org.gatein.management.api.binding.Marshaller;

import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
@Provider
public class BindingProviderResolver implements ContextResolver<BindingProviderResolver>
{
   private ManagementService service;

   public BindingProviderResolver(ManagementService service)
   {
      this.service = service;
   }

   @Override
   public BindingProviderResolver getContext(Class<?> type)
   {
      return this;
   }

   public <T> Marshaller<T> getMarshaller(Class<T> type, ContentType contentType, UriInfo uriInfo)
   {
      String componentName = null;
      if (uriInfo.getPathSegments().size() >=1)
      {
         componentName = uriInfo.getPathSegments().get(1).getPath();
      }

      BindingProvider bp = service.getBindingProvider(componentName);
      if (bp == null) return null;

      return bp.getMarshaller(type, contentType);
   }
}
