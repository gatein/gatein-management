package org.gatein.management.rest.content;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
@XmlRootElement(name = "link")
@XmlType(propOrder = {"rel", "href", "type", "method"})
public class Link
{
   private String rel;
   private String href;
   private String type;
   private String method;

   public Link()
   {
   }

   public Link(String rel, String href, String type, String method)
   {
      this.rel = rel;
      this.href = href;
      this.type = type;
      this.method = method;
   }

   @XmlAttribute
   public String getRel()
   {

      return rel;
   }

   public void setRel(String rel)
   {
      this.rel = rel;
   }

   @XmlAttribute
   public String getHref()
   {
      return href;
   }

   public void setHref(String href)
   {
      this.href = href;
   }

   @XmlAttribute
   public String getType()
   {
      return type;
   }

   public void setType(String type)
   {
      this.type = type;
   }

   @XmlAttribute
   public String getMethod()
   {
      return method;
   }

   public void setMethod(String method)
   {
      this.method = method;
   }
}
