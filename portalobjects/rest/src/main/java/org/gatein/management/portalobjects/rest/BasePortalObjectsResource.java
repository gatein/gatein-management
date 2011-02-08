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

import org.exoplatform.portal.pom.data.ModelDataStorage;
import org.gatein.management.core.rest.AbstractContainerResource;
import org.gatein.management.portalobjects.common.utils.PortalObjectsUtils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
//TODO: Add validation for some fields like page name, nav name and uri, make sure name and nav uri are consistent
public abstract class BasePortalObjectsResource extends AbstractContainerResource<ModelDataStorage>
{

   protected String checkOwnerType(String ownerType)
   {
      if (ownerType == null) ownerType = "portal"; // Default to portal, not required as query parameter in URL.

      if (PortalObjectsUtils.isValidOwnerType(ownerType))
      {
         return ownerType;
      }
      else
      {
         throw new WebApplicationException(new Exception("'" + ownerType + "' is not a valid ownerType."), Response.Status.BAD_REQUEST);
      }
   }

   protected String checkOwnerId(String ownerType, String ownerId)
   {
      if (ownerId == null)
      {
         throw new WebApplicationException(new Exception("ownerId is required for this request."), Response.Status.BAD_REQUEST);
      }

      return PortalObjectsUtils.fixOwnerId(ownerType, ownerId);
   }

   protected WebApplicationException exception(String message, Response.Status status)
   {
      return new WebApplicationException(new Exception(message), status);
   }

   protected WebApplicationException ownerException(String message, String ownerType, String ownerId, Response.Status status)
   {
      return exception(createMessage(message, ownerType, ownerId), status);
   }

   protected String createMessage(String message, String ownerType, String ownerId)
   {
      return new StringBuilder().append(message).append(" for [ownerType=").append(ownerType)
         .append(", ownerId=").append(ownerId)
         .append("]").toString();
   }
}
