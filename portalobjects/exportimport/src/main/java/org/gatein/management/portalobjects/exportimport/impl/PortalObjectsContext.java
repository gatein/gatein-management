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

package org.gatein.management.portalobjects.exportimport.impl;

import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.gatein.common.util.ParameterValidation;
import org.gatein.management.portalobjects.exportimport.api.ExportContext;
import org.gatein.management.portalobjects.exportimport.api.ImportContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.gatein.management.portalobjects.common.utils.PortalObjectsUtils.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PortalObjectsContext implements ExportContext, ImportContext
{
   // Portal objects within context
   private Map<String, PortalConfig> portalConfigMap = new LinkedHashMap<String, PortalConfig>();
   private Map<String, List<Page>> pageMap = new LinkedHashMap<String, List<Page>>();
   private Map<String, PageNavigation> navigationMap = new LinkedHashMap<String, PageNavigation>();

   // Information on full overwrites
   private boolean allOverwrite = false;
   private Set<String> pagesOverwrites = new HashSet<String>();
   private Set<String> navigationOverwrites = new HashSet<String>();

   // List for access to portal objects within context
   private List<PortalConfig> portalConfigs = Collections.emptyList();
   private List<List<Page>> pages = Collections.emptyList();
   private List<PageNavigation> navigations = Collections.emptyList();

   @Override
   public void addToContext(PortalConfig portalConfig)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portalConfig, "portalConfig");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(portalConfig.getType(), "type", "portalConfig");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(portalConfig.getName(), "name", "portalConfig");

      String key = createKey(portalConfig.getType(), portalConfig.getName());

      portalConfigMap.put(key, new PortalConfig(portalConfig.build()));
      portalConfigs = new ArrayList<PortalConfig>(portalConfigMap.values());
   }

   @Override
   public void addToContext(Page page)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(page, "page");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(page.getOwnerType(), "ownerType", "page");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(page.getOwnerId(), "ownerId", "page");

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
         pages.add(new Page(page.build()));
      }
      else
      {
         int index = pages.indexOf(existing);
         pages.set(index, new Page(page.build()));
      }
      this.pages = new ArrayList<List<Page>>(pageMap.values());
   }

   @Override
   public void addToContext(List<Page> pages)
   {
      if (pages == null) return;

      for (Page page : pages)
      {
         addToContext(page);
      }
   }

   @Override
   public void addToContext(PageNavigation navigation)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(navigation, "navigation");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(navigation.getOwnerType(), "ownerType", "navigation");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(navigation.getOwnerId(), "ownerId", "navigation");

      String key = createKey(navigation.getOwnerType(), navigation.getOwnerId());
      navigationMap.put(key, new PageNavigation(navigation.build()));
      navigations = new ArrayList<PageNavigation>(navigationMap.values());
   }

   @Override
   public void addToContext(String ownerType, String ownerId, PageNode node)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(node, "node");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(ownerType, "ownerType", null);
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(ownerId, "ownerId", null);

      String key = createKey(ownerType, ownerId);
      PageNavigation nav = navigationMap.get(key);
      if (nav == null)
      {
         PageNavigation pageNavigation = new PageNavigation();
         pageNavigation.setOwnerType(ownerType);
         pageNavigation.setOwnerId(ownerId);
         ArrayList<PageNode> nodes = new ArrayList<PageNode>();
         nodes.add(new PageNode(node.build()));
         pageNavigation.setNodes(nodes);
         navigationMap.put(key, pageNavigation);
      }
      else
      {
         PageNode existing = nav.getNode(node.getName());
         if (existing == null)
         {
            nav.addNode(new PageNode(node.build()));
         }
         else
         {
            List<PageNode> nodes = nav.getNodes();
            int index = nodes.indexOf(existing);
            nodes.set(index, new PageNode(node.build()));
         }
      }

      navigations = new ArrayList<PageNavigation>(navigationMap.values());
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
   public boolean isOverwrite()
   {
      return allOverwrite;
   }

   @Override
   public void setOverwrite(boolean overwrite)
   {
      this.allOverwrite = overwrite;
   }


   @Override
   public boolean isPagesOverwrite(String ownerType, String ownerId)
   {
      return allOverwrite || pagesOverwrites.contains(createKey(ownerType, ownerId));
   }

   @Override
   public void setPagesOverwrite(String ownerType, String ownerId, boolean overwrite)
   {
      if (overwrite)
      {
         pagesOverwrites.add(createKey(ownerType, ownerId));
      }
      else
      {
         pagesOverwrites.remove(createKey(ownerType, ownerId));
      }
   }

   @Override
   public boolean isNavigationOverwrite(String ownerType, String ownerId)
   {
      return allOverwrite || navigationOverwrites.contains(createKey(ownerType, ownerId));
   }

   @Override
   public void setNavigationOverwrite(String ownerType, String ownerId, boolean overwrite)
   {
      if (overwrite)
      {
         navigationOverwrites.add(createKey(ownerType, ownerId));
      }
      else
      {
         navigationOverwrites.remove(createKey(ownerType, ownerId));
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
}
