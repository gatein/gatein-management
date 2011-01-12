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
import javax.ws.rs.core.MediaType;
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
public abstract class AbstractExoContainerResource<S>
{
   private S service;

   @SuppressWarnings("unchecked")
   public AbstractExoContainerResource(String containerName)
   {
      Class<S> serviceType = getServiceClass();
      if (serviceType == null) throw new RuntimeException("Could not determine service class from type parameter.");

      RootContainer container = (RootContainer) ExoContainerContext.getTopContainer();
      ExoContainer exoContainer = container.getPortalContainer(containerName);
      if (exoContainer == null) throw new RuntimeException("Could not retrieve portal container for " + containerName + " portal.");

      service = (S) exoContainer.getComponentInstanceOfType(serviceType);

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

   protected void validateNonNullParameter(String parameter, String parameterName)
   {
      if (parameter == null) throw new WebApplicationException(new Exception(parameterName + " is a required parameter for this request."), Response.Status.BAD_REQUEST);
   }

   protected Response checkNullResult(Object result, UriInfo uriInfo)
   {
      if (result == null)
      {
         String message = "No data found for request " + uriInfo.getRequestUri();
         if (getLogger().isDebugEnabled())
         {
            getLogger().debug(message);
         }
         return Response.status(Response.Status.NOT_FOUND).entity(message).type(MediaType.TEXT_PLAIN).build();
      }
      else
      {
         return Response.ok().entity(result).build();
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

   protected S getService()
   {
      return service;
   }

   protected <T> Response handleRequest(String ownerType, UriInfo uriInfo, RestfulManagementServiceCallback<S,T> callback)
   {
      return _handleRequest(ownerType, null, uriInfo, callback);
   }

   protected <T> Response handleRequest(String ownerType, String ownerId, UriInfo uriInfo, RestfulManagementServiceCallback<S, T> callback)
   {
      ownerType = checkOwnerType(ownerType);
      checkOwnerId(ownerId);

      return _handleRequest(ownerType, ownerId, uriInfo, callback);
   }

   private <T> Response _handleRequest(String ownerType, String ownerId, UriInfo uriInfo, RestfulManagementServiceCallback<S, T> callback)
   {
      try
      {
         T result = callback.doService(service, ownerType, ownerId);
         if (callback instanceof AbstractMgmtServiceCallbackNoResult) // sort of a hack...
         {
            return Response.ok().build();
         }
         else
         {
            return checkNullResult(result, uriInfo);
         }
      }
      //TODO: Do we want to build an exception XML message body writer/reader or just send errors back as plain text ?
      catch (ManagementException e)
      {
         getLogger().error("Management exception occurred for request " + uriInfo.getRequestUri(), e);
         return Response.serverError().entity("Management exception occurred for this request. " + e.getLocalizedMessage())
            .type(MediaType.TEXT_PLAIN).build();
      }
      catch (Throwable t)
      {
         getLogger().error("Unknown exception occurred for request " + uriInfo.getRequestUri(), t);
         return Response.serverError().entity("Unknown error for this request.  See server log for more details.")
            .type(MediaType.TEXT_PLAIN).build();
      }
   }

   public abstract Logger getLogger();

   @SuppressWarnings("unchecked")
   private Class<S> getServiceClass()
   {
      Type t = getClass().getGenericSuperclass();
      if (t instanceof ParameterizedType)
      {
         return (Class<S>) ((ParameterizedType) t).getActualTypeArguments()[0];
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
