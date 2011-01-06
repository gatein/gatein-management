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

import org.exoplatform.portal.pom.data.PortalData;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.ManagementException;
import org.gatein.management.pomdata.api.SiteManagementService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
//TODO: Add debugging
public class SiteResource extends AbstractExoContainerResource<SiteManagementService>
{
   private static final Logger log = LoggerFactory.getLogger(SiteResource.class);

   public SiteResource(String containerName)
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
   public GenericEntity<List<PortalData>> getPortalData(@Context UriInfo uriInfo, @QueryParam("ownerType") String ownerType)
   {
      ownerType = checkOwnerType(ownerType);

      List<PortalData> data = null;
      try
      {
         data = getService().getPortalData(ownerType);
      }
      catch (ManagementException e)
      {
         handleManagementException(e, uriInfo);
      }
      catch (Throwable t)
      {
         handleUnknownError(t, uriInfo);
      }

      if (data == null || data.isEmpty()) data = null;
      checkNullResult(data, uriInfo);

      return new GenericEntity<List<PortalData>>(data){};
   }

   @GET
   @Path("/{owner-id:.*}")
   @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
   public PortalData getPortalData(@Context UriInfo uriInfo, @QueryParam("ownerType") String ownerType,
                                   @PathParam("owner-id") String ownerId)
   {
      ownerType = checkOwnerType(ownerType);
      if ("group".equals(ownerType) && ownerId.indexOf(0) != '/')
      {
         ownerId = "/" + ownerId;
      }

      PortalData data = null;
      try
      {
         data = getService().getPortalData(ownerType, ownerId);
      }
      catch (ManagementException e)
      {
         handleManagementException(e, uriInfo);
      }
      catch (Throwable t)
      {
         handleUnknownError(t, uriInfo);
      }

      checkNullResult(data, uriInfo);
      return data;
   }
}
