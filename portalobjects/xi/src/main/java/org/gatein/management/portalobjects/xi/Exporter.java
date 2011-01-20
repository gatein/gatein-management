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

package org.gatein.management.portalobjects.xi;

import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.gatein.management.portalobjects.client.api.PortalObjectsMgmtClient;
import org.gatein.management.portalobjects.common.exportimport.PortalObjectsContext;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class Exporter
{
   @Option(name = "--config", aliases = "-c", usage = "Sets configuration file for export", metaVar = " ")
   File configFile;

   @Option(name = "--basedirectory", aliases = "-basedir", usage = "Sets base directory for export", metaVar = " ")
   File basedir;

   @Option(name = "--username", aliases = "-u", usage = "Username to connect to portal with.", metaVar = " ")
   String username;

   @Option(name = "--password", aliases = "-p", usage = "Password to connect to portal with.", metaVar = " ")
   String password;

   @Option(name = "--host", aliases = "-h", usage = "Host of the server the portal is running on.", metaVar = " ")
   String host;

   @Option(name = "--port", usage = "Port of the server the portal is running on.", metaVar = " ")
   int port;

   @Option(name = "--portalcontainer", aliases = "-pc", usage = "Portal container name (ie portal)", metaVar = " ")
   String portalContainer;

   @Option(name = "--scope", aliases = "-s", usage = "Scope of data (ie portal, group, user). Use * for all scopes.", metaVar = " ")
   String scope;

   @Option(name = "--ownerid", aliases = "-o", usage = "Owner id (ie classic, /platform/administrators, root). Use * for all ownerId's.", metaVar = " ")
   String ownerId;

   @Option(name = "--datatype", aliases = "-d", usage = "Data type (ie site, page, navigation). Use * for all data types.", metaVar = " ")
   String dataType;

   @Option(name = "--name", aliases = "-n", usage = "Name of page name or navigation path or * for all pages/navigations.", metaVar = " ")
   String itemName;

   @Option(name = "--help")
   boolean help;

   private PortalObjectsMgmtClient client;
   private File exportDir;
   private int level;
   private PortalObjectsContext context = new PortalObjectsContext();

   void init(Properties properties) throws Exception
   {
      Date date = Calendar.getInstance().getTime();
      SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
      SimpleDateFormat sdf2 = new SimpleDateFormat("HH.mm.ss.SSS");
      String exportPath = new StringBuilder().append(sdf1.format(date)).
         append("/").append(sdf2.format(date)).toString();

      if (basedir == null)
      {
         exportDir = new File("epp-exports", exportPath);
      }
      else
      {
         exportDir = new File(basedir, exportPath);
      }
      if (!exportDir.mkdirs())
      {
         throw new RuntimeException("Could not create export directory " + exportDir.getAbsolutePath());
      }

      System.out.println("Using directory " + exportDir.getAbsolutePath() + " for export.");
   }

   public void doExport() throws Exception
   {
      //TODO: Verify containerName is correct (requires remote call)
      if (portalContainer == null)
      {
         portalContainer = getUserInput("Container name (ie portal)");
      }

      //TODO: Pass credentials
      client = PortalObjectsMgmtClient.Factory.create(InetAddress.getByName(host), port, portalContainer);

      // Process scopes for export
      Scope[] scopes = getScopes();
      for (Scope s : scopes)
      {
         processScope(s);
      }

      client.exportAsZip(context, new File(exportDir, "epp-export.zip"));
   }

   private Scope[] getScopes()
   {
      if (scope == null)
      {
         // Ask user for scope value
         scope = getUserInput("Scope (portal, group, user, * for all)");
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
         ownerId = getUserInput(inputString);
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
         dataType = getUserInput("Data Type (site, page, navigation, * for all)");
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
         indent();
         System.out.print("--- Current owner id '" + ownerId + "' ---\n");
         level++;
         DataType[] datatypes = getDataTypes();
         for (DataType datatype : datatypes)
         {
            level++;
            indent();
            System.out.print("--- Current data type '" + datatype + "' ---\n");
            try
            {
               writeData(scope, ownerId, datatype);
            }
            catch (IOException e)
            {
               System.out.println("Exception writing data.");
               e.printStackTrace();
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
            indent();
            if (data != null)
            {
               context.addPortalConfig(data);
               System.out.println("Successfully exported.");
            }
            else
            {
               System.out.println("Nothing to export.");
            }
            break;
         }
         case PAGE:
         {
            String name = (itemName == null) ? getUserInput("Page name ('*' for all)") : itemName;
            if ("*".endsWith(name))
            {
               List<Page> pages = client.getPages(scope.getName(), ownerId);
               indent();
               if (pages != null)
               {
                  context.addPages(pages);
                  System.out.println("Successfully exported.");
               }
               else
               {
                  System.out.println("Nothing to export.");
               }
            }
            else
            {
               Page page = client.getPage(scope.getName(), ownerId, name);
               indent();
               if (page != null)
               {
                  context.addPage(page);
                  System.out.println("Successfully exported.");
               }
               else
               {
                  System.out.println("Nothing to export.");
               }
            }
            break;
         }
         case NAVIGATION:
         {
            String name = (itemName == null) ? getUserInput("Navigation path ('*' for all)") : itemName;
            PageNavigation data;
            if ("*".equals(name))
            {
               data = client.getNavigation(scope.getName(), ownerId);
            }
            else
            {
               PageNode node = client.getNavigationNode(scope.getName(), ownerId, name);
               data = new PageNavigation();
               data.setOwnerType(scope.getName());
               data.setOwnerId(ownerId);
               ArrayList<PageNode> nodes = new ArrayList<PageNode>(1);
               nodes.add(node);
               data.setNodes(nodes);
            }

            indent();
            if (data != null)
            {
               context.addNavigation(data);
               System.out.println("Successfully exported.");
            }
            else
            {
               System.out.println("Nothing to export.");
            }
            break;
         }
         default:
            throw new RuntimeException("Unsupported data type " + dataType);
      }
   }

   private String getUserInput(String label)
   {
      indent();
      System.out.printf("%s: ", label);
      Scanner scanner = new Scanner(System.in);
      return scanner.next();
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

   private void indent()
   {
      for (int i = 0; i < level; i++)
      {
         System.out.print("   ");
      }
   }
}
