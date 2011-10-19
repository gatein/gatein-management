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

package org.gatein.management.cli.crash.arguments;

import org.crsh.cmdline.ParameterDescriptor;
import org.crsh.cmdline.annotations.Man;
import org.crsh.cmdline.annotations.Option;
import org.crsh.cmdline.annotations.Usage;
import org.crsh.cmdline.spi.Completer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
@Retention(RetentionPolicy.RUNTIME)
@Option(names = {"m", "importMode"}, completer = ImportModeOption.ImportModeCompleter.class)
@Usage("The import mode for an import operation")
@Man("The import mode for an import operation. Valid values are: conserve, insert, merge, and overwrite.")
public @interface ImportModeOption
{
   public static class ImportModeCompleter implements Completer
   {
      public static final Set<String> modes = new HashSet<String>(Arrays.asList("conserve", "insert", "merge", "overwrite"));

      @Override
      public Map<String, Boolean> complete(ParameterDescriptor<?> parameter, String prefix) throws Exception
      {
         Map<String, Boolean> completions = new HashMap<String, Boolean>(modes.size());
         for (String mode : modes)
         {
            if ("".equals(prefix))
            {
               completions.put(mode, true);
            }
            else if (mode.startsWith(prefix))
            {
               completions.put(mode.substring(prefix.length()), true);
            }
         }

         return completions;
      }
   }
}
