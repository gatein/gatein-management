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

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.portalobjects.cli.Utils;
import org.gatein.management.portalobjects.client.api.ClientException;
import org.gatein.management.portalobjects.client.api.PortalObjectsMgmtClient;
import org.gatein.management.portalobjects.exportimport.api.ImportContext;
import org.kohsuke.args4j.Option;

import javax.transaction.SystemException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class Importer
{
   private static final Logger log = LoggerFactory.getLogger(Importer.class);

   @Option(name = "--config", aliases = "-c", usage = "Sets custom configuration file to be used for import.", metaVar = " ")
   File configFile;

   @Option(name = "--log4j", aliases = "-l", usage = "Sets custom log4j config file to be used for logging.", metaVar = " ")
   File log4jFile;

   @Option(name = "--loglevel", usage = "Sets log level of root log4j logger (ie debug, info).", metaVar = " ")
   String logLevel;

   @Option(name = "--username", aliases = "-u", usage = "Username to connect to portal with.", metaVar = " ")
   String username;

   @Option(name = "--password", aliases = "-p", usage = "Password to connect to portal with.", metaVar = " ")
   String password;

   @Option(name = "--host", aliases = "-h", usage = "Host of the server the portal is running on.", metaVar = " ")
   String host;

   @Option(name = "--port", usage = "Port of the server the portal is running on.", metaVar = " ")
   int port;

   @Option(name = "--portalcontainer", aliases = "-pc", usage = "Portal container name (ie portal).", metaVar = " ")
   String portalContainer;

   @Option(name = "--importfile", aliases = "-file", usage = "The import file to be imported.", metaVar = " ")
   File importFile;

   @Option(name = "--overwrite", aliases = "-o", usage = "Indicates that the contents of each file should overwrite everything on the destination server. This also means that anything not included will be deleted.", metaVar = " ")
   String overwrite;

   @Option(name = "--force", aliases = "-f", usage = "Force all options without confirmation.", metaVar = " ")
   boolean force;

   @Option(name = "--help")
   boolean help;

   private PortalObjectsMgmtClient client;
   int level;

   public void doImport()
   {
      if (portalContainer == null)
      {
         portalContainer = Utils.getUserInput("Container name (ie portal)", level);
      }

      try
      {
         client = PortalObjectsMgmtClient.Factory.create(InetAddress.getByName(host), port, username, password, portalContainer);
      }
      catch (UnknownHostException e)
      {
         System.err.println("Unknown host name " + host);
         e.printStackTrace(System.err);
         System.exit(1);
      }

      if (importFile == null)
      {
         String file = Utils.getUserInput("Import file", level);
         importFile = new File(file);
      }
      if (!importFile.exists())
      {
         System.err.println("Cannot find file " + importFile);
         System.exit(1);
      }

      Utils.initializeLogging(log4jFile, logLevel, importFile.getParentFile(), importFile.getName(), "import");

      if (overwrite == null)
      {
         overwrite = Utils.getUserInput("Do you wish to fully overwrite all data defined in import file (N) ? Y/N", level);
         if ("Y".equalsIgnoreCase(overwrite))
         {
            overwrite = "true";
         }
      }

      boolean ow = Boolean.valueOf(overwrite);
      if (ow && !force)
      {
         System.out.println("\nOverwrite set to true. This means that all data for a site will be overwritten and any data not defined will be deleted.");
         String proceed = Utils.getUserInput("Do you wish to proceed (N) ? Y/N", level);
         if (!"Y".equalsIgnoreCase(proceed))
         {
            System.exit(0);
         }
      }
      
      try
      {
         ImportContext context = client.importFromZip(importFile);
         if (isEmptyContext(context))
         {
            System.out.println("Nothing to import. " + importFile + " did not contain anything to import.");
            System.exit(0);
         }

         context.setOverwrite(ow);
         client.importContext(context);
      }
      catch (IOException e)
      {
         System.err.println("Exception reading zip file. See log for more details.");
         log.error("IOException reading zip file " + importFile, e);
      }
      catch (ClientException e)
      {
         System.err.println("Client exception during import. See log for more details.");
         log.error("ClientException during import.", e);
      }
      catch (Throwable t)
      {
         System.err.println("Unknown exception occurred during import.  See log for more details.");
         log.error("Uknown exception during import.", t);
      }
   }

   private boolean isEmptyContext(ImportContext context)
   {
      return (context.getPortalConfigs().isEmpty() && context.getPages().isEmpty() && context.getNavigations().isEmpty());
   }
}
