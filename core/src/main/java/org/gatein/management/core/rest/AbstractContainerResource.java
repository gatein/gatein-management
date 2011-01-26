/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
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

package org.gatein.management.core.rest;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.RootContainer;
import org.gatein.common.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public abstract class AbstractContainerResource<C>
{
   private C component;

   @SuppressWarnings("unchecked")
   public AbstractContainerResource(String containerName)
   {
      Class<C> componentClass = getComponentClass();
      if (componentClass == null)
      {
         throw new WebApplicationException(new RuntimeException("Could not determine component class from type parameter."));
      }

      RootContainer container = (RootContainer) ExoContainerContext.getTopContainer();
      ExoContainer exoContainer = container.getPortalContainer(containerName);
      if (exoContainer == null)
      {
         throw new WebApplicationException(new RuntimeException("Could not retrieve portal container for " +
            containerName + " portal."));
      }

      component = (C) exoContainer.getComponentInstanceOfType(componentClass);

      if (component == null)
      {
         throw new WebApplicationException(new RuntimeException("Could not retrieve component " +
            componentClass + " from portal container " + containerName));
      }
   }

   protected void validateNonNullParameter(String parameter, String parameterName)
   {
      if (parameter == null) throw new WebApplicationException(
         new Exception(parameterName + " is a required parameter for this request."), Response.Status.BAD_REQUEST);
   }

   protected <T> Response doRequest(UriInfo uriInfo, ComponentRequestCallback<C, T> callback)
   {
      try
      {
         T result = callback.inRequest(component);
         if (callback instanceof ComponentRequestCallbackNoResult) // sort of a hack...
         {
            return Response.ok().build();
         }
         else
         {
            if (result == null)
            {
               if (getLogger().isDebugEnabled())
               {
                  getLogger().debug("No result returned for request " + uriInfo.getRequestUri());
               }
               return Response.status(Response.Status.NOT_FOUND).build();
            }
            else
            {
               return Response.ok().entity(result).build();
            }
         }
      }
      catch (WebApplicationException wae)
      {
         getLogger().error("Web application exception for request " + uriInfo.getRequestUri(), wae);
         return wae.getResponse();
      }
      catch (Throwable t)
      {
         getLogger().error("Unknown exception occurred for request " + uriInfo.getRequestUri(), t);
         return Response.serverError().build();
      }
   }

   public abstract Logger getLogger();

   @SuppressWarnings("unchecked")
   private Class<C> getComponentClass()
   {
      Class<?> clazz = getClass();

      while (clazz != AbstractContainerResource.class)
      {
         Type t = clazz.getGenericSuperclass();
         if (t instanceof ParameterizedType)
         {
            return (Class<C>) ((ParameterizedType) t).getActualTypeArguments()[0];
         }

         clazz = clazz.getSuperclass();
      }
      return null;
   }
}
