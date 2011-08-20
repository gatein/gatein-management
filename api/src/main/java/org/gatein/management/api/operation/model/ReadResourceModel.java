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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
@XmlRootElement(name = "result")
public class ReadResourceModel
{
   @XmlElement
   private String description;
   @XmlElement
   private ChildrenContainer children;
   @XmlElement
   private OperationsContainer operations;

   private ReadResourceModel() {}

   public ReadResourceModel(String description, Set<String> children)
   {
      this(description, children, Collections.<OperationInfo>emptyList());
   }

   public ReadResourceModel(String description, Set<String> children, List<OperationInfo> operations)
   {
      this.description = description;
      this.children = new ChildrenContainer(children);
      this.operations = new OperationsContainer(operations);
   }

   public String getDescription()
   {
      return description;
   }

   public Set<String> getChildren()
   {
      return Collections.unmodifiableSet(children.children);
   }

   public List<OperationInfo> getOperations()
   {
      return Collections.unmodifiableList(operations.operations);
   }

   private static class ChildrenContainer
   {
      @XmlElement(name = "child")
      private Set<String> children;

      private ChildrenContainer()
      {
      }

      private ChildrenContainer(Set<String> children)
      {
         this.children = children;
      }
   }

   private static class OperationsContainer
   {
      @XmlElement(name = "operation")
      private List<OperationInfo> operations;

      private OperationsContainer(){}

      private OperationsContainer(List<OperationInfo> operations)
      {
         this.operations = operations;
      }
   }
}
