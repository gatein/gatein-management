package org.gatein.management.api.operation.model;

import javax.xml.bind.annotation.XmlType;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
@XmlType
public class OperationInfo
{
   private String name;
   private String description;

   public OperationInfo()
   {
   }

   public OperationInfo(String name, String description)
   {
      this.name = name;
      this.description = description;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   @Override
   public String toString()
   {
      return "OperationDescription{" +
         "name='" + name + '\'' +
         ", description='" + description + '\'' +
         '}';
   }
}
