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

package org.gatein.management.pomdata.core.rest;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.RootContainer;
import org.gatein.common.logging.Logger;
import org.gatein.management.ManagementException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public abstract class AbstractExoContainerResource<T>
{
   private T service;

   @SuppressWarnings("unchecked")
   public AbstractExoContainerResource(String containerName)
   {
      Class<T> serviceType = getServiceClass();
      if (serviceType == null) throw new RuntimeException("Could not determine service class from type parameter.");

      RootContainer container = (RootContainer) ExoContainerContext.getTopContainer();
      ExoContainer exoContainer = container.getPortalContainer(containerName);
      if (exoContainer == null) throw new RuntimeException("Could not retrieve portal container for " + containerName + " portal.");

      service = (T) exoContainer.getComponentInstanceOfType(serviceType);

      if (service == null) throw new RuntimeException("Could not retrieve resource service " + serviceType + " from portal container " + containerName);
   }

   protected String checkOwnerType(String ownerType)
   {
      if (ownerType == null) ownerType = "portal"; // Default to portal, not required as query parameter in URL.

      if (isValidOwnerType(ownerType))
      {
         return ownerType;
      }
      else
      {
         throw new WebApplicationException(new Exception("'" + ownerType + "' is not a valid ownerType."), Response.Status.BAD_REQUEST);
      }
   }

   protected void checkOwnerId(String ownerId)
   {
      if (ownerId == null)
      {
         throw new WebApplicationException(new Exception("ownerId is required for this request."), Response.Status.BAD_REQUEST);
      }
   }

   protected void checkNullResult(Object result, UriInfo uriInfo)
   {
      if (result == null)
      {
         String message = "No data found for request " + uriInfo.getRequestUri();
         getLogger().error(message);
         throw new WebApplicationException(new Exception(message), Response.Status.NOT_FOUND);
      }
   }

   protected void handleManagementException(ManagementException e, UriInfo uriInfo)
   {
      getLogger().error("Management exception occurred for request " + uriInfo.getRequestUri(), e);
      throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
   }

   protected void handleUnknownError(Throwable t, UriInfo uriInfo)
   {
      getLogger().error("Unknown error occurred for request " + uriInfo.getRequestUri(), t);
      throw new WebApplicationException(
         new Exception("Unknown error occurred for this request. See server log for more detail."),
         Response.Status.INTERNAL_SERVER_ERROR);
   }

   protected T getService()
   {
      return service;
   }

   public abstract Logger getLogger();

   @SuppressWarnings("unchecked")
   private Class<T> getServiceClass()
   {
      Type t = getClass().getGenericSuperclass();
      if (t instanceof ParameterizedType)
      {
         return (Class<T>) ((ParameterizedType) t).getActualTypeArguments()[0];
      }

      return null;
   }

   private boolean isValidOwnerType(String ownerType)
   {
      return validOwnerTypes.contains(ownerType);
   }

   private static final Set<String> validOwnerTypes;
   static {
      Set<String> set = new HashSet<String>();
      set.add("portal");
      set.add("group");
      set.add("user");
      validOwnerTypes = set;
   }
}
