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
import org.gatein.management.portalobjects.api.exportimport.ExportContext;
import org.gatein.management.portalobjects.api.exportimport.ImportContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PortalObjectsContext implements ExportContext, ImportContext
{
   private Map<String, PortalConfig> portalConfigMap = new LinkedHashMap<String, PortalConfig>();
   private Map<String, List<Page>> pageMap = new LinkedHashMap<String, List<Page>>();
   private Map<String, PageNavigation> navigationMap = new LinkedHashMap<String, PageNavigation>();
   private Map<String, PageNode> navigationNodeMap = new LinkedHashMap<String, PageNode>();

   private List<PortalConfig> portalConfigs;
   private List<List<Page>> pages;
   private List<PageNavigation> navigations;
   private List<PageNode> navigationNodes;

   @Override
   public void addToContext(PortalConfig portalConfig)
   {
      String key = createKey(portalConfig.getType(), portalConfig.getName());

      PortalConfig pc = portalConfigMap.get(key);
      if (pc == null)
      {
         portalConfigMap.put(key, portalConfig);
      }
      portalConfigs = new ArrayList<PortalConfig>(portalConfigMap.values());
   }

   @Override
   public void addToContext(Page page)
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

   @Override
   public void addToContext(List<Page> pages)
   {
      for (Page page : pages)
      {
         addToContext(page);
      }
   }

   @Override
   public void addToContext(PageNavigation navigation)
   {
      String key = createKey(navigation.getOwnerType(), navigation.getOwnerId());
      navigationMap.put(key, navigation);
      navigations = new ArrayList<PageNavigation>(navigationMap.values());
   }

   @Override
   public void addToContext(String ownerType, String ownerId, PageNode node)
   {
      String key = createKey(ownerType, ownerId, node.getUri());
      navigationNodeMap.put(key, node);

      navigationNodes = new ArrayList<PageNode>(navigationNodeMap.values());
   }

   @Override
   public List<PortalConfig> getPortalConfigs()
   {
      return portalConfigs;
   }

   @Override
   public List<List<Page>> getPages()
   {
      return pages;
   }

   @Override
   public List<PageNavigation> getNavigations()
   {
      return navigations;
   }

   @Override
   public List<PageNode> getNavigationNodes()
   {
      return navigationNodes;
   }

   @Override
   public String[] getOwnerInfoForNode(PageNode pageNode)
   {
      for (Map.Entry<String, PageNode> entry : navigationNodeMap.entrySet())
      {
         if (entry.getValue() == pageNode)
         {
            String[] tmp = entry.getKey().split("::");
            String[] ownerInfo = new String[2];
            ownerInfo[0] = tmp[0];
            ownerInfo[1] = tmp[1];
            return ownerInfo;
         }
      }

      return null;
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

   private String createKey(String ownerType, String ownerId, String uri)
   {
      return new StringBuilder().append(ownerType).append("::").append(ownerId).append("::").append(uri).toString();
   }
}
