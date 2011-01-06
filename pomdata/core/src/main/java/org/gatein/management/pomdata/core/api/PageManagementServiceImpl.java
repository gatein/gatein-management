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
import org.exoplatform.portal.pom.data.ModelDataStorage;
import org.exoplatform.portal.pom.data.PageData;
import org.exoplatform.portal.pom.data.PageKey;
import org.gatein.management.ManagementException;
import org.gatein.management.pomdata.api.PageManagementService;

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

   public PageManagementServiceImpl(ModelDataStorage modelDataStorage)
   {
      this.modelDataStorage = modelDataStorage;
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
         throw new ManagementException("Could not retrieve all pages for ownerType " + ownerType + " and ownerId " + ownerId, e);
      }
   }

   @Override
   public PageData getPage(String ownerType, String ownerId, String pageName) throws ManagementException
   {
      try
      {
         return modelDataStorage.getPage(new PageKey(ownerType, ownerId, pageName));
      }
      catch (Exception e)
      {
         throw new ManagementException("Could not get page for ownerType " + ownerType + " and ownerId " + ownerId + " and pageName " + pageName, e);
      }
   }
}
