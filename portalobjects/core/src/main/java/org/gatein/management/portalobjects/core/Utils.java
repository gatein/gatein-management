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

package org.gatein.management.portalobjects.core;

import org.exoplatform.portal.pom.data.NavigationData;
import org.exoplatform.portal.pom.data.PageData;
import org.exoplatform.portal.pom.data.PortalData;

import java.util.Collection;
import java.util.Collections;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class Utils
{
   private Utils(){}

   @SuppressWarnings("unchecked")
   public static <T> T fixOwner(String ownerType, String ownerId, T data)
   {
      ownerId = fixOwnerId(ownerType, ownerId);

      if (data instanceof Collection)
      {
         return fixOwnerCollection(ownerType, ownerId, data);
      }
      else if (data instanceof PageData)
      {
         PageData page = (PageData) data;
         return (T) new PageData(page.getStorageId(), page.getId(), page.getName(), page.getIcon(),
            page.getTemplate(), page.getFactoryId(), page.getTitle(), page.getDescription(),
            page.getWidth(), page.getHeight(), page.getAccessPermissions(), page.getChildren(),
            ownerType, ownerId, page.getEditPermission(), page.isShowMaxWindow());
      }
      else if (data instanceof NavigationData)
      {
         NavigationData nav = (NavigationData) data;
         return (T) new NavigationData(nav.getStorageId(), ownerType, ownerId, nav.getPriority(), nav.getNodes());
      }
      else if (data instanceof PortalData)
      {
         PortalData pd = (PortalData) data;
         return (T) new PortalData(pd.getStorageId(), pd.getName(), ownerType, pd.getLocale(),
            pd.getAccessPermissions(), pd.getEditPermission(), pd.getProperties(), pd.getSkin(), pd.getPortalLayout());
      }
      else
      {
         return data;
      }
   }

   @SuppressWarnings("unchecked")
   private static <T> T fixOwnerCollection(String ownerType, String ownerId, T data)
   {
      Collection collection = (Collection) data;
      Object[] objects = collection.toArray();
      for (int i = 0; i < objects.length; i++)
      {
         objects[i] = fixOwner(ownerType, ownerId, objects[i]);
      }
      collection.clear();
      Collections.addAll(collection, objects);

      return (T) collection;
   }

   public static String fixOwnerId(String ownerType, String ownerId)
   {
      if ("group".equals(ownerType) && ownerId.charAt(0) != '/')
      {
         return '/' + ownerId;
      }
      else
      {
         return ownerId;
      }
   }
}
