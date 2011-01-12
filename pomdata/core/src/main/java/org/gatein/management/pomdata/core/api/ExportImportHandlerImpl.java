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

import org.exoplatform.portal.pom.data.NavigationData;
import org.exoplatform.portal.pom.data.PageData;
import org.exoplatform.portal.pom.data.PortalData;
import org.gatein.management.binding.api.BindingException;
import org.gatein.management.binding.api.BindingProvider;
import org.gatein.management.binding.api.Marshaller;
import org.gatein.management.domain.PortalArtifacts;
import org.gatein.management.pomdata.api.ExportImportHandler;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class ExportImportHandlerImpl implements ExportImportHandler
{
   private BindingProvider bindingProvider;

   public ExportImportHandlerImpl(BindingProvider bindingProvider)
   {
      this.bindingProvider = bindingProvider;
   }

   @Override
   public void exportPortalArtifacts(PortalArtifacts artifacts, OutputStream output) throws IOException
   {
      if (!(output instanceof BufferedOutputStream))
      {
         output = new BufferedOutputStream(output);
      }
      ZipOutputStream zos = new ZipOutputStream(output);

      // Write portal.xml files to zip
      Marshaller<PortalData> portalDataMarshaller = bindingProvider.createContext(PortalData.class).createMarshaller();
      Iterator<PortalData> portalDataIter = artifacts.getPortalDataIterator();
      while (portalDataIter.hasNext())
      {
         PortalData portalData = portalDataIter.next();
         createZipEntry(portalData.getType(), portalData.getName(), "portal.xml", zos);

         portalDataMarshaller.marshal(portalData, zos);
         zos.closeEntry();
      }

      // Write pages.xml files to zip
      Marshaller<PageData> pageMarshaller = bindingProvider.createContext(PageData.class).createMarshaller();
      Iterator<List<PageData>> pageIter = artifacts.getPageDataIterator();
      while (pageIter.hasNext())
      {
         List<PageData> pages = pageIter.next();
         PageData page = pages.get(0);
         createZipEntry(page.getOwnerType(), page.getOwnerId(), "pages.xml", zos);

         pageMarshaller.marshalObjects(pages, zos);
         zos.closeEntry();
      }

      // Write navigation.xml files to zip
      Marshaller<NavigationData> navMarshaller = bindingProvider.createContext(NavigationData.class).createMarshaller();
      Iterator<NavigationData> navIter = artifacts.getNavigationDataIterator();
      while (navIter.hasNext())
      {
         NavigationData navigation = navIter.next();
         createZipEntry(navigation.getOwnerType(), navigation.getOwnerId(), "navigation.xml", zos);

         navMarshaller.marshal(navigation, zos);
         zos.closeEntry();
      }

      zos.flush();
      zos.close();
   }

   @Override
   public PortalArtifacts importPortalArtifacts(InputStream input) throws IOException
   {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
   }

   private void createZipEntry(String ownerType, String ownerId, String file, ZipOutputStream zos) throws IOException
   {
      StringBuilder path = new StringBuilder().append(ownerType);
      if (!ownerId.startsWith("/")) path.append("/");
      path.append(ownerId).append("/").append(file);

      zos.putNextEntry(new ZipEntry(path.toString()));
   }
}
