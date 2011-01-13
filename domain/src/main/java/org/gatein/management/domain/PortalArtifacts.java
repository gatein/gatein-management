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

package org.gatein.management.domain;

import org.exoplatform.portal.pom.data.NavigationData;
import org.exoplatform.portal.pom.data.PageData;
import org.exoplatform.portal.pom.data.PortalData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PortalArtifacts
{
   Map<String, PortalData> portalDataMap = new HashMap<String, PortalData>();
   Map<String, List<PageData>> pageDataMap = new HashMap<String, List<PageData>>();
   Map<String, NavigationData> navigationDataMap = new HashMap<String, NavigationData>();

   public Iterator<PortalData> getPortalDataIterator()
   {
      return portalDataMap.values().iterator();
   }

   public void addPortalData(PortalData data)
   {
      String key = createKey(data.getType(), data.getName());

      PortalData pd = portalDataMap.get(key);
      if (pd == null)
      {
         portalDataMap.put(key, data);
      }
   }

   public Iterator<List<PageData>> getPageDataIterator()
   {
      return pageDataMap.values().iterator();
   }

   public void addPage(PageData page)
   {
      String key = createKey(page.getOwnerType(), page.getOwnerId());
      List<PageData> list = pageDataMap.get(key);
      if (list == null)
      {
         list = new ArrayList<PageData>();
         pageDataMap.put(key, list);
      }
      list.add(page);
   }

   public void addPages(Collection<PageData> pages)
   {
      for (PageData page : pages)
      {
         addPage(page);
      }
   }

   public Iterator<NavigationData> getNavigationDataIterator()
   {
      return navigationDataMap.values().iterator();
   }

   public void addNavigation(NavigationData navigation)
   {
      String key = createKey(navigation.getOwnerType(), navigation.getOwnerId());
      NavigationData nav = navigationDataMap.get(key);
      if (nav == null)
      {
         navigationDataMap.put(key, navigation);
      }
   }

   private String createKey(String ownerType, String ownerId)
   {
      return new StringBuilder().append(ownerType).append("::").append(ownerId).toString();
   }

   public static class PageDataIterator implements Iterator<List<PageData>>
   {
      private Iterator<List<PageData>> iterator;

      public PageDataIterator(final Map<String,List<PageData>> pageDataMap)
      {
         this.iterator = pageDataMap.values().iterator();
      }

      @Override
      public boolean hasNext()
      {
         return iterator.hasNext();
      }

      @Override
      public List<PageData> next()
      {
         return iterator.next();
      }

      @Override
      public void remove()
      {
         throw new UnsupportedOperationException("Remove not supported for this iterator.");
      }
   }
}
