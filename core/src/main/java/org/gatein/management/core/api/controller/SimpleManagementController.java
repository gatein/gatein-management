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
import org.gatein.management.api.operation.OperationHandler;
import org.gatein.management.api.operation.model.OperationInfo;
import org.gatein.management.api.operation.model.ReadResourceModel;
import org.gatein.management.core.api.ManagementServiceImpl;
import org.gatein.management.core.api.operation.BasicResultHandler;
import org.gatein.management.core.api.operation.OperationContextImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
//TODO: Add some logging
public class SimpleManagementController implements ManagementController
{
   private static final Logger log = LoggerFactory.getLogger(SimpleManagementController.class);

   private ManagementService managementService;
   private RuntimeContext runtimeContext;
   private ManagedResource rootResource;

   public SimpleManagementController(ManagementService managementService, RuntimeContext runtimeContext)
   {
      this.managementService = managementService;
      this.runtimeContext = runtimeContext;
   }

   @Override
   public ManagedResponse execute(ManagedRequest request) throws ResourceNotFoundException, OperationException
   {
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

         // Execute operation for given registered operation handler
         BasicResultHandler resultHandler = new BasicResultHandler();
         operationHandler.execute(new OperationContextImpl(request, root, runtimeContext, bindingProvider), resultHandler);

         if (resultHandler.getFailureDescription() != null)
         {
            return new FailureResponse(resultHandler.getFailureDescription());
         }
         else
         {
            Object result = resultHandler.getResult();
            if (result instanceof ReadResourceModel)
            {
               ReadResourceModel readResource = (ReadResourceModel) result;
               if (readResource.getOperations() == null || readResource.getOperations().isEmpty())
               {
                  Map<String, ManagedDescription> descriptions = root.getOperationDescriptions(address);
                  List<OperationInfo> operations = new ArrayList<OperationInfo>(descriptions.size());
                  for (Map.Entry<String, ManagedDescription> desc : descriptions.entrySet())
                  {
                     operations.add(new OperationInfo(desc.getKey(), desc.getValue().getDescription()));
                  }

                  result = new ReadResourceModel(readResource.getDescription(), readResource.getChildren(), operations);
               }
            }

            return new SuccessfulResponse<Object>(bindingProvider, result, request.getContentType());
         }
      }
      else
      {
         throw new OperationException(operationName, "Operation not found for address '" + address + "'");
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
