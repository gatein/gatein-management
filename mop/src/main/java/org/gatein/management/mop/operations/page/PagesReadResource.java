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

import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.operation.model.ReadResourceModel;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.ResultHandler;
import org.gatein.mop.api.workspace.Page;
import org.gatein.mop.api.workspace.Site;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PagesReadResource extends AbstractPageOperationHandler
{
   @Override
   protected void execute(OperationContext operationContext, ResultHandler resultHandler, Site site)  throws ResourceNotFoundException, OperationException
   {
      Collection<Page> pages = getPages(site);
      Set<String> children = new LinkedHashSet<String>(pages.size());
      for (Page page : pages)
      {
         children.add(page.getName());
      }

      resultHandler.completed(new ReadResourceModel("List of pages available for given site.", children));
   }
}
