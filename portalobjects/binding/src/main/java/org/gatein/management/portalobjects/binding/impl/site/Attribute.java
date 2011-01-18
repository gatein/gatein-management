package org.gatein.management.portalobjects.binding.impl.site;

import org.gatein.staxbuilder.EnumAttribute;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public enum Attribute implements EnumAttribute<Attribute>
{
   UNKNOWN(null),
   PROPERTIES_KEY("key");

   private final String name;

   Attribute(final String name)
   {
      this.name = name;
   }

   /**
    * Get the local name of this element.
    *
    * @return the local name
    */
   public String getLocalName()
   {
      return name;
   }

   private static final Map<String, Attribute> MAP;

   static
   {
      final Map<String, Attribute> map = new HashMap<String, Attribute>();
      for (Attribute attribute : values())
      {
         final String name = attribute.getLocalName();
         if (name != null) map.put(name, attribute);
      }
      MAP = map;
   }

   public static Attribute forName(String localName)
   {
      final Attribute attribute = MAP.get(localName);
      return attribute == null ? UNKNOWN : attribute;
   }
}
