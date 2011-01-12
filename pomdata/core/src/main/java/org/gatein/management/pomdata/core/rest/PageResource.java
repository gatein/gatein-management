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

import org.exoplatform.portal.pom.data.PageData;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.ManagementException;
import org.gatein.management.pomdata.api.PageManagementService;

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
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
//TODO: Add debugging
public class PageResource extends AbstractExoContainerResource<PageManagementService>
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
                            @QueryParam("ownerType") String ownerType,
                            @QueryParam("ownerId") String ownerId)
   {
      return handleRequest(ownerType, ownerId, uriInfo, new RestfulManagementServiceCallback<PageManagementService, GenericEntity<List<PageData>>>()
      {
         @Override
         public GenericEntity<List<PageData>> doService(PageManagementService service, String ownerType, String ownerId) throws ManagementException
         {
            List<PageData> pages = service.getPages(ownerType, ownerId);
            if (pages == null || pages.isEmpty())
            {
               return null;
            }
            else
            {
               // This ensures that the generic information can be retrieved at runtime
               return new GenericEntity<List<PageData>>(pages)
               {
               };
            }
         }
      });
   }

   @GET
   @Path("/{page-name}")
   @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
   public Response getPage(@Context UriInfo uriInfo,
                           @QueryParam("ownerType") String ownerType,
                           @QueryParam("ownerId") String ownerId,
                           @PathParam("page-name") final String pageName)
   {
      return handleRequest(ownerType, ownerId, uriInfo, new RestfulManagementServiceCallback<PageManagementService, PageData>()
      {
         @Override
         public PageData doService(PageManagementService service, String ownerType, String ownerId) throws ManagementException
         {
            return service.getPage(ownerType, ownerId, pageName);
         }
      });
   }

   // This method doesn't follow RESTful guidelines for a create. We should be returning the location of the newly created
   // resource as part of the response.
   @POST
   @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
   public Response createPage(@Context UriInfo uriInfo,
                              @QueryParam("ownerType") String ownerType,
                              @QueryParam("ownerId") String ownerId,
                              @QueryParam("name") final String pageName,
                              @QueryParam("title") final String title)
   {
      validateNonNullParameter(pageName, "name");
      validateNonNullParameter(title, "title");

      return handleRequest(ownerType, ownerId, uriInfo, new RestfulManagementServiceCallback<PageManagementService, PageData>()
      {
         @Override
         public PageData doService(PageManagementService service, String ownerType, String ownerId) throws ManagementException
         {
            return service.createPage(ownerType, ownerId, pageName, title);
         }
      });
   }

   @PUT
   @Path("/{page-name}")
   @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
   public Response updatePage(@Context UriInfo uriInfo,
                              @QueryParam("ownerType") String ownerType,
                              @QueryParam("ownerId") String ownerId,
                              @PathParam("page-name") String pageName,
                              PageData page)
   {
      if (!page.getName().equals(pageName)) throw new WebApplicationException(new Exception("URI page name is not same as page name in data."), Response.Status.BAD_REQUEST);

      final PageData pageData = updateData(ownerType, ownerId, page);

      return handleRequest(ownerType, ownerId, uriInfo, new AbstractMgmtServiceCallbackNoResult<PageManagementService>()
      {
         @Override
         public void doServiceNoResult(PageManagementService service, String ownerType, String ownerId)
         {
            service.updatePage(pageData);
         }
      });
   }

   @DELETE
   @Path("/{page-name}")
   @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
   public Response deletePage(@Context UriInfo uriInfo,
                              @QueryParam("ownerType") String ownerType,
                              @QueryParam("ownerId") String ownerId,
                              @PathParam("page-name") final String pageName)
   {
      return handleRequest(ownerType, ownerId, uriInfo, new AbstractMgmtServiceCallbackNoResult<PageManagementService>()
      {
         @Override
         public void doServiceNoResult(PageManagementService service, String ownerType, String ownerId)
         {
            service.deletePage(ownerType, ownerId, pageName);
         }
      });
   }

   private PageData updateData(String ownerType, String ownerId, PageData originalPage)
   {
      return new PageData(originalPage.getStorageId(), originalPage.getId(), originalPage.getName(), originalPage.getIcon(),
         originalPage.getTemplate(), originalPage.getFactoryId(), originalPage.getTitle(), originalPage.getDescription(),
         originalPage.getWidth(), originalPage.getHeight(), originalPage.getAccessPermissions(), originalPage.getChildren(),
         ownerType, ownerId, originalPage.getEditPermission(), originalPage.isShowMaxWindow());
   }
}
