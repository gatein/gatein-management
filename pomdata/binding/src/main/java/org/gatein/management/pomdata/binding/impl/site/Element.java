package org.gatein.management.pomdata.binding.impl.site;

import org.gatein.staxbuilder.EnumElement;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public enum Element implements EnumElement<Element>
{
   UNKNOWN(null),
   SKIP("skip"),
   PORTAL_CONFIGS("portal-configs"), // To support multiple portal configs (not supported by gatein_objects)
   PORTAL_CONFIG("portal-config"),
   PORTAL_NAME("portal-name"),
   LOCALE("locale"),
   SKIN("skin"),
   PROPERTIES("properties"),
   PROPERTIES_ENTRY("entry"),
   PORTAL_LAYOUT("portal-layout"),
   PAGE_BODY("page-body")
   ;

   private final String name;

   Element(final String name)
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

   private static final Map<String, Element> MAP;

   static
   {
      final Map<String, Element> map = new HashMap<String, Element>();
      for (Element element : values())
      {
         final String name = element.getLocalName();
         if (name != null) map.put(name, element);
      }
      MAP = map;
   }

   public static Element forName(String localName)
   {
      final Element element = MAP.get(localName);
      return element == null ? UNKNOWN : element;
   }
}
