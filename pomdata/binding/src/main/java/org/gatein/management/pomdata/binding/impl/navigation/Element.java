package org.gatein.management.pomdata.binding.impl.navigation;

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
   NODE_NAVIGATION("node-navigation"),
   PRIORITY("priority"),
   PAGE_NODES("page-nodes"),
   NODE("node"),
   NAME("name"),
   URI("uri"),
   LABEL("label"),
   ICON("icon"),
   START_PUBLICATION_DATE("start-publication-date"),
   END_PUBLICATION_DATE("end-publication-date"),
   VISIBILITY("visibility"),
   PAGE_REFERENCE("page-reference"),
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
