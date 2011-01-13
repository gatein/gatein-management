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

package org.gatein.management.pomdata.api;

import org.exoplatform.portal.pom.data.NavigationData;
import org.exoplatform.portal.pom.data.NavigationNodeData;
import org.exoplatform.portal.pom.data.PageData;
import org.gatein.management.ManagementException;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public interface NavigationManagementService
{
   /**
    * Returns all navigation for the ownerType and ownerId.
    * @param ownerType the ownerType of the navigation, ie: portal, group, user
    * @param ownerId the ownerId of the navigation, ie: classic, /platform/administrators, etc
    * @return NavigationData representing all navigation for the ownerType & ownerId
    * @throws ManagementException if an exception occurs retrieving the navigation
    */
   NavigationData getNavigation(String ownerType, String ownerId) throws ManagementException;

   NavigationNodeData getNavigationNode(String ownerType, String ownerId, String uri) throws ManagementException;

   NavigationData createNavigation(String ownerType, String ownerId, Integer priority) throws ManagementException;

   NavigationNodeData createNavigationNode(String ownerType, String ownerId, String uri, String label) throws ManagementException;

   void updateNavigation(NavigationData navigation) throws ManagementException;

   void updateNavigationNode(String ownerType, String ownerId, NavigationNodeData node) throws ManagementException;

   void deleteNavigation(String ownerType, String ownerId) throws ManagementException;

   void deleteNavigationNode(String ownerType, String ownerId, String uri) throws ManagementException;
}
