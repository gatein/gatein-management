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

package org.gatein.management.portalobjects.exportimport.impl;

import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.pom.data.NavigationData;
import org.exoplatform.portal.pom.data.PageData;
import org.exoplatform.portal.pom.data.PortalData;
import org.gatein.management.binding.api.BindingProvider;
import org.gatein.management.binding.api.Marshaller;
import org.gatein.management.portalobjects.common.utils.PortalObjectsUtils;
import org.gatein.management.portalobjects.exportimport.api.ExportContext;
import org.gatein.management.portalobjects.exportimport.api.ImportContext;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class ExportImportUtils
{
   private static final String PORTAL_FILE = "portal.xml";
   private static final String PAGES_FILE = "pages.xml";
   private static final String NAVIGATION_FILE = "navigation.xml";

   private ExportImportUtils(){}

   public static void exportAsZip(BindingProvider bindingProvider, ExportContext context, OutputStream out) throws IOException
   {
      if (!(out instanceof BufferedOutputStream))
      {
         out = new BufferedOutputStream(out);
      }
      ZipOutputStream zos = new ZipOutputStream(out);

      // Write portal.xml files to zip
      Marshaller<PortalData> portalDataMarshaller = bindingProvider.createContext(PortalData.class).createMarshaller();
      for (PortalConfig pc : context.getPortalConfigs())
      {
         PortalData portalData = pc.build();
         createZipEntry(portalData.getType(), portalData.getName(), PORTAL_FILE, zos);

         portalDataMarshaller.marshal(portalData, zos);
         zos.closeEntry();
      }

      // Write pages.xml files to zip
      Marshaller<PageData> pageMarshaller = bindingProvider.createContext(PageData.class).createMarshaller();
      for (List<Page> pageList : context.getPages())
      {
         List<PageData> pages = new ArrayList<PageData>(pageList.size());
         for (Page p : pageList)
         {
            pages.add(p.build());
         }
         PageData page = pages.get(0);
         createZipEntry(page.getOwnerType(), page.getOwnerId(), PAGES_FILE, zos);

         pageMarshaller.marshalObjects(pages, zos);
         zos.closeEntry();
      }

      // Write navigation.xml files to zip
      Marshaller<NavigationData> navMarshaller = bindingProvider.createContext(NavigationData.class).createMarshaller();
      for (PageNavigation pageNav : context.getNavigations())
      {
         NavigationData navigation = pageNav.build();
         createZipEntry(navigation.getOwnerType(), navigation.getOwnerId(), NAVIGATION_FILE, zos);

         navMarshaller.marshal(navigation, zos);
         zos.closeEntry();
      }

      zos.flush();
      zos.close();
   }

   public static ImportContext importFromZip(BindingProvider bindingProvider, InputStream in) throws IOException
   {
      if (!(in instanceof BufferedInputStream))
      {
         in = new BufferedInputStream(in);
      }
      NonCloseableZipInputStream zis = new NonCloseableZipInputStream(in);
      ZipEntry entry;
      try
      {
         PortalObjectsContext context = new PortalObjectsContext();
         while ( (entry = zis.getNextEntry()) != null)
         {
            String[] parts = parseEntry(entry);
            String ownerType = parts[0];
            String ownerId = parts[1];
            String file = parts[2];

            if (PORTAL_FILE.equals(file))
            {
               PortalData data = bindingProvider.createContext(PortalData.class).createMarshaller().unmarshal(zis);
               ownerId = PortalObjectsUtils.fixOwnerId(ownerType, ownerId);
               if (!data.getName().equals(ownerId))
               {
                  throw new IOException("Corrupt data for portal file " + entry.getName() + ". Name of portal should be " + ownerId);
               }
               data = PortalObjectsUtils.fixOwner(ownerType, ownerId, data);
               context.addToContext(new PortalConfig(data));
            }
            else if (PAGES_FILE.equals(file))
            {
               Collection<PageData> pages = bindingProvider.createContext(PageData.class).createMarshaller().unmarshalObjects(zis);
               pages = PortalObjectsUtils.fixOwner(ownerType, ownerId, pages);
               for (PageData page : pages)
               {
                  context.addToContext(new Page(page));
               }
            }
            else if (NAVIGATION_FILE.equals(file))
            {
               NavigationData navigation = bindingProvider.createContext(NavigationData.class).createMarshaller().unmarshal(zis);
               navigation = PortalObjectsUtils.fixOwner(ownerType, ownerId, navigation);
               context.addToContext(new PageNavigation(navigation));
            }
            else
            {
               throw new IOException("Unknown entry encountered in zip file: " + entry.getName());
            }

            zis.closeEntry();
         }
         return context;
      }
      finally
      {
         zis.reallyClose();
      }
   }

   private static void createZipEntry(String ownerType, String ownerId, String file, ZipOutputStream zos) throws IOException
   {
      StringBuilder path = new StringBuilder().append(ownerType);
      if (!ownerId.startsWith("/")) path.append("/");
      path.append(ownerId).append("/").append(file);

      zos.putNextEntry(new ZipEntry(path.toString()));
   }

   private static String[] parseEntry(ZipEntry entry) throws IOException
   {
      String name = entry.getName();
      if (name.endsWith(PORTAL_FILE) || name.endsWith(PAGES_FILE) || name.endsWith(NAVIGATION_FILE))
      {
         String[] parts = new String[3];
         parts[0] = name.substring(0, name.indexOf("/"));
         parts[1] = name.substring(parts[0].length() + 1, name.lastIndexOf("/"));
         parts[2] = name.substring(name.lastIndexOf("/") + 1);
         return parts;
      }
      else
      {
         throw new IOException("Unknown entry " + name + " in zip file.");
      }
   }

   // Bug in SUN's JDK XMLStreamReader implementation closes the underlying stream when
   // it finishes reading an XML document. This is no good when we are using a ZipInputStream.
   // See http://bugs.sun.com/view_bug.do?bug_id=6539065 for more information.
   private static class NonCloseableZipInputStream extends ZipInputStream
   {
      private NonCloseableZipInputStream(InputStream in)
      {
         super(in);
      }

      @Override
      public void close() throws IOException
      {
      }

      private void reallyClose() throws IOException
      {
         super.close();
      }
   }
}
