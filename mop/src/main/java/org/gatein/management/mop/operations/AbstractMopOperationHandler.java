/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.gatein.management.mop.operations;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.OperationHandler;
import org.gatein.management.api.operation.ResultHandler;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public abstract class AbstractMopOperationHandler implements OperationHandler
{
   @Override
   public final void execute(OperationContext operationContext, ResultHandler resultHandler) throws ResourceNotFoundException, OperationException
   {
      String operationName = operationContext.getOperationName();
      PathAddress address = operationContext.getAddress();

      String siteType = address.resolvePathTemplate("site-type");
      if (siteType == null) throw new OperationException(operationName, "Site type was not specified.");

      ObjectType<Site> objectType = getObjectType(siteType);
      if (objectType == null)
      {
         throw new ResourceNotFoundException("No site type found for " + siteType);
      }

      POMSessionManager mgr = operationContext.getRuntimeContext().getRuntimeComponent(POMSessionManager.class);
      if (mgr == null) throw new OperationException(operationName, "Could not obtain necessary mop component from the container.");

      Workspace workspace = mgr.getSession().getWorkspace();

      if (workspace == null) throw new OperationException(operationName, "Could not obtain the MOP workspace.");

      execute(operationContext, resultHandler, workspace, objectType);
   }

   protected abstract void execute(OperationContext operationContext, ResultHandler resultHandler,
                                   Workspace workspace, ObjectType<Site> siteType) throws ResourceNotFoundException, OperationException;


   private ObjectType<Site> getObjectType(String siteType)
   {
      if (SiteType.PORTAL.getName().equals(siteType))
      {
         return ObjectType.PORTAL_SITE;
      }
      else if (SiteType.GROUP.getName().equals(siteType))
      {
         return ObjectType.GROUP_SITE;
      }
      else if (SiteType.USER.getName().equals(siteType))
      {
         return ObjectType.USER_SITE;
      }
      else
      {
         return null;
      }
   }

   protected SiteType getSiteType(ObjectType<? extends Site> objectType)
   {
      if (ObjectType.PORTAL_SITE == objectType)
      {
         return SiteType.PORTAL;
      }
      else if (ObjectType.GROUP_SITE == objectType)
      {
         return SiteType.GROUP;
      }
      else if (ObjectType.USER_SITE == objectType)
      {
         return SiteType.USER;
      }
      else
      {
         return null;
      }
   }

   protected SiteKey getSiteKey(ObjectType<? extends Site> objectType, String name)
   {
      if (ObjectType.PORTAL_SITE == objectType)
      {
         return SiteKey.portal(name);
      }
      else if (ObjectType.GROUP_SITE == objectType)
      {
         if (name.charAt(0) != '/') name = "/" + name;
         
         return SiteKey.group(name);
      }
      else if (ObjectType.USER_SITE == objectType)
      {
         return SiteKey.user(name);
      }
      else
      {
         return null;
      }
   }

   protected SiteKey getSiteKey(Site site)
   {
      return getSiteKey(site.getObjectType(), site.getName());
   }
}
