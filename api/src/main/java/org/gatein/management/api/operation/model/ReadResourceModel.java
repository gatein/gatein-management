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

package org.gatein.management.api.operation.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class ReadResourceModel
{
   private String description;
   private List<NamedDescription> operations;
   private List<NamedDescription> children;
   private boolean childDescriptionsSet;

   /**
    * Result of a successful read-resource operation.
    *
    * @param description description of the result of a read-resource operation.
    * @param childNames the children (if any) of the managed resource.
    *
    */
   public ReadResourceModel(String description, Set<String> childNames)
   {
      if (description == null) throw new IllegalArgumentException("description was null");
      if (childNames == null) throw new IllegalArgumentException("childNames was null");

      this.description = description;
      this.children = new ArrayList<NamedDescription>(childNames.size());
      for (String childName : childNames)
      {
         // Core implementation will add the descriptions after so 'dynamic' extensions don't have to.
         children.add(new NamedDescription(childName, null));
      }
      childDescriptionsSet = false;
   }

   public String getDescription()
   {
      return description;
   }

   public Set<String> getChildren()
   {
      Set<String> childNames = new LinkedHashSet<String>(children.size());
      for (NamedDescription nd : children)
      {
         childNames.add(nd.getName());
      }

      return Collections.unmodifiableSet(childNames);
   }

   public List<NamedDescription> getOperations()
   {
      if (operations == null) return Collections.emptyList();

      return Collections.unmodifiableList(operations);
   }

   public void addOperation(NamedDescription namedDescription)
   {
      if (operations == null) operations = new ArrayList<NamedDescription>();

      operations.add(namedDescription);
   }

   public NamedDescription getChildDescription(String childName)
   {
      return findNamedDescription(childName, children);
   }

   public List<NamedDescription> getChildDescriptions()
   {
      return Collections.unmodifiableList(children);
   }

   public void setChildDescription(String name, String description)
   {
      NamedDescription found = findNamedDescription(name, children);
      if (found != null)
      {
         found.setDescription(description);
         childDescriptionsSet = true;
      }
   }

   public boolean isChildDescriptionsSet()
   {
      return childDescriptionsSet;
   }

   private NamedDescription findNamedDescription(String name, List<NamedDescription> namedDescriptions)
   {
      for (NamedDescription nd : namedDescriptions)
      {
         if (name.equals(nd.getName())) return nd;
      }

      return null;
   }
}
