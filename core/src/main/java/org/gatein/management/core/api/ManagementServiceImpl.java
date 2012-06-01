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

package org.gatein.management.core.api;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.api.ContentType;
import org.gatein.management.api.ManagedDescription;
import org.gatein.management.api.ManagedResource;
import org.gatein.management.api.ManagementService;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.binding.BindingException;
import org.gatein.management.api.binding.BindingProvider;
import org.gatein.management.api.binding.Marshaller;
import org.gatein.management.api.binding.ModelProvider;
import org.gatein.management.api.operation.OperationNames;
import org.gatein.management.core.api.binding.GlobalBindingProvider;
import org.gatein.management.core.api.binding.json.ModelMapperMarshaller;
import org.gatein.management.core.api.operation.global.GlobalOperationHandlers;
import org.gatein.management.core.spi.ExtensionContextImpl;
import org.gatein.management.spi.ExtensionContext;
import org.gatein.management.spi.ManagementExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class ManagementServiceImpl implements ManagementService
{
   private static final Logger log = LoggerFactory.getLogger(ManagementService.class);

   private ManagedResource rootResource;
   private List<ManagementExtension> extensions;
   private ManagementProviders providers;
   private BindingProvider globalBindingProvider;

   @Override
   public ManagedResource getManagedResource(PathAddress address)
   {
      return rootResource.getSubResource(address);
   }

   @Override
   public BindingProvider getBindingProvider(final String componentName)
   {
      return new BindingProvider()
      {
         @Override
         public <T> Marshaller<T> getMarshaller(Class<T> type, ContentType contentType) throws BindingException
         {
            Marshaller<T> marshaller = null;
            BindingProvider bp = providers.getBindingProvider(componentName);
            if (bp != null)
            {
               marshaller = bp.getMarshaller(type, contentType);
               if (marshaller != null) return marshaller;
            }

            if (contentType == ContentType.JSON)
            {
               ModelProvider mp = providers.getModelProvider(componentName);
               if (mp != null)
               {
                  ModelProvider.ModelMapper<T> mapper = mp.getModelMapper(type);
                  if (mapper != null)
                  {
                     return new ModelMapperMarshaller<T>(mapper);
                  }
               }
            }

            return globalBindingProvider.getMarshaller(type, contentType);
         }
      };
   }

   @Override
   public void reloadExtensions()
   {
      unload();
      load();
   }

   @Override
   public void load()
   {
      extensions = new ArrayList<ManagementExtension>();

      SimpleManagedResource resource = new SimpleManagedResource(null, null, new ManagedDescription()
      {
         @Override
         public String getDescription()
         {
            return "Root management resource.";
         }
      });

      ManagementProviders providers = new ManagementProviders();
      ExtensionContext context = new ExtensionContextImpl(resource, providers);

      ServiceLoader<ManagementExtension> loader = ServiceLoader.load(ManagementExtension.class);
      for (ManagementExtension extension : loader)
      {
         try
         {
            extension.initialize(context);
            extensions.add(extension);
         }
         catch (Exception e)
         {
            log.error("Could not initialize extension " + extension, e);
         }
      }

      log.debug("Successfully loaded " + extensions.size() + " management extension(s).");

      initGlobalOperations(resource);

      rootResource = resource;
      this.providers = providers;
      globalBindingProvider = new GlobalBindingProvider();
   }

   @Override
   public void unload()
   {
      if (extensions != null)
      {
         for (ManagementExtension extension : extensions)
         {
            extension.destroy();
         }
         extensions.clear();
      }

      rootResource = null;
   }

   private void initGlobalOperations(ManagedResource.Registration registration)
   {
      registration.registerOperationHandler(OperationNames.READ_RESOURCE, GlobalOperationHandlers.READ_RESOURCE, GlobalOperationHandlers.READ_RESOURCE, true);
   }
}
