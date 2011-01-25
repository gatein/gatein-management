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

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.pom.data.ComponentData;
import org.exoplatform.portal.pom.data.ModelDataStorage;
import org.exoplatform.portal.pom.data.PageData;
import org.exoplatform.portal.pom.data.PageKey;
import org.exoplatform.portal.pom.data.PortalData;
import org.exoplatform.portal.pom.data.PortalKey;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.core.rest.ComponentRequestCallback;
import org.gatein.management.core.rest.ComponentRequestCallbackNoResult;
import org.gatein.management.portalobjects.common.utils.PortalObjectsUtils;

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
import javax.ws.rs.core.GenericEntity;
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
//TODO: Add debugging
public class PageResource extends BasePortalObjectsResource
{
   private static final Logger log = LoggerFactory.getLogger(PageResource.class);

   public PageResource(String containerName)
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
   public Response getPages(@Context UriInfo uriInfo,
                            @QueryParam("ownerType") String type,
                            @QueryParam("ownerId") String id)
   {
      final String ownerType = checkOwnerType(type);
      final String ownerId = checkOwnerId(ownerType, id);

      if (log.isDebugEnabled())
      {
         log.debug(createMessage("Retrieving pages", ownerType, ownerId));
      }

      return doRequest(uriInfo, new ComponentRequestCallback<ModelDataStorage, GenericEntity<List<PageData>>>()
      {
         @Override
         public GenericEntity<List<PageData>> inRequest(ModelDataStorage modelDataStorage) throws Exception
         {
            Query<PageData> query = new Query<PageData>(ownerType, ownerId, PageData.class);
            LazyPageList<PageData> results = modelDataStorage.find(query);

            List<PageData> list = new ArrayList<PageData>(results.getAll());
            if (list.isEmpty())
            {
               return null;
            }

            //TODO: Do we want to sort on page name or accept the order of what's returned from data storage ?
//               Collections.sort(list, new Comparator<PageData>()
//               {
//                  @Override
//                  public int compare(PageData page1, PageData page2)
//                  {
//                     return page1.getName().compareTo(page2.getName());
//                  }
//               });
               return new GenericEntity<List<PageData>>(list){};
         }
      });
   }

   @GET
   @Path("/{page-name}")
   @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
   public Response getPage(@Context UriInfo uriInfo,
                           @QueryParam("ownerType") String type,
                           @QueryParam("ownerId") String id,
                           @PathParam("page-name") final String pageName)
   {
      final String ownerType = checkOwnerType(type);
      final String ownerId = checkOwnerId(ownerType, id);

      if (log.isDebugEnabled())
      {
         log.debug(createMessage("Retrieving page", ownerType, ownerId, pageName));
      }

      return doRequest(uriInfo, new ComponentRequestCallback<ModelDataStorage, PageData>()
      {
         @Override
         public PageData inRequest(ModelDataStorage dataStorage) throws Exception
         {
            return dataStorage.getPage(new PageKey(ownerType, ownerId, pageName));
         }
      });
   }

   // This method doesn't follow RESTful guidelines for a create. We should be returning the location of the newly created
   // resource as part of the response.
   @POST
   @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
   public Response createPage(@Context UriInfo uriInfo,
                              @QueryParam("ownerType") String type,
                              @QueryParam("ownerId") String id,
                              @QueryParam("name") final String pageName,
                              @QueryParam("title") final String title)
   {
      final String ownerType = checkOwnerType(type);
      final String ownerId = checkOwnerId(ownerType, id);

      validateNonNullParameter(pageName, "name");
      validateNonNullParameter(title, "title");

      if (log.isDebugEnabled())
      {
         log.debug(createMessage("Creating page", ownerType, ownerId, pageName));
      }

      return doRequest(uriInfo, new ComponentRequestCallback<ModelDataStorage, PageData>()
      {
         @Override
         public PageData inRequest(ModelDataStorage dataStorage) throws Exception
         {
            PortalData portalData = dataStorage.getPortalConfig(new PortalKey(ownerType, ownerId));
            if (portalData == null)
            {
               throw ownerException("Site does not exist, cannot create page", ownerType, ownerId, pageName, Status.NOT_FOUND);
            }
            ensurePageDoesNotExist(ownerType, ownerId, pageName, dataStorage);

            // Create new page with same permissions of the site
            PageData page = new PageData(null, null, pageName, null, null, null, title, null, null, null,
               portalData.getAccessPermissions(), Collections.<ComponentData>emptyList(), ownerType, ownerId,
               portalData.getEditPermission(), false);
            dataStorage.create(page);

            // Return the newly saved page
            return getPage(ownerType, ownerId, pageName, dataStorage);
         }
      });
   }

   @PUT
   @Path("/{page-name}")
   @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
   public Response updatePage(@Context UriInfo uriInfo,
                              @QueryParam("ownerType") String type,
                              @QueryParam("ownerId") String id,
                              @PathParam("page-name") final String pageName,
                              PageData page)
   {
      final String ownerType = checkOwnerType(type);
      final String ownerId = checkOwnerId(ownerType, id);

      if (!page.getName().equals(pageName))
      {
         throw ownerException("Invalid page name " + page.getName() +
            " within the body of the request. Cannot update page", ownerType, ownerId, pageName, Status.BAD_REQUEST);
      }

      final PageData pageData = PortalObjectsUtils.fixOwner(ownerType, ownerId, page);

      if (log.isDebugEnabled())
      {
         log.debug(createMessage("Updating page", ownerType, ownerId, pageName));
      }

      return doRequest(uriInfo, new ComponentRequestCallbackNoResult<ModelDataStorage>()
      {
         @Override
         public void inRequestNoResult(ModelDataStorage dataStorage) throws Exception
         {
            ensurePageExists(ownerType, ownerId, pageName, dataStorage);
            dataStorage.save(pageData);
         }
      });
   }

   @DELETE
   @Path("/{page-name}")
   @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
   public Response deletePage(@Context UriInfo uriInfo,
                              @QueryParam("ownerType") String type,
                              @QueryParam("ownerId") String id,
                              @PathParam("page-name") final String pageName)
   {
      final String ownerType = checkOwnerType(type);
      final String ownerId = checkOwnerId(ownerType, id);

      if (log.isDebugEnabled())
      {
         log.debug(createMessage("Deleting page", ownerType, ownerId, pageName));
      }

      return doRequest(uriInfo, new ComponentRequestCallbackNoResult<ModelDataStorage>()
      {
         @Override
         public void inRequestNoResult(ModelDataStorage dataStorage) throws Exception
         {
            PageData page = ensurePageExists(ownerType, ownerId, pageName, dataStorage);
            dataStorage.remove(page);
         }
      });
   }

   private PageData getPage(String ownerType, String ownerId, String name, ModelDataStorage dataStorage) throws Exception
   {
      return dataStorage.getPage(new PageKey(ownerType, ownerId, name));
   }

   private PageData ensurePageExists(String ownerType, String ownerId, String name, ModelDataStorage dataStorage) throws Exception
   {
      PageData page = getPage(ownerType, ownerId, name, dataStorage);
      if (page == null)
      {
         throw ownerException("Page does not exist", ownerType, ownerId, name, Status.NOT_FOUND);
      }

      return page;
   }

   private void ensurePageDoesNotExist(String ownerType, String ownerId, String name, ModelDataStorage dataStorage) throws Exception
   {
      PageData page = getPage(ownerType, ownerId, name, dataStorage);
      if (page != null)
      {
         throw ownerException("Page already exists", ownerType, ownerId, name, Status.NOT_FOUND);
      }
   }

   private WebApplicationException ownerException(String message, String ownerType, String ownerId, String pageName, Response.Status status)
   {
      return exception(createMessage(message, ownerType, ownerId, pageName), status);
   }

   private String createMessage(String message, String ownerType, String ownerId, String pageName)
   {
      return new StringBuilder().append(message).append(" for ownerType ").append(ownerType)
         .append(" and ownerId ").append(ownerId)
         .append(" and pageName ").append(pageName)
         .toString();
   }
}
