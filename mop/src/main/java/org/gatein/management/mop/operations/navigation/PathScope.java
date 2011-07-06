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

import org.exoplatform.portal.mop.navigation.NodeState;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.navigation.VisitMode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PathScope implements Scope
{
   private String path;
   private String foundId;

   public PathScope(String path)
   {
      if (path == null) throw new IllegalArgumentException("path is null.");
      this.path = path;
   }

   @Override
   public Visitor get()
   {
      final String[] segments = trim(path.split("/"));
      return new Visitor()
      {
         @Override
         public VisitMode enter(int depth, String id, String name, NodeState state)
         {
            if (depth == 0)
            {
               return VisitMode.ALL_CHILDREN;
            }
            assert depth > 0;

            if (depth < segments.length)
            {
               if (name.equals(segments[depth-1]))
               {
                  return VisitMode.ALL_CHILDREN;
               }
               else
               {
                  return VisitMode.NO_CHILDREN;
               }
            }
            else if (depth == segments.length)
            {
               if (name.equals(segments[depth-1]))
               {
                  foundId = id;
                  return VisitMode.ALL_CHILDREN;
               }
               else
               {
                  return VisitMode.NO_CHILDREN;
               }
            }
            else if (depth > segments.length)
            {
               if (foundId != null)
               {
                  return VisitMode.ALL_CHILDREN;
               }
               else
               {
                  return VisitMode.NO_CHILDREN;
               }
            }
            else
            {
               return VisitMode.NO_CHILDREN;
            }
         }

         @Override
         public void leave(int depth, String id, String name, NodeState state)
         {
         }
      };
   }

   String getFoundId()
   {
      return foundId;
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
