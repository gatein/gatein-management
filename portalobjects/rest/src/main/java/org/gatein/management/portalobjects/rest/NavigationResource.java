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

package org.gatein.management.portalobjects.rest;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.pom.data.ModelDataStorage;
import org.exoplatform.portal.pom.data.NavigationData;
import org.exoplatform.portal.pom.data.NavigationKey;
import org.exoplatform.portal.pom.data.NavigationNodeData;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.core.rest.ComponentRequestCallback;
import org.gatein.management.core.rest.ComponentRequestCallbackNoResult;
import org.gatein.management.portalobjects.common.utils.PortalObjectsUtils;

import javax.annotation.security.RolesAllowed;
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
import java.util.Collections;
import java.util.List;

import static javax.ws.rs.core.Response.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class NavigationResource extends BasePortalObjectsResource
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
   @RolesAllowed("administrators")
   @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
   public Response getNavigation(@Context UriInfo uriInfo,
                                 @QueryParam("ownerType") String type,
                                 @QueryParam("ownerId") String id)
   {
      final String ownerType = checkOwnerType(type);
      final String ownerId = checkOwnerId(ownerType, id);

      if (log.isDebugEnabled())
      {
         log.debug(createMessage("Retrieving navigation", ownerType, ownerId));
      }

      return doRequest(uriInfo, new ComponentRequestCallback<ModelDataStorage, NavigationData>()
      {
         @Override
         public NavigationData inRequest(ModelDataStorage dataStorage) throws Exception
         {
            return getNavigation(ownerType, ownerId, dataStorage);
         }
      });
   }

   @GET
   @Path("/{nav-uri:.*}")
   @RolesAllowed("administrators")
   @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
   public Response getNavigation(@Context UriInfo uriInfo,
                                 @QueryParam("ownerType") String type,
                                 @QueryParam("ownerId") String id,
                                 @PathParam("nav-uri") final String navigationUri)
   {
      final String ownerType = checkOwnerType(type);
      final String ownerId = checkOwnerId(ownerType, id);

      if (log.isDebugEnabled())
      {
         log.debug(createMessage("Retrieving navigation", ownerType, ownerId, navigationUri));
      }

      return doRequest(uriInfo, new ComponentRequestCallback<ModelDataStorage, NavigationData>()
      {
         @Override
         public NavigationData inRequest(ModelDataStorage dataStorage) throws Exception
         {
            return getNavigation(ownerType, ownerId, navigationUri, dataStorage);
         }
      });
   }

   // Since you can either create a default navigation, or a navigation node at the default level, this
   // method will handle both.
   @PUT
   @RolesAllowed("administrators")
   @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
   public Response createNavigation(@Context UriInfo uriInfo,
                                    @QueryParam("ownerType") String type,
                                    @QueryParam("ownerId") String id,
                                    @QueryParam("priority") final Integer priority,
                                    @QueryParam("name") final String name,
                                    @QueryParam("label") final String label)
   {
      // If priority wasn't specified as a query parameter, then we can assume it's a navigation node create.
      if (priority == null)
      {
         return createNavigation(uriInfo, type, id, (String) null, name, label);
      }

      final String ownerType = checkOwnerType(type);
      final String ownerId = checkOwnerId(ownerType, id);

      // This check ensures the client didn't accidentally include a priority parameter when it wanted a node create
      if (name != null || label != null)
      {
         throw ownerException("Cannot create navigation when name or label is specified along with priority",
            ownerType, ownerId, Status.BAD_REQUEST);
      }

      // Validate priority
      String s = String.valueOf(priority);
      validateNonNullParameter(s, "priority");

      return doRequest(uriInfo, new ComponentRequestCallback<ModelDataStorage, NavigationData>()
      {
         @Override
         public NavigationData inRequest(ModelDataStorage dataStorage) throws Exception
         {
            ensureNavigationDoesNotExist(ownerType, ownerId, dataStorage);

            NavigationData navigation = new NavigationData(ownerType, ownerId, priority, Collections.<NavigationNodeData>emptyList());

            if (log.isDebugEnabled())
            {
               log.debug(createMessage("Creating navigation with priority", ownerType, ownerId));
            }

            // Create navigation
            dataStorage.create(navigation);

            // Fetch and return the navigation recently created
            return getNavigation(ownerType, ownerId, dataStorage);
         }
      });
   }

   @PUT
   @Path("/{parent-nav-uri:.*}")
   @RolesAllowed("administrators")
   @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
   public Response createNavigation(@Context UriInfo uriInfo,
                                    @QueryParam("ownerType") String type,
                                    @QueryParam("ownerId") String id,
                                    @PathParam("parent-nav-uri") final String parentNavigationUri,
                                    @QueryParam("name") final String name,
                                    @QueryParam("label") final String label)
   {
      final String ownerType = checkOwnerType(type);
      final String ownerId = checkOwnerId(ownerType, id);

      validateNonNullParameter(name, "name");
      validateNonNullParameter(label, "label");

      return doRequest(uriInfo, new ComponentRequestCallback<ModelDataStorage, NavigationData>()
      {
         @Override
         public NavigationData inRequest(ModelDataStorage dataStorage) throws Exception
         {
            NavigationData navigation = ensureNavigationExists(ownerType, ownerId, dataStorage);
            NavigationNodeData node = createChild(navigation, parentNavigationUri, name, label);

            if (log.isDebugEnabled())
            {
               log.debug(createMessage("Creating navigation", ownerType, ownerId, node.getURI()));
            }

            // Save navigation
            dataStorage.save(navigation);
            return getNavigation(ownerType, ownerId, node.getURI(), dataStorage);
         }
      });
   }

   @POST
   @RolesAllowed("administrators")
   @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
   public Response updateNavigation(@Context UriInfo uriInfo,
                                    @QueryParam("ownerType") String type,
                                    @QueryParam("ownerId") String id,
                                    NavigationData data)
   {
      final String ownerType = checkOwnerType(type);
      final String ownerId = checkOwnerId(ownerType, id);
      if (log.isDebugEnabled())
      {
         log.debug(createMessage("Updating navigation", ownerType, ownerId));
      }

      final NavigationData navigation = PortalObjectsUtils.fixOwner(ownerType, ownerId, data);
      return doRequest(uriInfo, new ComponentRequestCallbackNoResult<ModelDataStorage>()
      {
         @Override
         public void inRequestNoResult(ModelDataStorage dataStorage) throws Exception
         {
            ensureNavigationExists(ownerType, ownerId, dataStorage);
            dataStorage.save(navigation);
         }
      });
   }

   @POST
   @Path("/{nav-uri:.*}")
   @RolesAllowed("administrators")
   @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
   public Response updateNavigation(@Context UriInfo uriInfo,
                                    @QueryParam("ownerType") String type,
                                    @QueryParam("ownerId") String id,
                                    @PathParam("nav-uri") final String navigationUri,
                                    final NavigationData data)
   {
      final String ownerType = checkOwnerType(type);
      final String ownerId = checkOwnerId(ownerType, id);

      // Validate request data
      List<NavigationNodeData> nodes = data.getNodes();
      if (nodes == null)
      {
         throw ownerException("No navigation nodes found in requst, cannot update navigation", ownerType, ownerId, navigationUri, Status.BAD_REQUEST);
      }
      if (nodes.size() != 1)
      {
         throw ownerException("Only one navigation node aloud for request, cannot update navigation", ownerType, ownerId, navigationUri, Status.BAD_REQUEST);
      }

      // Ensure URI of request data matches URI of URL Scheme
      final NavigationNodeData node = nodes.get(0);
      if (!node.getURI().equals(navigationUri))
      {
         throw ownerException("Invalid node uri " + node.getURI() +
            " within the body of the request. Cannot update navigation", ownerType, ownerId, navigationUri, Status.BAD_REQUEST);
      }

      if (log.isDebugEnabled())
      {
         log.debug(createMessage("Updating navigation", ownerType, ownerId, navigationUri));
      }

      return doRequest(uriInfo, new ComponentRequestCallbackNoResult<ModelDataStorage>()
      {
         @Override
         public void inRequestNoResult(ModelDataStorage dataStorage) throws Exception
         {
            NavigationData navigation = ensureNavigationExists(ownerType, ownerId, dataStorage);

            // Replace node in navigation
            updateNavigationNode(navigation, node);

            DataStorage ds = (DataStorage) ExoContainerContext.getCurrentContainer().
               getComponentInstanceOfType(DataStorage.class);

            ds.save(new PageNavigation(navigation));

            // Save
            //dataStorage.save(navigation);
         }
      });
   }

   @DELETE
   @RolesAllowed("administrators")
   @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
   public Response deleteNavigation(@Context UriInfo uriInfo,
                                    @QueryParam("ownerType") String type,
                                    @QueryParam("ownerId") String id)
   {
      final String ownerType = checkOwnerType(type);
      final String ownerId = checkOwnerId(ownerType, id);

      if (log.isDebugEnabled())
      {
         log.debug(createMessage("Deleting navigation", ownerType, ownerId));
      }

      return doRequest(uriInfo, new ComponentRequestCallbackNoResult<ModelDataStorage>()
      {
         @Override
         public void inRequestNoResult(ModelDataStorage dataStorage) throws Exception
         {
            NavigationData navigation = ensureNavigationExists(ownerType, ownerId, dataStorage);
            dataStorage.remove(navigation);
         }
      });
   }

   @DELETE
   @Path("/{nav-uri:.*}")
   @RolesAllowed("administrators")
   @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
   public Response deleteNavigation(@Context UriInfo uriInfo,
                                    @QueryParam("ownerType") String type,
                                    @QueryParam("ownerId") String id,
                                    @PathParam("nav-uri") final String navigationUri)
   {
      final String ownerType = checkOwnerType(type);
      final String ownerId = checkOwnerId(ownerType, id);

      if (log.isDebugEnabled())
      {
         log.debug(createMessage("Deleting navigation", ownerType, ownerId, navigationUri));
      }

      return doRequest(uriInfo, new ComponentRequestCallbackNoResult<ModelDataStorage>()
      {
         @Override
         public void inRequestNoResult(ModelDataStorage dataStorage) throws Exception
         {
            NavigationData navigation = ensureNavigationExists(ownerType, ownerId, dataStorage);

            String parentUri = getParentUri(navigationUri);
            List<NavigationNodeData> siblings;
            if (parentUri == null)
            {
               siblings = navigation.getNodes();
            }
            else
            {
               NavigationNodeData parent = ensureNodeExists(navigation, parentUri);
               siblings = parent.getNodes();
            }
            String name = getNameForUri(navigationUri);
            NavigationNodeData node = getNode(siblings, name);
            if (node == null)
            {
               throw ownerException("Node does not exist, cannot delete navigation", ownerType, ownerId, navigationUri, Status.NOT_FOUND);
            }

            siblings.remove(node);
            dataStorage.save(navigation);
         }
      });
   }

   private NavigationData getNavigation(String ownerType, String ownerId, ModelDataStorage dataStorage)
   {
      try
      {
         return dataStorage.getPageNavigation(new NavigationKey(ownerType, ownerId));
      }
      catch (Exception e)
      {
         throw ownerException("Unknown error getting navigation", ownerType, ownerId, Status.INTERNAL_SERVER_ERROR);
      }
   }

   private NavigationData getNavigation(String ownerType, String ownerId, String uri, ModelDataStorage dataStorage)
   {
      try
      {
         NavigationData navigation = ensureNavigationExists(ownerType, ownerId, dataStorage);
         NavigationNodeData node = findNodeByUri(navigation.getNodes(), uri);
         if (node == null) return null;

         List<NavigationNodeData> nodes = new ArrayList<NavigationNodeData>();
         nodes.add(node);
         return new NavigationData(navigation.getStorageId(), navigation.getOwnerType(), navigation.getOwnerId(), navigation.getPriority(), nodes);
      }
      catch (Exception e)
      {
         throw ownerException("Unknown error getting navigation", ownerType, ownerId, Status.INTERNAL_SERVER_ERROR);
      }
   }

   private NavigationData ensureNavigationExists(String ownerType, String ownerId, ModelDataStorage dataStorage) throws WebApplicationException
   {
      NavigationData navigationData = getNavigation(ownerType, ownerId, dataStorage);
      if (navigationData == null)
      {
         throw ownerException("Navigation does not exist", ownerType, ownerId, Status.NOT_FOUND);
      }

      return navigationData;
   }

   private void ensureNavigationDoesNotExist(String ownerType, String ownerId, ModelDataStorage dataStorage) throws WebApplicationException
   {
      NavigationData navigationData = getNavigation(ownerType, ownerId, dataStorage);
      if (navigationData != null) throw ownerException("Navigation already exists", ownerType, ownerId, Status.FORBIDDEN);
   }

   private NavigationNodeData ensureNodeExists(NavigationData navigation, String uri)
   {
      NavigationNodeData existing = findNodeByUri(navigation.getNodes(), uri);
      if (existing == null)
      {
         throw ownerException("Navigation node does not exist", navigation.getOwnerType(), navigation.getOwnerId(), uri, Status.NOT_FOUND);
      }

      return existing;
   }

   private NavigationNodeData createChild(NavigationData navigation, String parentUri, String name, String label)
   {
      String ownerType = navigation.getOwnerType();
      String ownerId = navigation.getOwnerId();

      List<NavigationNodeData> siblings;
      String uri;
      if (parentUri == null)
      {
         uri = name;
         siblings = navigation.getNodes();
      }
      else
      {
         // Ensure parent exists, before attempting ot create new node
         NavigationNodeData parent = ensureNodeExists(navigation, parentUri);
         uri = parent.getURI() + "/" + name;
         siblings = parent.getNodes();
      }

      // Ensure child doesn't already exist
      if (getNode(siblings, name) != null)
      {
         throw ownerException("Node already exists, cannot create navigation", ownerType, ownerId, uri, Status.FORBIDDEN);
      }

      // Create new node, with no children
      NavigationNodeData newNode =
         new NavigationNodeData(uri, label, null, name, null, null, null, null, Collections.<NavigationNodeData>emptyList());

      // Add it to the list
      siblings.add(newNode);

      return newNode;
   }

   private void updateNavigationNode(NavigationData navigation, NavigationNodeData node)
   {
      String parentUri = getParentUri(node.getURI());
      List<NavigationNodeData> siblings;
      if (parentUri == null)
      {
         siblings = navigation.getNodes();
      }
      else
      {
         NavigationNodeData parent = ensureNodeExists(navigation, parentUri);
         siblings = parent.getNodes();
      }

      NavigationNodeData existing = getNode(siblings, node.getName());
      if (existing == null)
      {
         throw ownerException("Node does not exist, cannot update navigation",
            navigation.getOwnerType(), navigation.getOwnerId(), node.getURI(), Status.NOT_FOUND);
      }

      int index = siblings.indexOf(existing);
      siblings.set(index, node);
   }

   private WebApplicationException ownerException(String message, String ownerType, String ownerId, String uri, Response.Status status)
   {
      return exception(createMessage(message, ownerType, ownerId, uri), status);
   }

   private String createMessage(String message, String ownerType, String ownerId, String uri)
   {
      return new StringBuilder().append(message).append(" for [ownerType=").append(ownerType)
         .append(", ownerId=").append(ownerId)
         .append(", uri=").append(uri)
         .append("]").toString();
   }

   private NavigationNodeData getNode(List<NavigationNodeData> nodes, String name)
   {
      for (NavigationNodeData node : nodes)
      {
         if (node.getName().equals(name))
         {
            return node;
         }
      }

      return null;
   }

   private NavigationNodeData findNodeByUri(List<NavigationNodeData> nodes, String uri)
   {
      if (uri.charAt(0) == '/') uri = uri.substring(1);
      if (uri.charAt(uri.length()-1) == '/') uri = uri.substring(0, uri.length() - 1);

      int index = uri.indexOf('/');
      if (index != -1)
      {
         String childName = uri.substring(0, index);
         String grandChildren = uri.substring(index+1, uri.length());
         NavigationNodeData child = getNode(nodes, childName);
         if (child == null) return null;

         return findNodeByUri(child.getNodes(), grandChildren);
      }
      else
      {
         return getNode(nodes, uri);
      }
   }

   private String getNameForUri(String uri)
   {
      return uri.substring(uri.lastIndexOf('/') + 1, uri.length());
   }

   private String getParentUri(String uri)
   {
      if (!uri.contains("/"))
      {
         return null;
      }
      return uri.substring(0, uri.lastIndexOf('/'));
   }
}
