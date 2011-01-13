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

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
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
         throw new ManagementException("Could not retrieve " + navigationToString(ownerType, ownerId, null), e);
      }
   }

   @Override
   public NavigationNodeData getNavigationNode(String ownerType, String ownerId, String uri) throws ManagementException
   {
      uri = fixNavigationPath(uri);

      NavigationData defaultNav = getNavigation(ownerType, ownerId);
      return getNavigationNodeByUri(defaultNav.getNodes(), uri);
   }

   @Override
   public NavigationData createNavigation(String ownerType, String ownerId, Integer priority) throws ManagementException
   {
      NavigationData defaultNav = getNavigation(ownerType, ownerId);
      if (defaultNav != null)
      {
         throw new ManagementException("Cannot create navigation.  " +
            navigationToString(ownerType, ownerId, null) + " already exists.");
      }

      try
      {
         dataStorage.create(new NavigationData(ownerType, ownerId, priority, Collections.<NavigationNodeData>emptyList()));
         return dataStorage.getPageNavigation(new NavigationKey(ownerType, ownerId));
      }
      catch (Exception e)
      {
         throw new ManagementException("Exception creating " + navigationToString(ownerType, ownerId, null));
      }
   }

   @Override
   public NavigationNodeData createNavigationNode(String ownerType, String ownerId, String uri, String label) throws ManagementException
   {
      NavigationData defaultNav = getNavigation(ownerType, ownerId);
      if (defaultNav == null)
      {
         throw new ManagementException("Cannot create navigation node. " +
            navigationToString(ownerType, ownerId, null) + " does not exist.");
      }
      uri = fixNavigationPath(uri);

      // Ensure navigation with uri doesn't already exist.
      if (getNavigationNodeByUri(defaultNav.getNodes(), uri) != null)
      {
         throw new ManagementException("Cannot create navigation. " +
            navigationToString(ownerType, ownerId, uri) + " already exists.");
      }

      // Ensure parent exists
      String parentPath = getParentUri(uri);
      NavigationNodeData parent = getNavigationNodeByUri(defaultNav.getNodes(), parentPath);
      if (parent == null)
      {
         throw new ManagementException("Cannot create navigation. " +
            navigationToString(ownerType, ownerId, uri) + "'s parent does not exist.");
      }

      // Create new node with no children, add to parent.
      String name = getNameFromUri(uri);
      NavigationNodeData node = new NavigationNodeData(uri, label, null, name, null, null, null, null, Collections.<NavigationNodeData>emptyList());
      parent.getNodes().add(node);

      // Save navigation
      try
      {
         dataStorage.save(defaultNav);
         return node;
      }
      catch (Exception e)
      {
         throw new ManagementException("Exception creating " + navigationToString(ownerType, ownerId, uri), e);
      }
   }

   @Override
   public void updateNavigation(NavigationData navigation) throws ManagementException
   {
      try
      {
         dataStorage.save(navigation);
      }
      catch (Exception e)
      {
         throw new ManagementException("Exception updating " + navigationToString(navigation.getOwnerType(), navigation.getOwnerId(), null), e);
      }
   }

   @Override
   public void updateNavigationNode(String ownerType, String ownerId, NavigationNodeData node) throws ManagementException
   {
      NavigationData defaultNav = getNavigation(ownerType, ownerId);
      if (defaultNav == null)
      {
         throw new ManagementException("Cannot update navigation node. " +
            navigationToString(ownerType, ownerId, null) + " does not exist.");
      }

      // Ensure the node exists
      if (getNavigationNodeByUri(defaultNav.getNodes(), node.getURI()) == null)
      {
         throw new ManagementException("Cannot update navigation. " +
            navigationToString(ownerType, ownerId, node.getURI()) + " does not exist.");
      }

      // Get the parent
      String parentUri = getParentUri(node.getURI());
      NavigationNodeData parent = getNavigationNodeByUri(defaultNav.getNodes(), parentUri);
      List<NavigationNodeData> children = parent.getNodes();

      // Get the name of the child.
      String name = getNameFromUri(node.getURI());
      for (int i=0; i<children.size(); i++)
      {
         if (children.get(i).getName().equals(name))
         {
            // Replace child
            children.set(i, node);
            break;
         }
      }

      // Save navigation
      try
      {
         dataStorage.save(defaultNav);
      }
      catch (Exception e)
      {
         throw new ManagementException("Exception updating " + navigationToString(ownerType, ownerId, node.getURI()), e);
      }
   }

   @Override
   public void deleteNavigation(String ownerType, String ownerId) throws ManagementException
   {
      NavigationData defaultNav = getNavigation(ownerType, ownerId);
      if (defaultNav == null)
      {
         throw new ManagementException("Cannot delete navigation. " + navigationToString(ownerType, ownerId, null) + " does not exist.");
      }

      // Delete navigation
      try
      {
         dataStorage.remove(defaultNav);
      }
      catch (Exception e)
      {
         throw new ManagementException("Exception deleting " + navigationToString(ownerType, ownerId, null), e);
      }
   }

   @Override
   public void deleteNavigationNode(String ownerType, String ownerId, String uri) throws ManagementException
   {
      NavigationData defaultNav = getNavigation(ownerType, ownerId);
      if (defaultNav == null)
      {
         throw new ManagementException("Cannot delete navigation. " + navigationToString(ownerType, ownerId, null) + " does not exist.");
      }

      // Ensure the node exists
      NavigationNodeData node = getNavigationNodeByUri(defaultNav.getNodes(), uri);
      if (node == null)
      {
         throw new ManagementException("Cannot delete navigation. " +
            navigationToString(ownerType, ownerId, uri) + " does not exist.");
      }

      // Get the parent
      String parentUri = getParentUri(node.getURI());
      NavigationNodeData parent = getNavigationNodeByUri(defaultNav.getNodes(), parentUri);
      List<NavigationNodeData> children = parent.getNodes();

      // Get the name of the child.
      String name = getNameFromUri(node.getURI());
      NavigationNodeData found = null;
      for (NavigationNodeData child : children)
      {
         if (child.getName().equals(name))
         {
            found = child;
         }
      }
      if (found == null)
      {
         throw new ManagementException("Cannot delete navigation. Could not find child once parent was located for " +
            navigationToString(ownerType, ownerId, node.getURI()));
      }
      if (!children.remove(found))
      {
         throw new ManagementException("Cannot delete navigation. Could not remove node from parent.");
      }

      // Save navigation
      try
      {
         dataStorage.save(defaultNav);
      }
      catch (Exception e)
      {
         throw new ManagementException("Exception deleting  " + navigationToString(ownerType, ownerId, node.getURI()), e);
      }
   }

   private String fixNavigationPath(String navigationPath)
   {
      if (navigationPath.charAt(0) == '/') navigationPath = navigationPath.substring(1);
      if (navigationPath.endsWith("/")) navigationPath = navigationPath.substring(0, navigationPath.length() - 1);

      return navigationPath;
   }

   private String getNameFromUri(String uri)
   {
      return uri.substring(uri.lastIndexOf('/') + 1, uri.length());
   }

   private String getParentUri(String uri)
   {
      return uri.substring(0, uri.lastIndexOf('/'));
   }

   private NavigationNodeData getNavigationNodeByUri(List<NavigationNodeData> nodes, String uri)
   {
      int index = uri.indexOf('/');
      if (index != -1)
      {
         String childName = uri.substring(0, index);
         String grandChildren = uri.substring(index+1, uri.length());
         NavigationNodeData child = findChild(nodes, childName);
         if (child == null) return null;

         return getNavigationNodeByUri(child.getNodes(), grandChildren);
      }
      else
      {
         return findChild(nodes, uri);
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

   private String navigationToString(String ownerType, String ownerId, String uri)
   {
      StringBuilder sb = new StringBuilder().append("Navigation[ownerType=").append(ownerType);
      sb.append(", ownerId=").append(ownerId);
      if (uri != null)
      {
         sb.append(", uri=").append(uri);
      }
      sb.append("]");

      return sb.toString();
   }
}
