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
import org.gatein.management.api.ManagedResource;
import org.gatein.management.api.ManagementService;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.controller.ManagementController;
import org.gatein.management.api.controller.ManagedRequest;
import org.gatein.management.api.controller.ManagedResponse;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.operation.OperationHandler;
import org.gatein.management.api.operation.ResultHandler;
import org.gatein.management.api.binding.BindingProvider;
import org.gatein.management.core.api.ManagementServiceImpl;
import org.gatein.management.core.api.operation.OperationContextImpl;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class SimpleManagementController implements ManagementController
{
   private static final Logger log = LoggerFactory.getLogger(SimpleManagementController.class);

   private final ManagementServiceImpl service;
   private ManagedResource rootResource;

   public SimpleManagementController(ManagementService service)
   {
      //TODO: This is a hack to get at the runtime context, let's come up with a better way or just depend on implementation
      this.service = (ManagementServiceImpl) service;
   }

//   @Override
//   public ManagedResponse execute(PathAddress address, String operationName) throws ResourceNotFoundException, OperationException
//   {
//      return execute(address, operationName, null);
//   }

   @Override
   public ManagedResponse execute(ManagedRequest request) throws ResourceNotFoundException, OperationException
   {
      PathAddress address = request.getAddress();
      String operationName = request.getOperationName();
      
      ManagedResource root = getRootResource();
      if (root.getSubResource(address) == null)
      {
         throw new ResourceNotFoundException("Could not locate managed resource for address " + address);
      }

      OperationHandler operationHandler = root.getOperationHandler(address, operationName);
      if (operationHandler != null)
      {
         SimpleResultHandler resultHandler = new SimpleResultHandler();
         String componentName = (address.size() >= 1) ? address.get(0) : null;
         BindingProvider bindingProvider = service.getBindingProvider(componentName);
         operationHandler.execute(new OperationContextImpl(request, root, service.getRuntimeContext(), bindingProvider), resultHandler);

         if (resultHandler.failureDescription != null)
         {
            return new FailureResponse(resultHandler.failureDescription);
         }
         else
         {
            return new SuccessfulResponse<Object>(bindingProvider, resultHandler.result);
         }
      }
      else
      {
         throw new OperationException(operationName, "Operation not found for address " + address);
      }
   }

   private ManagedResource getRootResource()
   {
      if (rootResource == null)
      {
         rootResource = service.getManagedResource(PathAddress.EMPTY_ADDRESS);
      }

      return rootResource;
   }

   private static class SimpleResultHandler implements ResultHandler
   {
      private Object result;
      private String failureDescription;

      @Override
      public void completed(Object result)
      {
         this.result = result;
      }

      @Override
      public void failed(String failureDescription)
      {
         this.failureDescription = failureDescription;
      }
   }
}
