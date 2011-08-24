package org.gatein.management.rest.content;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
@XmlRootElement
@XmlType(propOrder = {"operationName", "operationDescription", "operationLink"})
public class Operation
{
   private String operationName;
   private String operationDescription;
   private Link operationLink;

   public Operation()
   {
   }

   public Operation(String operationName, String operationDescription, Link link)
   {
      this.operationName = operationName;
      this.operationDescription = operationDescription;
      this.operationLink = link;
   }

   @XmlElement (name = "operation-name")
   public String getOperationName()
   {
      return operationName;
   }

   public void setOperationName(String operationName)
   {
      this.operationName = operationName;
   }

   @XmlElement(name = "operation-description")
   public String getOperationDescription()
   {
      return operationDescription;
   }

   public void setOperationDescription(String operationDescription)
   {
      this.operationDescription = operationDescription;
   }

   @XmlElement(name = "link")
   public Link getOperationLink()
   {
      return operationLink;
   }

   public void setOperationLink(Link operationLink)
   {
      this.operationLink = operationLink;
   }
}
