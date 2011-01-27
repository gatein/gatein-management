/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.gatein.management.portalobjects.cli.importer;

import org.gatein.management.portalobjects.cli.Main;
import org.gatein.management.portalobjects.cli.Utils;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class ImportMain
{
   private static final String DEFAULT_CONFIG = "import.properties";

   private static final String IMPORTER_SPLASH =
      "-------------------------------------------------------------\n" +
      "*         Module:   Export/Import (XI) Utility              *\n" +
      "*         Program:  Importer                                *\n" +
      "*         Version:  1.0                                     *\n" +
      "* --------------------------------------------------------- *\n" +
      "*               For help run with --help                    *\n" +
      "-------------------------------------------------------------";

   public static void main(String...args) throws Exception
   {
      System.out.println(IMPORTER_SPLASH);

      // Load default properties
      Properties properties = new Properties();
      properties.load(Main.class.getResourceAsStream(DEFAULT_CONFIG));

      // Create the importer
      Importer importer = new Importer();

      // Parse command line options
      CmdLineParser parser = new CmdLineParser(importer);
      parser.parseArgument(args);

      File configFile = importer.configFile;
      if (configFile != null)
      {
         if (!configFile.exists()) throw new FileNotFoundException(configFile.getAbsolutePath());

         FileInputStream fis = new FileInputStream(configFile);
         try
         {
            // override any properties defined in default export.properties
            properties.load(fis);
         }
         finally
         {
            fis.close();
         }
      }

      // Pass optional configurable properties as args if program args do not include them already
      List<String> argList = new ArrayList<String>();
      argList.addAll(Arrays.asList(args));
      Utils.addPropertiesAsArgs(Importer.class, properties, argList,
         new String[]{"username", "password", "host", "port", "portalContainer", "importFile", "overwrite"});

      args = argList.toArray(new String[argList.size()]);
      parser.parseArgument(args);

      // Print help and exit
      if (importer.help)
      {
         parser.setUsageWidth(125);
         parser.printUsage(System.out);
         System.exit(0);
      }

      // Run importer
      importer.doImport();
   }
}
