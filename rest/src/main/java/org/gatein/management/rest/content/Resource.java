package org.gatein.management.rest.content;

import org.gatein.management.api.operation.OperationNames;
import org.gatein.management.api.operation.model.OperationInfo;
import org.gatein.management.api.operation.model.ReadResourceModel;

import javax.ws.rs.core.MediaType;
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
         for (OperationInfo info : readResource.getOperations())
         {
            Operation operation = new Operation(info.getName(), info.getDescription(), null);
            operations.add(operation);

            Link link;
            if (info.getName().equals(OperationNames.READ_RESOURCE))
            {
               link = new LinkBuilder(uriBuilder).rel("self").build();
            }
            else if (info.getName().equals(OperationNames.READ_CONFIG_AS_XML))
            {
               link = new LinkBuilder(uriBuilder)
                  .rel("content").extension("xml").type(MediaType.APPLICATION_XML).replaceQuery("").build();
            }
            else if (info.getName().equals(OperationNames.EXPORT_RESOURCE))
            {
               link = new LinkBuilder(uriBuilder)
                  .rel("content").extension("zip").type("application/zip").replaceQuery("").method("get").build();
            }
            else if (info.getName().equals(OperationNames.IMPORT_RESOURCE))
            {
               link = new LinkBuilder(uriBuilder)
                  .rel("operation").replaceQuery("").type("application/zip").method("put").build();
            }
            else
            {
               link = new LinkBuilder(uriBuilder)
                  .rel("operation").replaceQueryParam("op", info.getName()).build();
            }
            operation.setOperationLink(link);
         }
         this.operations = new OperationsContainer(operations);
      }

      Set<Child> children = new LinkedHashSet<Child>(readResource.getChildren().size());
      for (String child : readResource.getChildren())
      {
         children.add(new Child(child, new LinkBuilder(uriBuilder).rel("child").path(child).build()));
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
