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

package org.gatein.management.pomdata.core.api;

import org.exoplatform.portal.pom.data.ModelDataStorage;
import org.exoplatform.portal.pom.data.NavigationData;
import org.exoplatform.portal.pom.data.NavigationKey;
import org.exoplatform.portal.pom.data.NavigationNodeData;
import org.gatein.management.ManagementException;
import org.gatein.management.pomdata.api.NavigationManagementService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
//TODO: Add debugging
public class NavigationManagementServiceImpl implements NavigationManagementService
{
   private ModelDataStorage dataStorage;

   public NavigationManagementServiceImpl(ModelDataStorage dataStorage)
   {
      this.dataStorage = dataStorage;
   }

   @Override
   public NavigationData getNavigation(String ownerType, String ownerId) throws ManagementException
   {
      try
      {
         return dataStorage.getPageNavigation(new NavigationKey(ownerType, ownerId));
      }
      catch (Exception e)
      {
         throw new ManagementException("Could not retrieve navigation for ownerType " + ownerType + " and ownerId " + ownerId, e);
      }
   }

   @Override
   public NavigationData getNavigation(String ownerType, String ownerId, String navigationPath) throws ManagementException
   {
      if (navigationPath.charAt(0) == '/') navigationPath = navigationPath.substring(1);

      NavigationData defaultNav = getNavigation(ownerType, ownerId);

      NavigationNodeData node = getNavigationByPath(defaultNav.getNodes(), navigationPath);
      if (node != null)
      {
         List<NavigationNodeData> nodes = new ArrayList<NavigationNodeData>(1);
         nodes.add(node);
         return new NavigationData(defaultNav.getOwnerType(), defaultNav.getOwnerId(), defaultNav.getPriority(), nodes);
      }
      else
      {
         return null;
      }
   }

   private NavigationNodeData getNavigationByPath(List<NavigationNodeData> nodes, String path)
   {
      int index = path.indexOf('/');
      if (index != -1)
      {
         String childName = path.substring(0, index);
         String grandChildren = path.substring(index+1, path.length());
         NavigationNodeData child = findChild(nodes, childName);
         if (child == null) return null;

         return getNavigationByPath(child.getNodes(), grandChildren);
      }
      else
      {
         return findChild(nodes, path);
      }
   }

   private NavigationNodeData findChild(List<NavigationNodeData> nodes, String childName)
   {
      for (NavigationNodeData node : nodes)
      {
         if (node.getName().equals(childName))
         {
            return node;
         }
      }

      return null;
   }
}
