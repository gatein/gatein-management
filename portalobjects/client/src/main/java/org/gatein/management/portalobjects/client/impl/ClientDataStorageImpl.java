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

package org.gatein.management.portalobjects.client.impl;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.ListAccessImpl;
import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.Dashboard;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.pom.data.ModelChange;
import org.exoplatform.portal.pom.data.NavigationKey;
import org.exoplatform.portal.pom.data.PageKey;
import org.gatein.management.portalobjects.client.api.PortalObjectsMgmtClient;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class ClientDataStorageImpl implements DataStorage
{
   private PortalObjectsMgmtClient client;

   public ClientDataStorageImpl(PortalObjectsMgmtClient client)
   {
      this.client = client;
   }

   @Override
   public void create(PortalConfig config) throws Exception
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void save(PortalConfig config) throws Exception
   {
      client.updatePortalConfig(config);
   }

   @Override
   public PortalConfig getPortalConfig(String portalName) throws Exception
   {
      return getPortalConfig(PortalConfig.PORTAL_TYPE, portalName);
   }

   @Override
   public PortalConfig getPortalConfig(String ownerType, String portalName) throws Exception
   {
      return client.getPortalConfig(ownerType, portalName);
   }

   @Override
   public void remove(PortalConfig config) throws Exception
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public Page getPage(String pageId) throws Exception
   {
      PageKey key = PageKey.create(pageId);
      return client.getPage(key.getType(), key.getId(), key.getName());
   }

   @Override
   public Page clonePage(String pageId, String clonedOwnerType, String clonedOwnerId, String clonedName) throws Exception
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void remove(Page page) throws Exception
   {
      client.deletePage(page.getOwnerType(), page.getOwnerId(), page.getName());
   }

   @Override
   public void create(Page page) throws Exception
   {
      client.createPage(page.getOwnerType(), page.getOwnerId(), page.getName(), page.getTitle());
      client.updatePage(page);
   }

   @Override
   public List<ModelChange> save(Page page) throws Exception
   {
      client.updatePage(page);
      return null;
   }

   @Override
   public PageNavigation getPageNavigation(String fullId) throws Exception
   {
      NavigationKey key = NavigationKey.create(fullId);
      return getPageNavigation(key.getType(), key.getId());
   }

   @Override
   public PageNavigation getPageNavigation(String ownerType, String id) throws Exception
   {
      return client.getNavigation(ownerType, id);
   }

   @Override
   public void save(PageNavigation navigation) throws Exception
   {
      client.updateNavigation(navigation);
   }

   @Override
   public void create(PageNavigation navigation) throws Exception
   {
      client.createNavigation(navigation.getOwnerType(), navigation.getOwnerId(), navigation.getPriority());
      client.updateNavigation(navigation);
   }

   @Override
   public void remove(PageNavigation navigation) throws Exception
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void save(PortletPreferences portletPreferences) throws Exception
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public <S> String getId(ApplicationState<S> state) throws Exception
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public <S> S load(ApplicationState<S> state, ApplicationType<S> type) throws Exception
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public <S> ApplicationState<S> save(ApplicationState<S> state, S preferences) throws Exception
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public PortletPreferences getPortletPreferences(String windowID) throws Exception
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public <T> LazyPageList<T> find(final Query<T> q) throws Exception
   {
      if (q.getClassType() == Page.class)
      {
         @SuppressWarnings("unchecked")
         List<T> pages = (List<T>) client.getPages(q.getOwnerType(), q.getOwnerId());
         return new LazyPageList<T>(new ListAccessImpl<T>(q.getClassType(), pages), 10);
      }
      else if (q.getClassType() == PortalConfig.class)
      {
         @SuppressWarnings("unchecked")
         List<T> portalConfigs = (List<T>) client.getPortalConfig(q.getOwnerType());
         return new LazyPageList<T>(new ListAccessImpl<T>(q.getClassType(), portalConfigs), 10);
      }

      throw new UnsupportedOperationException("Unsupported query class type " + q.getClassType());
   }

   @Override
   public <T> LazyPageList<T> find(Query<T> q, Comparator<T> sortComparator) throws Exception
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public <T> ListAccess<T> find2(Query<T> q) throws Exception
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public <T> ListAccess<T> find2(Query<T> q, Comparator<T> sortComparator) throws Exception
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public Container getSharedLayout() throws Exception
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public Dashboard loadDashboard(String dashboardId) throws Exception
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void saveDashboard(Dashboard dashboard) throws Exception
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public List<String> getAllPortalNames() throws Exception
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public <A> A adapt(ModelObject modelObject, Class<A> type)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public <A> A adapt(ModelObject modelObject, Class<A> type, boolean create)
   {
      throw new UnsupportedOperationException();
   }
}
