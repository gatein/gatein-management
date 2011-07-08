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

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.pom.data.ModelDataStorage;
import org.exoplatform.portal.pom.data.PageData;
import org.exoplatform.portal.pom.data.PageKey;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.ResultHandler;
import org.gatein.mop.api.workspace.Page;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PageReadResource extends AbstractPageOperationHandler
{
   @Override
   protected void execute(OperationContext operationContext, ResultHandler resultHandler, Page pages)  throws ResourceNotFoundException, OperationException
   {
      String pageName = operationContext.getAddress().resolvePathTemplate("page-name");

      if (pageName == null)
         throw new OperationException(operationContext.getOperationName(), "No page name specified.");

      ModelDataStorage dataStorage = operationContext.getRuntimeContext().getRuntimeComponent(ModelDataStorage.class);

      SiteKey siteKey = getSiteKey(pages.getSite());

      PageKey key = new PageKey(siteKey.getTypeName(), siteKey.getName(), pageName);
      try
      {
         PageData page = dataStorage.getPage(key);
         if (page == null)
         {
            throw new ResourceNotFoundException("No page found for key " + key);
         }
         else
         {
            resultHandler.completed(page);
         }
      }
      catch (ResourceNotFoundException nfe)
      {
         throw nfe;
      }
      catch (Exception e)
      {
         throw new OperationException(operationContext.getOperationName(), "Operation failed getting page for key " + key, e);
      }
   }
}
