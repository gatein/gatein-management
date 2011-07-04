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

package org.gatein.management.mop.operations.navigation;

import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.ResultHandler;
import org.gatein.mop.api.workspace.Navigation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class NavigationReadResource extends AbstractNavigationOperationHandler
{
   @Override
   protected void execute(OperationContext operationContext, ResultHandler resultHandler, Navigation defaultNavigation)
   {
      String navUri = operationContext.getAddress().resolvePathTemplate("nav-uri");
      if (navUri == null)
      {
         resultHandler.completed(defaultNavigation);
      }
      else
      {
         String[] uris = trim(navUri.split("/"));
         Navigation nav = null;
         for (String uri : uris)
         {
            nav = defaultNavigation.getChild(uri);
            if (nav == null)
            {
               throw new ResourceNotFoundException("Navigation not found for navigation uri " + navUri);
            }
         }

         resultHandler.completed(nav);
      }
   }

   private String[] trim(String[] array)
   {
      List<String> trimmed = new ArrayList<String>(array.length);
      for (String s : array)
      {
         if (s != null && !"".equals(s))
         {
            trimmed.add(s);
         }
      }

      return trimmed.toArray(new String[trimmed.size()]);
   }
}
