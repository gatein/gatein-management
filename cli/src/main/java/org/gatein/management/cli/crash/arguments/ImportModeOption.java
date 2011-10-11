package org.gatein.management.cli.crash.arguments;

import org.crsh.cmdline.ParameterDescriptor;
import org.crsh.cmdline.annotations.Man;
import org.crsh.cmdline.annotations.Option;
import org.crsh.cmdline.annotations.Usage;
import org.crsh.cmdline.spi.Completer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.Map;

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
      private static final String[] modes = new String[] {"conserve", "insert", "merge", "overwrite"};

      @Override
      public Map<String, Boolean> complete(ParameterDescriptor<?> parameter, String prefix) throws Exception
      {
         for (String mode : modes)
         {
            if (mode.startsWith(prefix))
            {
               return Collections.singletonMap(mode.substring(prefix.length()), true);
            }
         }

         return Collections.emptyMap();
      }
   }
}
