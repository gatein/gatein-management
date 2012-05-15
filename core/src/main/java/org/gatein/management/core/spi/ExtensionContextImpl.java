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

package org.gatein.management.core.spi;

import org.gatein.management.api.ComponentRegistration;
import org.gatein.management.api.ManagedDescription;
import org.gatein.management.api.ManagedResource;
import org.gatein.management.api.binding.BindingProvider;
import org.gatein.management.api.model.ModelProvider;
import org.gatein.management.api.operation.OperationNames;
import org.gatein.management.core.api.ManagementProviders;
import org.gatein.management.core.api.operation.global.ExportResource;
import org.gatein.management.core.api.operation.global.GlobalOperationHandlers;
import org.gatein.management.spi.ExtensionContext;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class ExtensionContextImpl implements ExtensionContext
{
   private final ManagedResource.Registration rootRegistration;
   private final ManagementProviders providers;

   public ExtensionContextImpl(ManagedResource.Registration rootRegistration, ManagementProviders providers)
   {
      this.rootRegistration = rootRegistration;
      this.providers = providers;
   }

   @Override
   public ComponentRegistration registerManagedComponent(final String name) throws IllegalArgumentException
   {
      if (name == null) throw new IllegalArgumentException("name is null");

      return new ComponentRegistration()
      {
         @Override
         public ManagedResource.Registration registerManagedResource(ManagedDescription description)
         {
            ManagedResource.Registration registration = rootRegistration.registerSubResource(name, description);
            registration.registerOperationHandler(OperationNames.EXPORT_RESOURCE, GlobalOperationHandlers.EXPORT_RESOURCE, ExportResource.DESCRIPTION, true);

            return registration;
         }

         @Override
         public void registerBindingProvider(BindingProvider bindingProvider)
         {
            providers.register(name, bindingProvider);
         }

         @Override
         public void registerModelProvider(ModelProvider modelProvider)
         {
            providers.register(name, modelProvider);
         }
      };
   }
}
