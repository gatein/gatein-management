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

package org.gatein.management.rest;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
@Path("/portalinfo/{portal-container}/restcontext")
public class RestContextResource
{
   private static final Logger log = LoggerFactory.getLogger(RestContextResource.class);

   @GET
   public Response getRestContext(@PathParam("portal-container") String portalContainer)
   {
      RootContainer container = RootContainer.getInstance();
      PortalContainer pc = container.getPortalContainer(portalContainer);
      if (pc == null)
      {
         String message = "Unknown portal container '" + portalContainer + "'";
         if (log.isDebugEnabled())
         {
            log.error(message);
         }
         return Response.status(Response.Status.NOT_FOUND).entity(message).build();
      }

      return Response.ok().entity(pc.getRestContextName()).build();
   }
}
