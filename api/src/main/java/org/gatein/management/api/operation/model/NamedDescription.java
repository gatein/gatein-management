package org.gatein.management.api.operation.model;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class NamedDescription
{
   private String name;
   private String description;

   public NamedDescription(String name, String description)
   {
      this.name = name;
      this.description = description;
   }

   public String getName()
   {
      return name;
   }

   public String getDescription()
   {
      return description;
   }

   void setDescription(String description)
   {
      this.description = description;
   }

   @Override
   public String toString()
   {
      return new StringBuilder().append("NamedDescription{")
         .append("name='").append(name).append("', description='").append(description)
         .append("'}").toString();
   }
}
