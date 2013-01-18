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

package org.gatein.management.core.api.controller;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.api.ExternalContext;
import org.gatein.management.api.ManagedDescription;
import org.gatein.management.api.ManagedResource;
import org.gatein.management.api.ManagementService;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.RuntimeContext;
import org.gatein.management.api.binding.BindingProvider;
import org.gatein.management.api.controller.ManagedRequest;
import org.gatein.management.api.controller.ManagedResponse;
import org.gatein.management.api.controller.ManagementController;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.model.ModelProvider;
import org.gatein.management.api.operation.OperationHandler;
import org.gatein.management.api.operation.OperationNames;
import org.gatein.management.api.operation.model.NamedDescription;
import org.gatein.management.api.operation.model.ReadResourceModel;
import org.gatein.management.core.api.ExternalContextImpl;
import org.gatein.management.core.api.model.DmrModelProvider;
import org.gatein.management.core.api.operation.BasicResultHandler;
import org.gatein.management.core.api.operation.OperationContextImpl;

import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
//TODO: Add some logging
public class SimpleManagementController implements ManagementController
{
   private static final Logger log = LoggerFactory.getLogger(SimpleManagementController.class);

   private final ManagementService managementService;
   private final RuntimeContext runtimeContext;
   private ManagedResource rootResource;

   public SimpleManagementController(ManagementService managementService, RuntimeContext runtimeContext)
   {
      this.managementService = managementService;
      this.runtimeContext = runtimeContext;
   }

   @Override
   @SuppressWarnings("deprecation")
   public ManagedResponse execute(ManagedRequest request) throws ResourceNotFoundException, OperationException
   {
      // Simple way to reload extensions. NOTE: ManagementServiceImpl is not thread safe, so this should be used with caution.
      if (request.getAttributes().containsKey("reload-extensions"))
      {
         managementService.reloadExtensions();
         rootResource = managementService.getManagedResource(PathAddress.empty());
      }

      // TODO: Remove once READ_CONFIG_AS_XML is completely removed.
      if (OperationNames.READ_CONFIG_AS_XML.equals(request.getOperationName()))
      {
         log.warn(OperationNames.READ_CONFIG_AS_XML + " is deprecated. Please use " + OperationNames.READ_CONFIG + " instead with proper content type.");
         request = new DeprecatedManagedRequest(request);
      }

      PathAddress address = request.getAddress();
      String operationName = request.getOperationName();

      boolean debug = log.isDebugEnabled();
      if (debug)
      {
         log.debug("Executing request for operation " + operationName + " at address " + address);
      }
      
      ManagedResource root = getRootResource();
      if (root.getSubResource(address) == null)
      {
         throw new ResourceNotFoundException("Could not locate managed resource for address '" + address + "'");
      }

      OperationHandler operationHandler = root.getOperationHandler(address, operationName);
      if (operationHandler != null)
      {
         // Obtain binding provider given managed component.
         String componentName = (address.size() >= 1) ? address.get(0) : null;
         BindingProvider bindingProvider = managementService.getBindingProvider(componentName);

         // ModelProvider to use for de-typed models
         ModelProvider modelProvider = DmrModelProvider.INSTANCE;

         // Execute operation for given registered operation handler
         BasicResultHandler resultHandler = new BasicResultHandler();
         operationHandler.execute(new OperationContextImpl(request, root, runtimeContext, new ExternalContextImpl(request), bindingProvider, modelProvider), resultHandler);

         if (resultHandler.getFailureDescription() != null)
         {
            return new FailureResponse(modelProvider.newModel().set(resultHandler.getFailureDescription()));
         }
         else if (resultHandler.getFailure() != null)
         {
            return new FailureResponse(resultHandler.getFailure());
         }
         else
         {
            Object result = resultHandler.getResult();

            // Set descriptions based on the ManagedResource so 'dynamic' extensions don't have to.
            if (result instanceof ReadResourceModel)
            {
               ReadResourceModel readResource = (ReadResourceModel) result;
               if (!readResource.isChildDescriptionsSet())
               {
                  populateChildDescriptions(root, address, readResource);
               }

               if (readResource.getOperations().isEmpty())
               {
                  Map<String, ManagedDescription> descriptions = root.getOperationDescriptions(address);
                  for (Map.Entry<String, ManagedDescription> desc : descriptions.entrySet())
                  {
                     readResource.addOperation(new NamedDescription(desc.getKey(), desc.getValue().getDescription()));
                  }
               }
            }

            return new SuccessfulResponse<Object>(bindingProvider, result, request.getContentType());
         }
      }
      else
      {
         // Why pass in operation name, if it's not used as part of the message...
         throw new OperationException(operationName, "Operation '" + operationName  + "' not found for address '" + address + "'");
      }
   }
   
   private void populateChildDescriptions(ManagedResource root, PathAddress address, ReadResourceModel readResource)
   {
      ManagedResource currentResource = root.getSubResource(address);
      Set<String> subResourceNames = currentResource.getSubResourceNames(PathAddress.empty());
      
      // We have children but no sub resources which typically means that the same resource serves multiple paths like a navigation URI.
      if (!readResource.getChildren().isEmpty() && subResourceNames.isEmpty())
      {
         for (String childName : readResource.getChildren())
         {
            readResource.setChildDescription(childName, currentResource.getResourceDescription(PathAddress.empty()).getDescription());
         }
      }
      else
      {
         // Set children descriptions
         for (String subResourceName : subResourceNames)
         {
            ManagedResource subResource = currentResource.getSubResource(subResourceName);
            for (String childName : readResource.getChildren())
            {
               ManagedResource mr = root.getSubResource(address.append(childName));
               if (mr == subResource || mr == currentResource)
               {
                  readResource.setChildDescription(childName, mr.getResourceDescription(PathAddress.empty()).getDescription());
               }
            }
         }
      }
   }

   private ManagedResource getRootResource()
   {
      if (rootResource == null)
      {
         rootResource = managementService.getManagedResource(PathAddress.EMPTY_ADDRESS);
      }

      return rootResource;
   }
}
