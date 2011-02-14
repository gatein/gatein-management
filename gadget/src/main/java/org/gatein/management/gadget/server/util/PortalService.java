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

package org.gatein.management.gadget.server.util;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.portalobjects.exportimport.api.ExportContext;
import org.gatein.management.portalobjects.exportimport.api.ExportHandler;
import org.gatein.management.portalobjects.exportimport.api.ImportContext;
import org.gatein.management.portalobjects.exportimport.api.ImportHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

/**
 * {@code PortalService}
 * <p/>
 * Created on Jan 5, 2011, 9:14:19 AM
 *
 * @author Nabil Benothman
 * @version 1.0
 */
public final class PortalService
{

   private static final org.gatein.common.logging.Logger log = LoggerFactory.getLogger(PortalService.class);
   private DataStorage dataStorage;
   private ExportHandler exportHandler;
   private ImportHandler importHandler;

   /**
    * Create a new instance of {@code PortalService}
    *
    * @param dataStorage
    * @param exportHandler
    * @param importHandler
    */
   public PortalService(DataStorage dataStorage, ExportHandler exportHandler, ImportHandler importHandler)
   {
      this.dataStorage = dataStorage;
      this.exportHandler = exportHandler;
      this.importHandler = importHandler;
   }

   /**
    * Create a new instance of {@code PortalService}
    *
    * @param container The portal container
    * @return a new instance of {@code PortalService}
    */
   public static PortalService create(ExoContainer container)
   {
      DataStorage dataStorage = (DataStorage) container.getComponentInstanceOfType(DataStorage.class);
      ExportHandler exportHandler = (ExportHandler) container.getComponentInstanceOfType(ExportHandler.class);
      ImportHandler importHandler = (ImportHandler) container.getComponentInstanceOfType(ImportHandler.class);

      return new PortalService(dataStorage, exportHandler, importHandler);
   }

   /**
    * Retrieve the {@code PortalConfig} having the given type
    *
    * @param type The type of {@code PortalConfig} (ownerType)
    * @return a collection of {@code PortalConfig}
    */
   public List<PortalConfig> getPortalConfigs(String type)
   {
      Query<PortalConfig> query = new Query<PortalConfig>(type, null, PortalConfig.class);
      try
      {
         return dataStorage.find(query).getAll();
      }
      catch (Exception e)
      {
         log.error("Exception retrieving sites for type " + type, e);
      }
      return Collections.emptyList();
   }

   /**
    * Retrieve the list of {@code PortalConfig} having the given type and name
    *
    * @param type The portal type (ownerType)
    * @param name The site name (ownerId)
    * @return a list of {@code PortalConfig}
    */
   public PortalConfig getPortalConfig(String type, String name)
   {
      try
      {
         return dataStorage.getPortalConfig(type, name);
      }
      catch (Exception e)
      {
         log.error("Exception retrieving site for type " + type + " and name " + name);
         return null;
      }
   }

   /**
    * Retrieve the list of {@code PortalConfig} given their pages
    *
    * @param pages the list of pages of a portal
    * @return a list of {@code PortalConfig}
    */
   //TODO: Is this method needed ?
//   public List<PortalConfig> getPortalConfigs(List<Page> pages)
//   {
//      Map<String, PortalConfig> pConfigs = new HashMap<String, PortalConfig>();
//      try
//      {
//         PortalConfig pc = null;
//         String key = null;
//         for (Page page : pages)
//         {
//            pc = dataStorage.getPortalConfig(page.getOwnerType(), page.getOwnerId());
//            key = page.getOwnerType() + "::" + page.getOwnerId();
//            if (pConfigs.get(key) == null && pc != null)
//            {
//               pConfigs.put(key, pc);
//            }
//         }
//         return new ArrayList<PortalConfig>(pConfigs.values());
//      }
//      catch (Exception exp)
//      {
//         log.error("Exception retrieving the list of sites.", exp);
//      }
//
//      return Collections.EMPTY_LIST;
//   }

   /**
    * Retrieve pages having the given portal type
    *
    * @param type the portal type (ownerType)
    * @return a collection of {@code Page}
    */
   //TODO: This method should be removed.  You can never retrieve pages without a site
//   public List<Page> getPages(String type)
//   {
//      return getPages(type, null);
//   }

   /**
    * Retrieve pages having the given portal type and site name
    *
    * @param type the portal type (ownerType)
    * @param name the site name (ownerId)
    * @return a collection of {@code Page}
    */
   public List<Page> getPages(String type, String name)
   {
      try
      {
         Query<Page> query = new Query<Page>(type, name, Page.class);
         LazyPageList<Page> results = dataStorage.find(query);
         return results.getAll();
      }
      catch (Exception exp)
      {
         log.error("Exception getting all pages for type: " + type + " and name: " + name, exp);
      }

      return Collections.emptyList();
   }

   /**
    * Retrieve page navigations having the given portal type
    *
    * @param type the portal type (ownerType)
    * @return a collection of {@code PageNavigation}
    */
   //TODO: Cannot retrieve navigation without a site
//   public List<PageNavigation> getPageNavigations(String type)
//   {
//      return getPageNavigations(type, null);
//   }

   /**
    * Retrieve page navigations having the given portal type and site name
    *
    * @param type the portal type (ownerType)
    * @param name the site name (ownerId)
    * @return a collection of {@code PageNavigation}
    */
   //TODO: Only one page navigation is returned per site.
   public PageNavigation getPageNavigation(String type, String name)
   {
      try
      {
         return dataStorage.getPageNavigation(type, name);
      }
      catch (Exception exp)
      {
         log.error("Exception retrieving the list of page navigations for type: " + type + ", name: " + name, exp);
      }

      return null;
   }

   /**
    * Retrieve the site having the given type and name
    *
    * @param type The site type (ownerType)
    * @param name The site name (ownerId)
    * @return the site having the given name and type
    */
   //TODO: This method uses the MOP object model, we should remove this
//   public Site getSiteByName(String type, String name)
//   {
//      try
//      {
//         Query<Site> query = new Query<Site>(type, name, Site.class);
//         LazyPageList<Site> results = dataStorage.find(query);
//         return results.getAll().isEmpty() ? null : results.getAll().get(0);
//      }
//      catch (Exception exp)
//      {
//         log.error("Exception retrieving the list of sites for type " + type, exp);
//      }
//
//      return null;
//   }

   /**
    * Export the site given by it type and name
    *
    * @param type the portal type (ownerType)
    * @param name the site name (ownerId)
    * @param os   the output stream in what the export file will be written
    * @throws IOException      if an exception occurs
    * @throws ProcessException if an exception occurs
    */
   public void exportSite(String type, String name, OutputStream os) throws IOException, ProcessException
   {
      ExportContext context = exportHandler.createExportContext();
      PortalConfig portalConfig = getPortalConfig(type, name);
      if (portalConfig == null)
      {
         throw new ProcessException("No entry with type : " + type + " and name : " + name);
      }
      context.addToContext(portalConfig);

      List<Page> pages = getPages(type, name);
      context.addToContext(pages);

      PageNavigation pageNavigation = getPageNavigation(type, name);
      context.addToContext(pageNavigation);

      // export the site
      this.exportHandler.exportContext(context, os);
   }

   /**
    * Import the site to the portal. The site is given by file opened with the
    * {@code java.io.InputStream}.
    *
    * @param in the input stream pointing to the file containing the data of the
    *           site to import to the portal.
    */
   public void importSite(InputStream in, boolean overwrite) throws Exception
   {
      ImportContext context = importHandler.createContext(in);
      //TODO: Add the ability to overwrite everything
      //context.setOverwrite(true);
      context.setOverwrite(overwrite);
      this.importHandler.importContext(context);
   }

   /**
    * @param query
    * @return
    */
   public List<String> getUsers(String query)
   {
      return Collections.emptyList();
   }
}
