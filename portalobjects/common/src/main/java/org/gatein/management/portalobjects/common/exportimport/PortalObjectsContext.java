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

package org.gatein.management.portalobjects.common.exportimport;

import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PortalObjectsContext
{
   private Map<String, PortalConfig> portalConfigMap = new LinkedHashMap<String, PortalConfig>();
   private Map<String, List<Page>> pageMap = new LinkedHashMap<String, List<Page>>();
   private Map<String, PageNavigation> navigationMap = new LinkedHashMap<String, PageNavigation>();

   private List<PortalConfig> portalConfigs;
   private List<List<Page>> pages;
   private List<PageNavigation> navigations;

   public void addPortalConfig(PortalConfig portalConfig)
   {
      String key = createKey(portalConfig.getType(), portalConfig.getName());

      PortalConfig pc = portalConfigMap.get(key);
      if (pc == null)
      {
         portalConfigMap.put(key, portalConfig);
      }
      portalConfigs = new ArrayList<PortalConfig>(portalConfigMap.values());
   }

   public void addPage(Page page)
   {
      String key = createKey(page.getOwnerType(), page.getOwnerId());
      List<Page> pages = pageMap.get(key);
      if (pages == null)
      {
         pages = new ArrayList<Page>();
         pageMap.put(key, pages);
      }

      Page existing = findPage(pages, page.getName());
      if (existing == null)
      {
         pages.add(existing);
      }
      else
      {
         int index = pages.indexOf(existing);
         pages.set(index, page);
      }
      this.pages = new ArrayList<List<Page>>(pageMap.values());
   }

   public void addPages(List<Page> pages)
   {
      for (Page page : pages)
      {
         addPage(page);
      }
   }

   public void addNavigation(PageNavigation navigation)
   {
      String key = createKey(navigation.getOwnerType(), navigation.getOwnerId());
      PageNavigation nav = navigationMap.get(key);
      if (nav == null)
      {
         navigationMap.put(key, navigation);
      }
      else if (navigation.getNodes() != null)
      {
         for (PageNode node : navigation.getNodes())
         {
            _addNavigationNode(navigation, node);
         }
      }
      navigations = new ArrayList<PageNavigation>(navigationMap.values());
   }

   public void addNavigationNode(String ownerType, String ownerId, PageNode node)
   {
      String key = createKey(ownerType, ownerId);
      PageNavigation nav = navigationMap.get(key);
      if (nav == null)
      {
         PageNavigation pageNavigation = new PageNavigation();
         pageNavigation.setOwnerType(ownerType);
         pageNavigation.setOwnerId(ownerId);
         ArrayList<PageNode> nodes = new ArrayList<PageNode>();
         nodes.add(node);
         pageNavigation.setNodes(nodes);
         navigationMap.put(key, pageNavigation);
      }
      else
      {
         _addNavigationNode(nav, node);
      }

      navigations = new ArrayList<PageNavigation>(navigationMap.values());
   }

   public List<PortalConfig> getPortalConfigs()
   {
      return portalConfigs;
   }

   public List<List<Page>> getPages()
   {
      return pages;
   }

   public List<PageNavigation> getNavigations()
   {
      return navigations;
   }

   private void _addNavigationNode(PageNavigation navigation, PageNode node)
   {
      PageNode existing = navigation.getNode(node.getName());
      if (existing == null)
      {
         navigation.addNode(node);
      }
      else
      {
         List<PageNode> nodes = navigation.getNodes();
         int index = nodes.indexOf(existing);
         nodes.set(index, node);
      }
   }

   private Page findPage(List<Page> pages, String name)
   {
      for (Page page : pages)
      {
         if (page.getName().equals(name))
         {
            return page;
         }
      }

      return null;
   }

   private String createKey(String ownerType, String ownerId)
   {
      return new StringBuilder().append(ownerType).append("::").append(ownerId).toString();
   }
}
