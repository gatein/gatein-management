/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual
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

package org.gatein.management.portalobjects.cli.exporter;

import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.portalobjects.cli.Utils;
import org.gatein.management.portalobjects.client.api.PortalObjectsMgmtClient;
import org.gatein.management.portalobjects.exportimport.api.ExportContext;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class Exporter
{
   private static final Logger log = LoggerFactory.getLogger(Exporter.class);

   @Option(name = "--config", aliases = "-c", usage = "Sets custom configuration file to be used for export.", metaVar = " ")
   File configFile;

   @Option(name = "--log4j", aliases = "-l", usage = "Sets custom log4j config file to be used for logging.", metaVar = " ")
   File log4jFile;

   @Option(name = "--loglevel", usage = "Sets log level of root log4j logger (ie debug, info).", metaVar = " ")
   String logLevel;

   @Option(name = "--basedirectory", aliases = "-basedir", usage = "Sets base directory for export.", metaVar = " ")
   File basedir;

   @Option(name = "--username", aliases = "-u", usage = "Username to connect to portal with.", metaVar = "[default=root]")
   String username;

   @Option(name = "--password", aliases = "-p", usage = "Password to connect to portal with.", metaVar = "[default=gtn]")
   String password;

   @Option(name = "--host", aliases = "-h", usage = "Host of the server the portal is running on.", metaVar = "[default=localhost]")
   String host;

   @Option(name = "--port", usage = "Port of the server the portal is running on.", metaVar = "[default=8080]")
   int port;

   @Option(name = "--portalcontainer", aliases = "-pc", usage = "Portal container name (ie portal).", metaVar = "[default=portal]")
   String portalContainer;

   @Option(name = "--scope", aliases = "-s", usage = "Scope of data (ie portal, group, user). Use * for all scopes.", metaVar = "[default=*]")
   String scope;

   @Option(name = "--ownerid", aliases = "-o", usage = "Owner id (ie classic, /platform/administrators, root). Use * for all ownerId's.", metaVar = "[default=*]")
   String ownerId;

   @Option(name = "--datatype", aliases = "-d", usage = "Data type (ie site, page, navigation). Use * for all data types.", metaVar = "[default=*]")
   String dataType;

   @Option(name = "--name", aliases = "-n", usage = "Name of page name or navigation path or * for all pages/navigations.", metaVar = "[default=*]")
   String itemName;

   @Option(name = "--help")
   boolean help;

   private PortalObjectsMgmtClient client;
   private File exportFile;
   private int level;
   private StringBuilder exportSummary = new StringBuilder();

   private ExportContext context;

   private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
   void init()
   {
      portalContainer = Utils.trimToNull(portalContainer);
      scope = Utils.trimToNull(scope);
      dataType = Utils.trimToNull(dataType);
      itemName = Utils.trimToNull(itemName);

      File exportDir = new File("epp-exports");
      if (basedir != null)
      {
         exportDir = basedir;
      }
      if (!exportDir.exists())
      {
         if (!exportDir.mkdirs())
         {
            throw new RuntimeException("Could not create export directory " + exportDir.getAbsolutePath());
         }
      }

      String fileName = new StringBuilder().append("portal-objects_")
         .append(SDF.format(new Date())).append(".zip").toString();

      exportFile = new File(exportDir, fileName);
      System.out.println("Exporting data to file " + exportFile);
      
      Utils.initializeLogging(log4jFile, logLevel, exportDir, fileName, "export");
      log.info("Exporter successfully initialized and exporting to file " + exportFile);
   }

   public void doExport()
   {
      if (portalContainer == null)
      {
         portalContainer = Utils.getUserInput("Container name (ie portal)", level);
      }

      log.info("Connecting to portal container " + portalContainer + " @ " + host + ":" + port + " and user=" + username);
      try
      {
         client = PortalObjectsMgmtClient.Factory.create(InetAddress.getByName(host), port, username, password, portalContainer);
      }
      catch (UnknownHostException e)
      {
         System.err.println("Unknown host name " + host + ". See log for more details.");
         log.error("Exception retrieving host " + host + " by name.", e);
         System.exit(1);
      }

      context = client.createExportContext();
      // Process scopes for export
      Scope[] scopes = getScopes();
      for (Scope s : scopes)
      {
         processScope(s);
      }

      if (context.getNavigations().isEmpty() && context.getPages().isEmpty() && context.getPortalConfigs().isEmpty())
      {
         System.out.println("No data detected for export.  Nothing to export.");
         log.info("Export complete, however no data was exported.");
         System.exit(1);
      }

      log.info("Export Summary:" + exportSummary.toString());
      try
      {
         log.info("Writing export content to zip file " + exportFile.getAbsolutePath());
         client.exportToZip(context, exportFile);
         log.info("Export successful !");
      }
      catch (IOException e)
      {
         System.err.println("Error exporting content to zip file. See log for more details.");
         log.error("IOException exporting content to zip file " + exportFile.getAbsolutePath(), e);
         System.exit(1);
      }
   }

   private Scope[] getScopes()
   {
      if (scope == null)
      {
         // Ask user for scope value
         scope = Utils.getUserInput("Scope (portal, group, user, * for all)", level);
      }

      // Parse scope value
      if ("*".equals(scope))
      {
         return new Scope[]{Scope.PORTAL, Scope.GROUP, Scope.USER};
      }
      else
      {
         return new Scope[]{Scope.forName(scope)};
      }
   }
   private String[] getOwnerIds(Scope scope, String inputString)
   {
      if (ownerId == null)
      {
         ownerId = Utils.getUserInput(inputString, level);
         if (ownerId.contains(","))
         {
            return ownerId.split(",");
         }
      }
      if ("*".equals(ownerId))
      {
         Collection<String> ownerIds;
         switch (scope)
         {
            case PORTAL:
               ownerIds = getSiteNames(Scope.PORTAL.getName());
               return ownerIds.toArray(new String[ownerIds.size()]);
            case GROUP:
               ownerIds = getSiteNames(Scope.GROUP.getName());
               return ownerIds.toArray(new String[ownerIds.size()]);
            case USER:
               ownerIds = getSiteNames(Scope.USER.getName());
               return ownerIds.toArray(new String[ownerIds.size()]);
            default:
               throw new RuntimeException("Scope " + scope.getName() + " not supported.");
         }
      }
      else
      {
         return new String[]{ownerId};
      }
   }

   private Collection<String> getSiteNames(String ownerType)
   {
      List<PortalConfig> list = client.getPortalConfig(ownerType);
      Collection<String> names = new ArrayList<String>(list.size());
      for (PortalConfig config : list)
      {
         names.add(config.getName());
      }

      return names;
   }


   private DataType[] getDataTypes()
   {
      if (dataType == null)
      {
         // Ask use for data type value
         dataType = Utils.getUserInput("Data Type (site, page, navigation, * for all)", level);
      }

      if ("*".equals(dataType))
      {
         return new DataType[]{DataType.SITE, DataType.PAGE, DataType.NAVIGATION};
      }
      else
      {
         return new DataType[]{DataType.valueOf(dataType.toUpperCase())};
      }
   }

   private void processScope(Scope scope)
   {
      System.out.println("--- Current scope '" + scope + "' ---");
      level++;

      String[] ownerIds;
      switch (scope)
      {
         case PORTAL:
            ownerIds = getOwnerIds(scope, "Owner Id (site name)");
            break;
         case GROUP:
            ownerIds = getOwnerIds(scope, "Owner Id (group name)");
            break;
         case USER:
            ownerIds = getOwnerIds(scope, "Owner Id (user name)");
            break;
         default:
            throw new RuntimeException("Scope " + scope + " not supported.");
      }

      for (String ownerId : ownerIds)
      {
         Utils.indent(level);
         System.out.print("--- Current owner id '" + ownerId + "' ---\n");
         level++;

         exportSummary.append("\n").append(scope.getName()).append(" site ")
            .append("'").append(ownerId).append("' :");

         DataType[] datatypes = getDataTypes();
         for (DataType datatype : datatypes)
         {
            level++;
            Utils.indent(level);
            System.out.print("--- Current data type '" + datatype + "' ---\n");
            try
            {
               writeData(scope, ownerId, datatype);
            }
            catch (IOException e)
            {
               System.err.println("Exception writing data. See log for more details.");
               log.error("Exception writing data for scope " + scope.getName() + " and ownerId " + ownerId + " and datatype " + dataType, e);
               System.exit(1);
            }
            level--;
         }
         level--;
      }

      level--;
   }

   private void writeData(Scope scope, String ownerId, DataType dataType) throws IOException
   {
      switch (dataType)
      {
         case SITE:
         {
            PortalConfig data = client.getPortalConfig(scope.getName(), ownerId);
            Utils.indent(++level);
            if (data != null)
            {
               context.addToContext(data);
               System.out.println("Successfully exported.");

               exportSummary.append("\n   1 site layout exported.");
            }
            else
            {
               System.out.println("Nothing to export.");
            }
            level--;
            break;
         }
         case PAGE:
         {
            String name = (itemName == null) ? Utils.getUserInput("Page name ('*' for all)", level) : itemName;
            if ("*".endsWith(name))
            {
               List<Page> pages = client.getPages(scope.getName(), ownerId);
               Utils.indent(++level);
               if (pages != null)
               {
                  context.addToContext(pages);
                  System.out.println("Successfully exported.");
                  exportSummary.append("\n   All pages exported.");
               }
               else
               {
                  System.out.println("Nothing to export.");
                  exportSummary.append("\n   No pages exported.");
               }
               level--;
            }
            else
            {
               Page page = client.getPage(scope.getName(), ownerId, name);
               Utils.indent(++level);
               if (page != null)
               {
                  context.addToContext(page);
                  System.out.println("Successfully exported.");
                  exportSummary.append("\n").append(name).append(" page exported for ").
                     append(scope.getName()).append(" site ").append(ownerId);
               }
               else
               {
                  System.out.println("Nothing to export.");
                  exportSummary.append("\n   No pages exported.");
               }
               level--;
            }
            break;
         }
         case NAVIGATION:
         {
            String name = (itemName == null) ? Utils.getUserInput("Navigation path ('*' for all)", level) : itemName;
            PageNavigation data = null;
            if ("*".equals(name))
            {
               data = client.getNavigation(scope.getName(), ownerId);
               if (data != null)
               {
                  exportSummary.append("\n   All navigation exported.");
               }
            }
            else
            {
               PageNode node = client.getNavigationNode(scope.getName(), ownerId, name);
               if (node != null)
               {
                  data = new PageNavigation();
                  data.setOwnerType(scope.getName());
                  data.setOwnerId(ownerId);
                  ArrayList<PageNode> nodes = new ArrayList<PageNode>(1);
                  nodes.add(node);
                  data.setNodes(nodes);

                  exportSummary.append("\n").append(name).append(" navigation node exported for ").
                     append(scope.getName()).append(" site ").append(ownerId);
               }
            }

            Utils.indent(++level);
            if (data != null)
            {
               context.addToContext(data);
               System.out.println("Successfully exported.");
            }
            else
            {
               System.out.println("Nothing to export.");
               exportSummary.append("\n   No navigation exported.");
            }
            level--;
            break;
         }
         default:
            throw new RuntimeException("Unsupported data type " + dataType);
      }
   }

   private String getProperty(String propertyName, String defaultValue, Properties properties)
   {
      String value = properties.getProperty(propertyName);
      if (value == null || value.trim().length() == 0) return defaultValue;

      return value;
   }

   private String getRequiredProperty(String propertyName, Properties properties) throws Exception
   {
      String value = getProperty(propertyName, null, properties);
      if (value == null) throw new Exception("Configuration property " + propertyName + " is required.");

      return value;
   }
}
