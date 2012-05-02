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

package org.gatein.management.rest.content;

import org.gatein.management.api.operation.OperationNames;
import org.gatein.management.api.operation.model.NamedDescription;
import org.gatein.management.api.operation.model.ReadResourceModel;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
@XmlRootElement(name = "resource")
@XmlType(propOrder = {"description", "children", "operations"})
public class Resource
{
   private String description;
   @XmlElement
   private ChildrenContainer children;
   @XmlElement
   private OperationsContainer operations;


   public Resource()
   {
   }

   public Resource(UriInfo uriInfo, ReadResourceModel readResource)
   {
      this.description = readResource.getDescription();
      UriBuilder uriBuilder = uriInfo.getRequestUriBuilder();
      if (readResource.getOperations() != null)
      {
         List<Operation> operations = new ArrayList<Operation>(readResource.getOperations().size());
         for (NamedDescription operationDescribed : readResource.getOperations())
         {
            Operation operation = new Operation(operationDescribed.getName(), operationDescribed.getDescription(), null);
            operations.add(operation);

            Link link;
            if (operationDescribed.getName().equals(OperationNames.READ_RESOURCE))
            {
               link = new LinkBuilder(uriBuilder).rel("self").build();
            }
            else if (operationDescribed.getName().equals(OperationNames.READ_CONFIG))
            {
               link = new LinkBuilder(uriBuilder).rel("content").replaceQuery("").build();
            }
            else if (operationDescribed.getName().equals(OperationNames.EXPORT_RESOURCE))
            {
               link = new LinkBuilder(uriBuilder)
                  .rel("content").extension("zip").type("application/zip").replaceQuery("").method("get").build();
            }
            else if (operationDescribed.getName().equals(OperationNames.IMPORT_RESOURCE))
            {
               link = new LinkBuilder(uriBuilder)
                  .rel("operation").replaceQuery("").type("application/zip").method("put").build();
            }
            else
            {
               link = new LinkBuilder(uriBuilder)
                  .rel("operation").replaceQueryParam("op", operationDescribed.getName()).build();
            }
            operation.setOperationLink(link);
         }
         this.operations = new OperationsContainer(operations);
      }

      Set<Child> children = new LinkedHashSet<Child>(readResource.getChildDescriptions().size());
      for (NamedDescription childDescribed : readResource.getChildDescriptions())
      {
         Link link = new LinkBuilder(uriBuilder).rel("child").path(childDescribed.getName()).build();
         children.add(new Child(childDescribed.getName(), childDescribed.getDescription(), link));
      }

      this.children = new ChildrenContainer(children);
   }

   @XmlElement
   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   @XmlTransient
   public Set<Child> getChildren()
   {
      return children.children;
   }

   public void setChildren(Set<Child> children)
   {
      this.children = new ChildrenContainer(children);
   }

   @XmlTransient
   public List<Operation> getOperations()
   {
      return operations.operations;
   }

   public void setOperations(List<Operation> operations)
   {
      this.operations = new OperationsContainer(operations);
   }

   private static class ChildrenContainer
   {
      @XmlElement(name = "child")
      private Set<Child> children;

      private ChildrenContainer()
      {
      }

      private ChildrenContainer(Set<Child> children)
      {
         this.children = children;
      }
   }

   private static class OperationsContainer
   {
      @XmlElement(name = "operation")
      private List<Operation> operations;

      private OperationsContainer(){}

      private OperationsContainer(List<Operation> operations)
      {
         this.operations = operations;
      }
   }
}
