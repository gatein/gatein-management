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

package org.gatein.management.portalobjects.common.utils;

import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.pom.data.NavigationData;
import org.exoplatform.portal.pom.data.NavigationNodeData;
import org.exoplatform.portal.pom.data.PageData;
import org.exoplatform.portal.pom.data.PortalData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PortalObjectsUtils
{
   private PortalObjectsUtils(){}

   private static final Set<String> validOwnerTypes;
   static {
      Set<String> set = new HashSet<String>();
      set.add("portal");
      set.add("group");
      set.add("user");
      validOwnerTypes = set;
   }

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

   public static boolean isValidOwnerType(String ownerType)
   {
      return validOwnerTypes.contains(ownerType);
   }

   public static List<PortalConfig> toPortalConfigList(List<PortalData> data)
   {
      if (data == null)
      {
         return null;
      }

      List<PortalConfig> configs = new ArrayList<PortalConfig>(data.size());
      for (PortalData pd : data)
      {
         configs.add(new PortalConfig(pd));
      }

      return configs;
   }

   public static PortalConfig toPortalConfig(PortalData data)
   {
      if (data == null) return null;

      return new PortalConfig(data);
   }

   public static PortalData toPortalData(PortalConfig portalConfig)
   {
      if (portalConfig == null) return null;

      return portalConfig.build();
   }

   public static Page toPage(PageData data)
   {
      if (data == null) return null;

      return new Page(data);
   }

   public static List<Page> toPageList(List<PageData> data)
   {
      if (data == null)
      {
         return null;
      }
      List<Page> pages = new ArrayList<Page>(data.size());
      for (PageData pd : data)
      {
         pages.add(new Page(pd));
      }

      return pages;
   }

   public static PageData toPageData(Page page)
   {
      if (page == null) return null;

      return page.build();
   }

   public static List<PageData> toPageDataList(List<Page> pages)
   {
      if (pages == null) return null;

      List<PageData> data = new ArrayList<PageData>(pages.size());
      for (Page page : pages)
      {
         data.add(page.build());
      }

      return data;
   }

   public static PageNavigation toPageNavigation(NavigationData data)
   {
      if (data == null) return null;

      return new PageNavigation(data);
   }

   public static PageNode toPageNode(NavigationNodeData data)
   {
      if (data == null) return null;

      return new PageNode(data);
   }

   public static NavigationData toNavigationData(PageNavigation navigation)
   {
      if (navigation == null) return null;

      return navigation.build();
   }

   public static NavigationNodeData toNavigationNodeData(PageNode pageNode)
   {
      if (pageNode == null) return null;

      return pageNode.build();
   }

   public static PageNode toPageNode(NavigationData data)
   {
      if (data == null) return null;

      PageNavigation navigation = PortalObjectsUtils.toPageNavigation(data);
      if (navigation.getNodes() != null && navigation.getNodes().size() == 1)
      {
         return navigation.getNodes().get(0);
      }

      return null;
   }

   public static NavigationData toNavigationData(String ownerType, String ownerId, PageNode node)
   {
      if (node == null) return null;

      NavigationNodeData nodeData = PortalObjectsUtils.toNavigationNodeData(node);
      List<NavigationNodeData> nodeDataList = new ArrayList<NavigationNodeData>(1);
      nodeDataList.add(nodeData);

      return new NavigationData(ownerType, ownerId, null, nodeDataList);
   }

   public static PageNode getNode(List<PageNode> nodes, String name)
   {
      for (PageNode node : nodes)
      {
         if (node.getName().equals(name))
         {
            return node;
         }
      }

      return null;
   }

   public static PageNode findNodeByUri(List<PageNode> nodes, String uri)
   {
      if (uri.charAt(0) == '/') uri = uri.substring(1);
      if (uri.charAt(uri.length()-1) == '/') uri = uri.substring(0, uri.length() - 1);

      int index = uri.indexOf('/');
      if (index != -1)
      {
         String childName = uri.substring(0, index);
         String grandChildren = uri.substring(index+1, uri.length());
         PageNode child = getNode(nodes, childName);
         if (child == null) return null;

         return findNodeByUri(child.getNodes(), grandChildren);
      }
      else
      {
         return getNode(nodes, uri);
      }
   }

   public static String getNameForUri(String uri)
   {
      return uri.substring(uri.lastIndexOf('/') + 1, uri.length());
   }

   public static String getParentUri(String uri)
   {
      if (!uri.contains("/"))
      {
         return null;
      }
      return uri.substring(0, uri.lastIndexOf('/'));
   }
}
