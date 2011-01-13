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

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.pom.data.ComponentData;
import org.exoplatform.portal.pom.data.ModelDataStorage;
import org.exoplatform.portal.pom.data.PageData;
import org.exoplatform.portal.pom.data.PageKey;
import org.exoplatform.portal.pom.data.PortalData;
import org.gatein.management.DataNotFoundException;
import org.gatein.management.ManagementException;
import org.gatein.management.pomdata.api.PageManagementService;
import org.gatein.management.pomdata.api.SiteManagementService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
//TODO: Add debugging
public class PageManagementServiceImpl implements PageManagementService
{
   private ModelDataStorage modelDataStorage;
   private SiteManagementService siteManagementService;

   public PageManagementServiceImpl(ModelDataStorage modelDataStorage, SiteManagementService siteManagementService)
   {
      this.modelDataStorage = modelDataStorage;
      this.siteManagementService = siteManagementService;
   }

   @Override
   public List<PageData> getPages(String ownerType, String ownerId) throws ManagementException
   {
      Query<PageData> query = new Query<PageData>(ownerType, ownerId, PageData.class);
      try
      {
         LazyPageList<PageData> results = modelDataStorage.find(query);

         List<PageData> list = new ArrayList<PageData>(results.getAll());
         //TODO: Do we want to sort on page name or accept the order of what's returned from data storage ?
//         Collections.sort(list, new Comparator<PageData>()
//         {
//            @Override
//            public int compare(PageData page1, PageData page2)
//            {
//               return page1.getName().compareTo(page2.getName());
//            }
//         });

         return list;
      }
      catch (Exception e)
      {
         throw new ManagementException("Exception getting pages for ownerType=" + ownerType + ", ownerId=" + ownerId, e);
      }
   }

   @Override
   public PageData getPage(String ownerType, String ownerId, String pageName) throws ManagementException
   {
      PageKey pageKey = new PageKey(ownerType, ownerId, pageName);
      try
      {
         return modelDataStorage.getPage(pageKey);
      }
      catch (Exception e)
      {
         throw new ManagementException("Exception getting page " + keyToString(pageKey), e);
      }
   }

   @Override
   public PageData createPage(String ownerType, String ownerId, String pageName, String title) throws ManagementException
   {
      PortalData portalData = siteManagementService.getPortalData(ownerType, ownerId);
      if (portalData == null) throw new DataNotFoundException("Cannot create page.  Site with ownerType=" + ownerType + " and ownerId=" + ownerId + " does not exist.");

      PageData existingPage = getPage(ownerType, ownerId, pageName);
      if (existingPage != null) throw new ManagementException("Cannot create page. " + pageToString(existingPage) + " already exists.");

      PageData page = new PageData(null, null, pageName, null, null, null, title, null, null, null,
         portalData.getAccessPermissions(), new ArrayList<ComponentData>(), ownerType, ownerId, portalData.getEditPermission(), false);
      try
      {
         modelDataStorage.create(page);
         return modelDataStorage.getPage(new PageKey(ownerType, ownerId, pageName));
      }
      catch (Exception e)
      {
         throw new ManagementException("Exception creating " + pageToString(page), e);
      }
   }

   @Override
   public void updatePage(PageData page) throws ManagementException
   {
      PageData existingPage = getPage(page.getOwnerType(), page.getOwnerId(), page.getName());
      if (existingPage == null) throw new DataNotFoundException("Cannot update page. " + pageToString(page) + " does not exist.");

      try
      {
         modelDataStorage.save(page);
      }
      catch (Exception e)
      {
         throw new ManagementException("Exception updating " + pageToString(page), e);
      }
   }

   @Override
   public void deletePage(String ownerType, String ownerId, String pageName) throws ManagementException
   {
      PageData page = getPage(ownerType, ownerId, pageName);
      if (page == null) throw new DataNotFoundException("Cannot delete page. " + keyToString(new PageKey(ownerType, ownerId, pageName)) +
         " does not exist.");

      try
      {
         modelDataStorage.remove(page);
      }
      catch (Exception e)
      {
         throw new ManagementException("Exception deleting " + pageToString(page), e);
      }
   }

   private String keyToString(PageKey pageKey)
   {
      return new StringBuilder().append("Page[ownerType=").append(pageKey.getType()).
         append(", ownerId=").append(pageKey.getId()).
         append(", name=").append(pageKey.getName()).
         append("]").toString();
   }

   private String pageToString(PageData page)
   {
      return keyToString(new PageKey(page.getOwnerType(), page.getOwnerId(), page.getName()));
   }
}
