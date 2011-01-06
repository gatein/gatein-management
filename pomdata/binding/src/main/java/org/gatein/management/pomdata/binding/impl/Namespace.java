package org.gatein.management.pomdata.binding.impl;

import org.gatein.staxbuilder.EnumNamespace;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public enum Namespace implements EnumNamespace<Namespace>
{
   UNKNOWN(null),
   GATEIN_OBJECTS_1_1("http://www.gatein.org/xml/ns/gatein_objects_1_1");

   /**
     * The current namespace version.
     */
    public static final Namespace CURRENT = GATEIN_OBJECTS_1_1;

    private final String name;

    Namespace(final String name) {
        this.name = name;
    }

    /**
     * Get the URI of this namespace.
     *
     * @return the URI
     */
    public String getUri() {
        return name;
    }

    private static final Map<String, Namespace> MAP;

    static {
        final Map<String, Namespace> map = new HashMap<String, Namespace>();
        for (Namespace namespace : values()) {
            final String name = namespace.getUri();
            if (name != null) map.put(name, namespace);
        }
        MAP = map;
    }

    public static Namespace forUri(String uri) {
        final Namespace element = MAP.get(uri);
        return element == null ? UNKNOWN : element;
    }
}
