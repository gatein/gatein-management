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

package org.gatein.management.mop;

import org.gatein.management.api.ComponentRegistration;
import org.gatein.management.api.ManagedDescription;
import org.gatein.management.api.ManagedResource;
import org.gatein.management.api.operation.OperationNames;
import org.gatein.management.mop.binding.MopBindingProvider;
import org.gatein.management.mop.operations.MopImportResource;
import org.gatein.management.mop.operations.MopReadResource;
import org.gatein.management.mop.operations.navigation.NavigationExportResource;
import org.gatein.management.mop.operations.navigation.NavigationReadResource;
import org.gatein.management.mop.operations.page.PageExportResource;
import org.gatein.management.mop.operations.page.PageReadResource;
import org.gatein.management.mop.operations.page.PagesReadResource;
import org.gatein.management.mop.operations.site.SiteLayoutExportResource;
import org.gatein.management.mop.operations.site.SiteLayoutReadResource;
import org.gatein.management.mop.operations.site.SiteReadResource;
import org.gatein.management.mop.operations.site.SiteTypeReadResource;
import org.gatein.management.spi.ExtensionContext;
import org.gatein.management.spi.ManagementExtension;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class MopManagementExtension implements ManagementExtension
{
   @Override
   public void initialize(ExtensionContext context)
   {
      ComponentRegistration registration = context.registerManagedComponent("mop");
      registration.registerBindingProvider(MopBindingProvider.INSTANCE);

      ManagedResource.Registration mop = registration.registerManagedResource(description("MOP (Model Object for Portal) Managed Resource"));
      mop.registerOperationHandler("import-resource", new MopImportResource(), description("Imports mop data from an exported zip file."), true);

      mop.registerOperationHandler(OperationNames.READ_RESOURCE, new MopReadResource(), description("Lists the available site types for a portal."));

      ManagedResource.Registration sitetypes = mop.registerSubResource("{site-type}sites", description("Management resource responsible for handling management operations on a specific site type for a portal."));
      sitetypes.registerOperationHandler(OperationNames.READ_RESOURCE, new SiteTypeReadResource(), description("Lists the available sites for a site type."));

      //TODO: Would be nice to find acceptable regex for site name, nav uri, page name
      ManagedResource.Registration sites = sitetypes.registerSubResource("{site-name: .*}", description("Management resource responsible for handling management operations on a specific site."));
      sites.registerOperationHandler(OperationNames.READ_RESOURCE, new SiteReadResource(), description("Lists all available artifacts for a given site (ie pages, navigation, site layout)"));

      // Site layout management and operation registration
      ManagedResource.Registration siteLayout = sites.registerSubResource("portal", description("Management resource responsible for handling management operations for a site layout."));
      siteLayout.registerOperationHandler(OperationNames.READ_RESOURCE, new SiteLayoutReadResource(), description("Retrieves site layout data for a specific site."));
      siteLayout.registerOperationHandler(OperationNames.EXPORT_RESOURCE, new SiteLayoutExportResource(), description("Exports a site layout as a zip file."));

      // Page management and operation registration
      PageExportResource pageExport = new PageExportResource();
      PageReadResource pageReadResource = new PageReadResource();
      ManagedResource.Registration pages = sites.registerSubResource("pages", description("Management resource responsible for handling management operations on all pages of a site."));
      pages.registerOperationHandler(OperationNames.READ_RESOURCE, new PagesReadResource(), description("Lists all available pages available for a site."));
      pages.registerOperationHandler(OperationNames.EXPORT_RESOURCE, pageExport, description("Exports all pages for a site as a zip file."));

      ManagedResource.Registration page = pages.registerSubResource("{page-name}", description("Page management resource representing an individual page."));
      page.registerOperationHandler(OperationNames.READ_RESOURCE, new PageReadResource(), description("Retrieves page data for a specific site."));
      page.registerOperationHandler(OperationNames.EXPORT_RESOURCE, pageExport, description("Exports a page as a zip file."));

      // Navigation management and operation registration
      NavigationReadResource navReadResource = new NavigationReadResource();
      NavigationExportResource navExport = new NavigationExportResource();
      ManagedResource.Registration navigation = sites.registerSubResource("navigation", description("Navigation management resource representing a sites navigation."));
      navigation.registerOperationHandler(OperationNames.READ_RESOURCE, navReadResource, description("Retrieves navigation for a specific site."));
      navigation.registerOperationHandler(OperationNames.EXPORT_RESOURCE, navExport, description("Exports navigation as a zip file."));

      ManagedResource.Registration navigationNode = navigation.registerSubResource("{nav-uri: .*}", description("Navigation node management resource representing a sites navigation."));
      navigationNode.registerOperationHandler(OperationNames.READ_RESOURCE, navReadResource, description("Retrieves navigation node for a specific site."));
      navigationNode.registerOperationHandler(OperationNames.EXPORT_RESOURCE, navExport, description("Exports navigation as a zip file."));
   }

   @Override
   public void destroy()
   {
   }

   private static ManagedDescription description(final String description)
   {
      return new ManagedDescription()
      {
         @Override
         public String getDescription()
         {
            return description;
         }
      };
   }
}
