/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.gatein.management.cli.crash.arguments;

import org.crsh.cmdline.ParameterDescriptor;
import org.crsh.cmdline.spi.Completer;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class StringCollectionCompleter implements Completer
{
   private final Collection<String> collection;

   public StringCollectionCompleter(Collection<String> collection)
   {
      this.collection = collection;
   }

   @Override
   public Map<String, Boolean> complete(ParameterDescriptor<?> parameter, String prefix) throws Exception
   {
      Map<String, Boolean> completions = Collections.emptyMap();
      for (String value : collection) {
         if (value.startsWith(prefix)) {
            if (completions.isEmpty()) {
               completions = new LinkedHashMap<String, Boolean>();
            }
            completions.put(value.substring(prefix.length()), true);
         }
      }

      return completions;
   }
}
