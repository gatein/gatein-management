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
import org.exoplatform.portal.pom.data.NavigationNodeData;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.ManagementException;
import org.gatein.management.pomdata.api.NavigationManagementService;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
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
         log.debug("Navigation RESTful resource retrieving navigation for ownerType " + ownerType + " and ownerId " + ownerId);
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
   @Path("/{nav-uri:.*}")
   @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
   public Response getNavigation(@Context UriInfo uriInfo,
                                 @QueryParam("ownerType") String ownerType,
                                 @QueryParam("ownerId") String ownerId,
                                 @PathParam("nav-uri") final String navigationUri)
   {
      if (log.isDebugEnabled())
      {
         log.debug("Navigation RESTful resource retrieving navigation for ownerType " +
            ownerType + " and ownerId " + ownerId + " and navigation uri " + navigationUri);
      }

      return handleRequest(ownerType, ownerId, uriInfo, new RestfulManagementServiceCallback<NavigationManagementService, NavigationData>()
      {
         @Override
         public NavigationData doService(NavigationManagementService service, String ownerType, String ownerId) throws ManagementException
         {
            NavigationNodeData node = service.getNavigationNode(ownerType, ownerId, navigationUri);
            return buildNavigationData(ownerType, ownerId, node);
         }
      });
   }

   @PUT
   @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
   public Response createNavigation(@Context UriInfo uriInfo,
                                    @QueryParam("ownerType") String ownerType,
                                    @QueryParam("ownerId") String ownerId,
                                    @QueryParam("priority") String priorityString)
   {
      validateNonNullParameter(priorityString, "priority");
      final Integer priority;
      try
      {
         priority = Integer.parseInt(priorityString);
      }
      catch (NumberFormatException nfe)
      {
         throw new WebApplicationException(new Exception("Invalid value " +
            priorityString + " for priority. This must be an integer."), Response.Status.BAD_REQUEST);
      }

      if (log.isDebugEnabled())
      {
         log.debug("Navigation RESTful resource creating navigation for ownerType " +
            ownerType + " and ownerId " + ownerId + " and priority " + priority);
      }

      return handleRequest(ownerType, ownerId, uriInfo, new RestfulManagementServiceCallback<NavigationManagementService, NavigationData>()
      {
         @Override
         public NavigationData doService(NavigationManagementService service, String ownerType, String ownerId) throws ManagementException
         {
            return service.createNavigation(ownerType, ownerId, priority);
         }
      });
   }

   @PUT
   @Path("/{parent-nav-uri:.*}")
   @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
   public Response createNavigation(@Context UriInfo uriInfo,
                                    @QueryParam("ownerType") String ownerType,
                                    @QueryParam("ownerId") String ownerId,
                                    @PathParam("parent-nav-uri") String parentNavigationUri,
                                    @QueryParam("name") final String name,
                                    @QueryParam("label") final String label)
   {
      validateNonNullParameter(name, "name");
      validateNonNullParameter(label, "label");

      final String uri = parentNavigationUri + "/" + name;
      if (log.isDebugEnabled())
      {
         log.debug("Navigation RESTful resource creating navigation for ownerType " +
            ownerType + " and ownerId " + ownerId + " and navigation uri " + uri + " and label " + label);
      }

      return handleRequest(ownerType, ownerId, uriInfo, new RestfulManagementServiceCallback<NavigationManagementService, NavigationData>()
      {
         @Override
         public NavigationData doService(NavigationManagementService service, String ownerType, String ownerId) throws ManagementException
         {
            NavigationNodeData node = service.createNavigationNode(ownerType, ownerId, uri, label);
            return buildNavigationData(ownerType, ownerId, node);
         }
      });
   }

   @POST
   @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
   public Response updateNavigation(@Context UriInfo uriInfo,
                                    @QueryParam("ownerType") String ownerType,
                                    @QueryParam("ownerId") String ownerId,
                                    final NavigationData data)
   {
      final boolean debug = log.isDebugEnabled();
      final List<NavigationNodeData> nodes = data.getNodes();
      if (nodes == null)
      {
         throw new WebApplicationException(
            new Exception("Cannot update navigation. No nodes were supplied in request."), Response.Status.BAD_REQUEST);
      }

      return handleRequest(ownerType, ownerId, uriInfo, new AbstractMgmtServiceCallbackNoResult<NavigationManagementService>()
      {
         @Override
         public void doServiceNoResult(NavigationManagementService service, String ownerType, String ownerId)
         {
            for (NavigationNodeData node : nodes)
            {
               if (debug)
               {
                  log.debug("Navigation RESTful resource updating navigation for ownerType " +
                     ownerType + " and ownerId " + ownerId + " and navigation uri " + node.getURI());
               }
               service.updateNavigationNode(ownerType, ownerId, node);
            }
         }
      });
   }

   @POST
   @Path("/{nav-uri:.*}")
   @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
   public Response updateNavigation(@Context UriInfo uriInfo,
                                    @QueryParam("ownerType") String ownerType,
                                    @QueryParam("ownerId") String ownerId,
                                    @PathParam("nav-uri") final String navigationUri,
                                    final NavigationData data)
   {
      if (log.isDebugEnabled())
      {
         log.debug("Navigation RESTful resource updating navigation for ownerType " +
            ownerType + " and ownerId " + ownerId + " and navigation uri " + navigationUri);
      }

      // Validate request data
      List<NavigationNodeData> nodes = data.getNodes();
      if (nodes == null)
      {
         throw new WebApplicationException(new Exception("Cannot update navigation for navigation uri " +
            navigationUri + ". No nodes were supplied in request."), Response.Status.BAD_REQUEST);
      }
      if (nodes.size() != 1)
      {
         throw new WebApplicationException(new Exception("Cannot update navigation for navigation uri " +
            navigationUri + ". Only one root node should be defined in this request."), Response.Status.BAD_REQUEST);
      }

      // Ensure URI of request data matches URI of URL Scheme
      final NavigationNodeData node = nodes.get(0);
      if (!node.getURI().equals(navigationUri))
      {
         throw new WebApplicationException(new Exception("Cannot delete navigation for navigation uri " +
            navigationUri + ". Node uri defined in data of request does not match nav URI of the request."), Response.Status.BAD_REQUEST);
      }

      return handleRequest(ownerType, ownerId, uriInfo, new AbstractMgmtServiceCallbackNoResult<NavigationManagementService>()
      {
         @Override
         public void doServiceNoResult(NavigationManagementService service, String ownerType, String ownerId)
         {
            service.updateNavigationNode(ownerType, ownerId, node);
         }
      });
   }

   //TODO: Do want to allow the ability to delete the default navigation for a site ?
//   @DELETE
//   @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
//   public Response deleteNavigation(@Context UriInfo uriInfo,
//                                    @QueryParam("ownerType") String ownerType,
//                                    @QueryParam("ownerId") String ownerId)
//   {
//      log.debug("Navigation RESTful resource deleting navigation for ownerType " + ownerType + " and ownerId " + ownerId);
//
//      return handleRequest(ownerType, ownerId, uriInfo, new AbstractMgmtServiceCallbackNoResult<NavigationManagementService>()
//      {
//         @Override
//         public void doServiceNoResult(NavigationManagementService service, String ownerType, String ownerId)
//         {
//            service.deleteNavigation(ownerType, ownerId);
//         }
//      });
//   }

   @DELETE
   @Path("/{nav-uri:.*}")
   @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
   public Response deleteNavigation(@Context UriInfo uriInfo,
                                    @QueryParam("ownerType") String ownerType,
                                    @QueryParam("ownerId") String ownerId,
                                    @PathParam("nav-uri") final String navigationUri)
   {
      if (log.isDebugEnabled())
      {
         log.debug("Navigation RESTful resource deleting navigation for ownerType " +
            ownerType + " and ownerId " + ownerId + " and navigation uri " + navigationUri);
      }

      return handleRequest(ownerType, ownerId, uriInfo, new AbstractMgmtServiceCallbackNoResult<NavigationManagementService>()
      {
         @Override
         public void doServiceNoResult(NavigationManagementService service, String ownerType, String ownerId)
         {
            service.deleteNavigationNode(ownerType, ownerId, navigationUri);
         }
      });
   }

   private NavigationData buildNavigationData(String ownerType, String ownerId, NavigationNodeData node)
   {
      if (node == null) return null;

      List<NavigationNodeData> nodes = new ArrayList<NavigationNodeData>();
      nodes.add(node);
      return new NavigationData(ownerType, ownerId, null, nodes);
   }
}
