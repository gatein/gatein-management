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

package org.gatein.management.mop.operations;

import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.portal.pom.data.ModelDataStorage;
import org.exoplatform.portal.pom.data.PortalData;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.api.ContentType;
import org.gatein.management.api.binding.Marshaller;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.operation.OperationAttachment;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.OperationHandler;
import org.gatein.management.api.operation.ResultHandler;
import org.gatein.management.api.operation.model.NoResultModel;
import org.gatein.management.mop.exportimport.ImportStrategy;
import org.gatein.management.mop.exportimport.NavigationExportTask;
import org.gatein.management.mop.exportimport.NavigationImportTask;
import org.gatein.management.mop.exportimport.PageExportTask;
import org.gatein.management.mop.exportimport.PageImportTask;
import org.gatein.management.mop.exportimport.SiteLayoutExportTask;
import org.gatein.management.mop.exportimport.SiteLayoutImportTask;
import org.gatein.management.mop.model.PageDataContainer;
import org.gatein.mop.api.workspace.Workspace;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class MopImportResource implements OperationHandler
{
   private static final Logger log = LoggerFactory.getLogger(MopImportResource.class);

   //TODO: Would like to see the step operations be handled by core.

   //TODO: Clean this up when we have time
   //TODO: See if using RequestLifeCycle begin() and end() around atomic operations solves portal cache issues

   @Override
   public void execute(final OperationContext operationContext, ResultHandler resultHandler) throws ResourceNotFoundException, OperationException
   {
      final String operationName = operationContext.getOperationName();

      OperationAttachment attachment = operationContext.getAttachment(true);
      if (attachment == null) throw new OperationException(operationContext.getOperationName(), "No attachment available for MOP import.");

      InputStream inputStream = attachment.getStream();
      if (inputStream == null) throw new OperationException(operationContext.getOperationName(), "No data stream available for import.");

      POMSessionManager mgr = operationContext.getRuntimeContext().getRuntimeComponent(POMSessionManager.class);
      POMSession session = mgr.getSession();
      if (session == null) throw new OperationException(operationName, "MOP session was null");

      Workspace workspace = session.getWorkspace();
      if (workspace == null) throw new OperationException(operationName, "MOP workspace was null");

      ModelDataStorage dataStorage = operationContext.getRuntimeContext().getRuntimeComponent(ModelDataStorage.class);
      if (dataStorage == null) throw new OperationException(operationName, "DataStorage was null");

      NavigationService navigationService = operationContext.getRuntimeContext().getRuntimeComponent(NavigationService.class);
      if (navigationService == null) throw new OperationException(operationName, "Navigation service was null");

      DescriptionService descriptionService = operationContext.getRuntimeContext().getRuntimeComponent(DescriptionService.class);
      if (descriptionService == null) throw new OperationException(operationName, "Description service was null");

      String strategyAttribute = operationContext.getAttributes().getValue("import-strategy");
      ImportStrategy strategy = ImportStrategy.MERGE;
      if (strategyAttribute != null)
      {
         strategy = ImportStrategy.forName(strategyAttribute);
         if (strategy == null) throw new OperationException(operationName, "Unknown import strategy " + strategyAttribute);
      }

      Map<SiteKey, MopImport> importMap = new HashMap<SiteKey, MopImport>();
      final NonCloseableZipInputStream zis = new NonCloseableZipInputStream(inputStream);
      ZipEntry entry;
      try
      {
         log.info("Preparing data for import.");
         while ( (entry = zis.getNextEntry()) != null)
         {
            // Skip directories
            if (entry.isDirectory()) continue;

            // Parse zip entry
            String[] parts = parseEntry(entry);
            SiteKey siteKey = Utils.siteKey(parts[0], parts[1]);
            String file = parts[2];
            
            MopImport mopImport = importMap.get(siteKey);
            if (mopImport == null)
            {
               mopImport =  new MopImport();
               importMap.put(siteKey, mopImport);
            }

            if (file.equals(SiteLayoutExportTask.FILE))
            {
               // Unmarshal site layout data
               Marshaller<PortalData> marshaller = operationContext.getBindingProvider().getMarshaller(PortalData.class, ContentType.XML);
               PortalData data = marshaller.unmarshal(zis);
               data = Utils.fixOwner(siteKey, data);

               // Add import task to run later
               mopImport.siteTask = new SiteLayoutImportTask(data, siteKey, dataStorage);
            }
            else if (file.equals(PageExportTask.FILE))
            {
               // Unmarshal page data
               Marshaller<PageDataContainer> marshaller = operationContext.getBindingProvider().getMarshaller(PageDataContainer.class, ContentType.XML);
               PageDataContainer data = marshaller.unmarshal(zis);
               data = Utils.fixOwner(siteKey, data);

               // Add import task to run later.
               mopImport.pageTask = new PageImportTask(data, siteKey, dataStorage);
            }
            else if (file.equals(NavigationExportTask.FILE))
            {
               // Unmarshal navigation data
               Marshaller<PageNavigation> marshaller = operationContext.getBindingProvider().getMarshaller(PageNavigation.class, ContentType.XML);
               PageNavigation navigation = marshaller.unmarshal(zis);
               navigation.setOwnerType(siteKey.getTypeName());
               navigation.setOwnerId(siteKey.getName());

               // Add import task to run later
               mopImport.navigationTask = new NavigationImportTask(navigation, siteKey, navigationService, descriptionService, dataStorage);
            }
         }

         resultHandler.completed(NoResultModel.INSTANCE);
      }
      catch (Throwable t)
      {
         throw new OperationException(operationContext.getOperationName(), "Exception reading data for import.", t);
      }
      finally
      {
         try
         {
            zis.reallyClose();
         }
         catch (IOException e)
         {
            log.warn("Exception closing underlying data stream from import.");
         }
      }

      // Perform import
      Map<SiteKey, MopImport> completedImportMap = new HashMap<SiteKey, MopImport>();
      try
      {
         log.info("Performing import using strategy '" + strategy.getName() + "'");
         for (Map.Entry<SiteKey, MopImport> mopImportEntry : importMap.entrySet())
         {
            SiteKey siteKey = mopImportEntry.getKey();
            MopImport mopImport = mopImportEntry.getValue();
            MopImport completed = new MopImport();

            if (completedImportMap.containsKey(siteKey))
            {
               throw new IllegalStateException("Multiple site imports for same operation.");
            }
            completedImportMap.put(siteKey, completed);

            log.debug("Importing data for site " + siteKey);

            // Site layout import
            if (mopImport.siteTask != null)
            {
               log.debug("Importing site layout data.");
               mopImport.siteTask.importData(strategy);
               completed.siteTask = mopImport.siteTask;
            }

            // Page import
            if (mopImport.pageTask != null)
            {
               log.debug("Importing page data.");
               mopImport.pageTask.importData(strategy);
               completed.pageTask = mopImport.pageTask;
            }

            // Navigation import
            if (mopImport.navigationTask != null)
            {
               log.debug("Importing navigation data.");
               mopImport.navigationTask.importData(strategy);
               completed.navigationTask = mopImport.navigationTask;
            }
         }
         log.info("Import successful !");
      }
      catch (Throwable t)
      {
         boolean rollbackSuccess = true;
         log.error("Exception importing data.", t);
         log.info("Attempting to rollback data modified by import.");
         for (Map.Entry<SiteKey, MopImport> mopImportEntry : completedImportMap.entrySet())
         {
            SiteKey siteKey = mopImportEntry.getKey();
            MopImport mopImport = mopImportEntry.getValue();

            log.debug("Rolling back imported data for site " + siteKey);
            if (mopImport.navigationTask != null)
            {
               log.debug("Rolling back navigation modified during import...");
               try
               {
                  mopImport.navigationTask.rollback();
               }
               catch (Throwable t1) // Continue rolling back even though there are exceptions.
               {
                  rollbackSuccess = false;
                  log.error("Error rolling back navigation data.", t1);
               }
            }
            if (mopImport.pageTask != null)
            {
               log.debug("Rolling back pages modified during import...");
               try
               {
                  mopImport.pageTask.rollback();
               }
               catch (Throwable t1) // Continue rolling back even though there are exceptions.
               {
                  rollbackSuccess = false;
                  log.error("Error rolling back page data.", t1);
               }
            }
            if (mopImport.siteTask != null)
            {
               log.debug("Rolling back site layout modified during import...");
               try
               {
                  mopImport.siteTask.rollback();
               }
               catch (Throwable t1) // Continue rolling back even though there are exceptions.
               {
                  rollbackSuccess = false;
                  log.error("Error rolling back site layout.", t1);
               }
            }
         }

         String message = (rollbackSuccess) ?
            "Error during import. Tasks successfully rolled back. Portal should be back to consistent state." :
            "Error during import. Errors in rollback as well. Portal may be in an inconsistent state.";

         throw new OperationException(operationName, message, t);
      }
      finally
      {
         importMap.clear();
         completedImportMap.clear();
      }
   }

   private static String[] parseEntry(ZipEntry entry) throws IOException
   {
      String name = entry.getName();
      if (name.endsWith(SiteLayoutExportTask.FILE) || name.endsWith(PageExportTask.FILE) || name.endsWith(NavigationExportTask.FILE))
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
      private NonCloseableZipInputStream(InputStream inputStream)
      {
         super(inputStream);
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

   private static class MopImport
   {
      private SiteLayoutImportTask siteTask;
      private PageImportTask pageTask;
      private NavigationImportTask navigationTask;
   }
}
