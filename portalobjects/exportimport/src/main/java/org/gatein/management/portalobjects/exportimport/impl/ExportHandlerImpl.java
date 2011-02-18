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

package org.gatein.management.portalobjects.exportimport.impl;

import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.binding.api.BindingProvider;
import org.gatein.management.portalobjects.common.utils.PortalObjectsUtils;
import org.gatein.management.portalobjects.exportimport.api.ExportContext;
import org.gatein.management.portalobjects.exportimport.api.ExportHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class ExportHandlerImpl implements ExportHandler
{
   private static final Logger log = LoggerFactory.getLogger(ExportHandler.class);

   private BindingProvider bindingProvider;

   public ExportHandlerImpl(BindingProvider bindingProvider)
   {
      this.bindingProvider = bindingProvider;
   }

   @Override
   public ExportContext createExportContext()
   {
      return new PortalObjectsContext();
   }

   @Override
   public void exportContext(ExportContext context, OutputStream out) throws IOException
   {
      if (log.isDebugEnabled())
      {
         logExportContext(context);
      }
      ExportImportUtils.exportAsZip(bindingProvider, context, out);
   }

   private void logExportContext(ExportContext context)
   {
      for (PortalConfig config : context.getPortalConfigs())
      {
         log.debug("Exporting portal config " + PortalObjectsUtils.format(config));
      }
      for (List<Page> pages : context.getPages())
      {
         for (Page page : pages)
         {
            log.debug("Exporting page " + PortalObjectsUtils.format(page));
         }
      }
      for (PageNavigation navigation : context.getNavigations())
      {
         if (navigation.getNodes().isEmpty())
         {
            log.debug("Exporting navigation " + PortalObjectsUtils.format(navigation));
         }
         else
         {
            String ownerType = navigation.getOwnerType();
            String ownerId = navigation.getOwnerId();
            for (PageNode node : navigation.getNodes())
            {
               log.debug("Exporting navigation node " + PortalObjectsUtils.format(ownerType, ownerId, node));
            }
         }
      }
   }
}
