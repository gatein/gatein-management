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

package org.gatein.management.mop.operations.page;

import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.portal.pom.data.PageKey;
import org.gatein.management.api.ContentType;
import org.gatein.management.api.binding.BindingProvider;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.ResultHandler;
import org.gatein.management.api.operation.model.ExportResourceModel;
import org.gatein.management.api.operation.model.ExportTask;
import org.gatein.management.mop.exportimport.PageExportTask;
import org.gatein.management.mop.model.PageDataContainer;
import org.gatein.mop.api.workspace.Page;
import org.gatein.mop.api.workspace.Site;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PageExportResource extends AbstractPageOperationHandler
{
   @Override
   protected void execute(OperationContext operationContext, ResultHandler resultHandler, Site site) throws ResourceNotFoundException, OperationException
   {
      String siteType = getSiteType(site.getObjectType());
      String siteName = site.getName();

      POMSessionManager manager = operationContext.getRuntimeContext().getRuntimeComponent(POMSessionManager.class);
      BindingProvider bindingProvider = operationContext.getBindingProvider();

      Collection<Page> pages = getPages(site);
      List<ExportTask> tasks = new ArrayList<ExportTask>(pages.size());
      PageExportTask pageExportTask = new PageExportTask(siteType, siteName, manager.getSession(), bindingProvider.getMarshaller(PageDataContainer.class, ContentType.XML));

      String pageName = operationContext.getAddress().resolvePathTemplate("page-name");
      for (Page page : pages)
      {
         if (pageName == null)
         {
            pageExportTask.addPageName(page.getName());
         }
         else if (pageName.equals(page.getName()))
         {
            pageExportTask.addPageName(page.getName());
         }
      }

      if (pageExportTask.getPageNames().isEmpty() && pageName != null)
      {
         throw new ResourceNotFoundException("No page found for key " + new PageKey(siteType, siteName, pageName));
      }

      tasks.add(pageExportTask);

      resultHandler.completed(new ExportResourceModel(tasks));
   }
}
