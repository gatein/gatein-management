/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.gatein.management.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Available content types for management operations.
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public enum ContentType
{
   JSON("json"),
   XML("xml"),
   ZIP("zip");

   private String name;

   ContentType(String name)
   {
      this.name = name;
   }

   private static final Map<String, ContentType> MAP;

   static
   {
      Map<String, ContentType> tmp = new HashMap<String, ContentType>(3);
      for (ContentType strategy : ContentType.values())
      {
         tmp.put(strategy.name, strategy);
      }

      MAP = tmp;
   }

   /**
    *
    * @return name of the content type.
    */
   public String getName()
   {
      return name;
   }

   /**
    * @param name name of the content type
    * @return content type based on the name
    */
   public static ContentType forName(String name)
   {
      return MAP.get(name);
   }
}
