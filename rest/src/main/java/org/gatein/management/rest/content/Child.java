package org.gatein.management.rest.content;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
@XmlRootElement
@XmlType(propOrder = {"name", "description", "link"})
public class Child
{
   @XmlElement
   private String name;
   @XmlElement
   private String description;
   @XmlElement
   private Link link;

   public Child(){}

   public Child(String name, String description, Link link)
   {
      this.name = name;
      this.description = description;
      this.link = link;
   }

   public String getName()
   {
      return name;
   }

   public String getDescription()
   {
      return description;
   }

   public Link getLink()
   {
      return link;
   }
}
