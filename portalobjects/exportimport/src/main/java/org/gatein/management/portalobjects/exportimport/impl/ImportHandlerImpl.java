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

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.binding.api.BindingProvider;
import org.gatein.management.portalobjects.exportimport.api.ImportContext;
import org.gatein.management.portalobjects.exportimport.api.ImportHandler;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.gatein.management.portalobjects.common.utils.PortalObjectsUtils.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class ImportHandlerImpl implements ImportHandler
{
   private static final Logger log = LoggerFactory.getLogger(ImportHandlerImpl.class);

   private BindingProvider bindingProvider;
   private DataStorage dataStorage;

   public ImportHandlerImpl(BindingProvider bindingProvider, DataStorage dataStorage)
   {
      this.bindingProvider = bindingProvider;
      this.dataStorage = dataStorage;
   }

   @Override
   public ImportContext createContext(InputStream in) throws IOException
   {
      return ExportImportUtils.importFromZip(bindingProvider, in);
   }

   @Override
   public void importContext(ImportContext context) throws Exception
   {
      PortalObjectsContext rollbackContext = new PortalObjectsContext();
      PortalObjectsContext deleteRollbackContext = new PortalObjectsContext();
      try
      {
         importPortalConfig(context, dataStorage, rollbackContext, deleteRollbackContext);
         importPages(context, dataStorage, rollbackContext, deleteRollbackContext);
         importNavigation(context, dataStorage, rollbackContext, deleteRollbackContext);
      }
      catch (Exception e)
      {
         log.error("Exception during import.", e);
         rollback(dataStorage, rollbackContext, deleteRollbackContext);
      }
   }

   private void importPortalConfig(ImportContext context, DataStorage dataStorage,
                                   PortalObjectsContext rollbackContext, PortalObjectsContext deleteRollbackContext)
      throws Exception
   {
      for (PortalConfig pc : context.getPortalConfigs())
      {
         PortalConfig existing = dataStorage.getPortalConfig(pc.getName());
         if (existing == null)
         {
            log.info("Import creating portal config " + format(pc));
            dataStorage.create(pc);
            deleteRollbackContext.addToContext(pc);
         }
         else
         {
            log.info("Import overwriting portal config " + format(pc));
            dataStorage.save(pc);
            rollbackContext.addToContext(existing);
         }
      }
   }

   private void importPages(ImportContext context, DataStorage dataStorage,
                            PortalObjectsContext rollbackContext, PortalObjectsContext deleteRollbackContext)
      throws Exception
   {
      for (List<Page> pages : context.getPages())
      {
         if (pages.isEmpty()) continue;

         // The list of pages we get from the context should
         String ownerType = pages.get(0).getOwnerType();
         String ownerId = pages.get(0).getOwnerId();
         Set<String> pageNames = new HashSet<String>();

         // Update/Create all pages found in import context
         for (Page page : pages)
         {
            pageNames.add(page.getName());

            Page existing = dataStorage.getPage(page.getPageId());
            if (existing == null)
            {
               log.info("Import creating page " + format(page));
               dataStorage.create(page);
               deleteRollbackContext.addToContext(page);
            }
            else
            {
               log.info("Import overwriting page " + format(page));
               dataStorage.save(page);
               rollbackContext.addToContext(existing);
            }
         }
         if (context.isPagesOverwrite(ownerType, ownerId))
         {
            // Delete all pages not found in import context
            Query<Page> query = new Query<Page>(ownerType, ownerId, Page.class);
            LazyPageList<Page> results = dataStorage.find(query);
            List<Page> list = new ArrayList<Page>(results.getAll());
            for (Page page : list)
            {
               if (!pageNames.contains(page.getName()))
               {
                  log.info("Import deleting page " + format(page));
                  dataStorage.remove(page);
                  rollbackContext.addToContext(page);
               }
            }
         }
      }
   }

   private void importNavigation(ImportContext context, DataStorage dataStorage,
                                 PortalObjectsContext rollbackContext, PortalObjectsContext deleteRollbackContext)
      throws Exception
   {
      for (PageNavigation navigation : context.getNavigations())
      {
         PageNavigation existing = dataStorage.getPageNavigation(navigation.getOwnerType(), navigation.getOwnerId());
         if (existing == null)
         {
            log.info("Import creating new navigation " + format(navigation));
            dataStorage.create(navigation);
            deleteRollbackContext.addToContext(navigation);
         }
         else
         {
            if (context.isNavigationOverwrite(navigation.getOwnerType(), navigation.getOwnerId()))
            {
               log.info("Import overwriting entire navigation " + format(navigation));
               dataStorage.save(navigation);
               rollbackContext.addToContext(existing);
            }
            else
            {
               String ownerType = navigation.getOwnerType();
               String ownerId = navigation.getOwnerId();
               ArrayList<PageNode> nodes = navigation.getNodes();
               for (PageNode node : nodes)
               {
                  String parentUri = getParentUri(node.getUri());
                  List<PageNode> siblings;
                  if (parentUri == null)
                  {
                     siblings = existing.getNodes();
                  }
                  else
                  {
                     PageNode parent = findNodeByUri(existing.getNodes(), parentUri);
                     if (parent == null)
                     {
                        throw new Exception("Navigation node " + format(ownerType, ownerId, node) + " has no parent.");
                     }
                     siblings = parent.getNodes();
                  }

                  PageNode found = getNode(siblings, node.getName());
                  if (found == null)
                  {
                     log.info("Import creating navigation node " + format(ownerType, ownerId, node));
                     siblings.add(node);
                  }
                  else
                  {
                     log.info("Import overwriting navigation node " + format(ownerType, ownerId, node));
                     int index = siblings.indexOf(found);
                     siblings.set(index, node);
                  }
               }
               PageNavigation clone = existing.clone();
               dataStorage.save(existing);
               rollbackContext.addToContext(clone);
            }
         }
      }
   }

   private void rollback(DataStorage dataStorage, PortalObjectsContext rollbackContext, PortalObjectsContext deleteRollbackContext)
   {
      log.info("Rolling back any changes that occurred during import.");
      rollbackPortalConfigs(dataStorage, rollbackContext, deleteRollbackContext);
      rollbackPages(dataStorage, rollbackContext, deleteRollbackContext);
      rollbackNavigation(dataStorage, rollbackContext, deleteRollbackContext);
   }

   private void rollbackPortalConfigs(DataStorage dataStorage, PortalObjectsContext rollbackContext, PortalObjectsContext deleteRollbackContext)
   {
      // Rollback overwrites
      if (rollbackContext.getPortalConfigs().isEmpty())
      {
         log.info("No portal configs were overwritten during import");
      }
      else
      {
         log.info("Reverting portal configs overwritten during import.");
      }
      for (PortalConfig pc : rollbackContext.getPortalConfigs())
      {
         try
         {
            dataStorage.save(pc);
            log.info("Successfully rolled back (reverted) portal config " + format(pc));
         }
         catch (Exception e)
         {
            log.error("Exception during rollback when trying to revert portal config " + format(pc));
         }
      }

      // Rollback creates
      if (deleteRollbackContext.getPortalConfigs().isEmpty())
      {
         log.info("No portal configs were creating during import");
      }
      else
      {
         log.info("Deleting portal configs created during import.");
      }
      for (PortalConfig pc : deleteRollbackContext.getPortalConfigs())
      {
         try
         {
            dataStorage.remove(pc);
            log.info("Successfully rolled back (deleted) portal config " + format(pc));
         }
         catch (Exception e)
         {
            log.error("Exception during rollback when trying to delete portal config " + format(pc));
         }
      }
   }

   private void rollbackPages(DataStorage dataStorage, PortalObjectsContext rollbackContext, PortalObjectsContext deleteRollbackContext)
   {
      // Rollback overwrites
      if (rollbackContext.getPages().isEmpty())
      {
         log.info("No pages were overwritten during import");
      }
      else
      {
         log.info("Reverting pages overwritten during import.");
      }
      for (List<Page> pages : rollbackContext.getPages())
      {
         for (Page page : pages)
         {
            try
            {
               dataStorage.save(page);
               log.info("Successfully rolled back (reverted) page " + format(page));
            }
            catch (Exception e)
            {
               log.error("Exception during rollback when trying to revert page " + format(page));
            }
         }
      }

      // Rollback creates
      if (deleteRollbackContext.getPages().isEmpty())
      {
         log.info("No pages were creating during import");
      }
      else
      {
         log.info("Deleting pages created during import.");
      }
      for (List<Page> pages : deleteRollbackContext.getPages())
      {
         for (Page page : pages)
         {
            try
            {
               dataStorage.remove(page);
               log.info("Successfully rolled back (deleted) page " + format(page));
            }
            catch (Exception e)
            {
               log.error("Exception during rollback when trying to delete page " + format(page));
            }
         }
      }
   }

   private void rollbackNavigation(DataStorage dataStorage, PortalObjectsContext rollbackContext, PortalObjectsContext deleteRollbackContext)
   {
      // Rollback overwrites
      if (rollbackContext.getNavigations().isEmpty())
      {
         log.info("No navigations were overwritten during import");
      }
      else
      {
         log.info("Reverting navigations overwritten during import.");
      }
      for (PageNavigation navigation : rollbackContext.getNavigations())
      {
         try
         {
            dataStorage.save(navigation);
            log.info("Successfully rolled back (reverted) navigation " + format(navigation));
         }
         catch (Exception e)
         {
            log.error("Exception during rollback when trying to revert navigation " + format(navigation));
         }
      }

      // Rollback creates
      if (deleteRollbackContext.getNavigations().isEmpty())
      {
         log.info("No navigations were creating during import");
      }
      else
      {
         log.info("Deleting navigations created during import.");
      }
      for (PageNavigation navigation : deleteRollbackContext.getNavigations())
      {
         try
         {
            dataStorage.remove(navigation);
            log.info("Successfully rolled back (deleted) navigation " + format(navigation));
         }
         catch (Exception e)
         {
            log.error("Exception during rollback when trying to delete navigation " + format(navigation));
         }
      }
   }
}
