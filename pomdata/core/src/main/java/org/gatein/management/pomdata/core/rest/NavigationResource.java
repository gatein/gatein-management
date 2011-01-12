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

import org.exoplatform.portal.pom.data.NavigationData;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.ManagementException;
import org.gatein.management.pomdata.api.NavigationManagementService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
//TODO: Add debugging
public class NavigationResource extends AbstractExoContainerResource<NavigationManagementService>
{
   private static final Logger log = LoggerFactory.getLogger(NavigationResource.class);

   public NavigationResource(String containerName)
   {
      super(containerName);
   }

   @Override
   public Logger getLogger()
   {
      return log;
   }

   @GET
   @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
   public Response getNavigation(@Context UriInfo uriInfo,
                                 @QueryParam("ownerType") String ownerType,
                                 @QueryParam("ownerId") String ownerId)
   {

      if (log.isDebugEnabled())
      {
         log.debug("RESTful resource retrieving navigation for ownerType " + ownerType + " and ownerId " + ownerId);
      }

      return handleRequest(ownerType, ownerId, uriInfo, new RestfulManagementServiceCallback<NavigationManagementService, NavigationData>()
      {
         @Override
         public NavigationData doService(NavigationManagementService service, String ownerType, String ownerId) throws ManagementException
         {
            return service.getNavigation(ownerType, ownerId);
         }
      });
   }

   @GET
   @Path("/{nav-path:.*}")
   @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
   public Response getNavigation(@Context UriInfo uriInfo,
                                 @QueryParam("ownerType") String ownerType,
                                 @QueryParam("ownerId") String ownerId,
                                 @PathParam("nav-path") String navigationPath)
   {
      if (log.isDebugEnabled())
      {
         log.debug("RESTful resource retrieving navigation for ownerType " + ownerType + " and ownerId " + ownerId + " and navigation path " + navigationPath);
      }

      // Ensure we have a leading '/'
      final String path = fixPath(navigationPath);

      // Call service and return the response.
      return handleRequest(ownerType, ownerId, uriInfo, new RestfulManagementServiceCallback<NavigationManagementService, NavigationData>()
      {
         @Override
         public NavigationData doService(NavigationManagementService service, String ownerType, String ownerId) throws ManagementException
         {
            return service.getNavigation(ownerType, ownerId, path);
         }
      });
   }

   private String fixPath(String originalPath)
   {
      if (originalPath.charAt(0) == '/') return originalPath;

      return '/' + originalPath;
   }
}
