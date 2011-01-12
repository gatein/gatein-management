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

package org.gatein.management.pomdata.client.api;

import org.exoplatform.portal.pom.data.NavigationData;
import org.exoplatform.portal.pom.data.PageData;
import org.exoplatform.portal.pom.data.PortalData;
import org.gatein.management.binding.api.BindingProvider;
import org.gatein.management.binding.core.api.BindingProviderImpl;
import org.gatein.management.domain.PortalArtifacts;
import org.gatein.management.pomdata.client.impl.RestfulPomDataClient;
import org.gatein.management.pomdata.core.api.ExportImportHandlerImpl;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public interface PomDataClient
{
   List<PortalData> getPortalConfig(String ownerType) throws ClientException;

   PortalData getPortalConfig(String ownerType, String ownerId) throws ClientException;

   List<PageData> getPages(String ownerType, String ownerId) throws ClientException;

   PageData getPage(String ownerType, String ownerId, String pageName) throws ClientException;

   PageData createPage(String ownerType, String ownerId, String pageName, String title) throws ClientException;

   void deletePage(String ownerType, String ownerId, String pageName) throws ClientException;

   void updatePage(PageData page) throws ClientException;

   NavigationData getNavigation(String ownerType, String ownerId) throws ClientException;

   NavigationData getNavigation(String ownerType, String ownerId, String navigationPath) throws ClientException;

   void exportAsZip(PortalArtifacts artifacts, File file) throws IOException;

   PortalArtifacts importFromZip(File file) throws IOException;

   public static class Factory
   {
      public static PomDataClient create(InetAddress address, int port, String portalContainerName)
      {
         BindingProvider bindingProvider = new BindingProviderImpl();
         bindingProvider.load();
         return new RestfulPomDataClient(address, port, portalContainerName, bindingProvider, new ExportImportHandlerImpl(bindingProvider));
      }
   }
}
