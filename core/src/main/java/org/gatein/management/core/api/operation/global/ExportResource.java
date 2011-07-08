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

package org.gatein.management.core.api.operation.global;

import org.gatein.management.api.ContentType;
import org.gatein.management.api.ManagedDescription;
import org.gatein.management.api.ManagedResource;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.RuntimeContext;
import org.gatein.management.api.binding.BindingProvider;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.operation.OperationAttachment;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.OperationHandler;
import org.gatein.management.api.operation.OperationNames;
import org.gatein.management.api.operation.QueryOperationHandler;
import org.gatein.management.api.operation.model.ExportResourceModel;
import org.gatein.management.api.operation.model.ExportTask;
import org.gatein.management.api.operation.model.ReadResourceModel;
import org.gatein.management.core.api.operation.BasicResultHandler;
import org.gatein.management.core.api.operation.StepResultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class ExportResource extends QueryOperationHandler<ExportResourceModel>
{
      @Override
   protected ExportResourceModel execute(OperationContext operationContext) throws ResourceNotFoundException, OperationException
   {
      ManagedResource resource = operationContext.getManagedResource();
      final PathAddress address = operationContext.getAddress();
      final String operationName = operationContext.getOperationName();

      StepResultHandler<ExportResourceModel> exportResultHandler = new StepResultHandler<ExportResourceModel>(address)
      {
         @Override
         public void failed(String failureDescription)
         {
            if (address.equals(getCurrentAddress()))
            {
               throw new OperationException(operationName, "Export operation failed. Reason: " + failureDescription);
            }
            else
            {
               throw new OperationException(operationName, "Export operation failed. Reason: " + failureDescription + " [Step Address: " + getCurrentAddress() + "]");
            }
         }
      };

      try
      {
         executeHandlers(resource, operationContext, address, operationName, exportResultHandler);
      }
      catch (OperationException e)
      {
         throw new OperationException(e.getOperationName(), getStepMessage(e, address, exportResultHandler), e);
      }
      catch (Throwable t)
      {
         throw new OperationException(operationName, getStepMessage(t, address, exportResultHandler), t);
      }

      List<ExportTask> tasks = new ArrayList<ExportTask>();
      for (ExportResourceModel model : exportResultHandler.getResults())
      {
         tasks.addAll(model.getTasks());
      }
      return new ExportResourceModel(tasks);
   }

   private void executeHandlers(ManagedResource resource, final OperationContext operationContext, PathAddress address, String operationName, StepResultHandler<ExportResourceModel> stepResultHandler)
   {
      OperationHandler handler = resource.getOperationHandler(address, operationName);
      if (handler != null && handler != this)
      {
         handler.execute(operationContext, stepResultHandler);
      }
      else
      {
         ManagedResource found = resource.getSubResource(address);
         if (found == null)
         {
            throw new OperationException(operationName, "Could not locate resource at address " + address);
         }
         
         OperationHandler opHandler = resource.getOperationHandler(address, OperationNames.READ_RESOURCE);
         BasicResultHandler resultHandler = new BasicResultHandler();
         opHandler.execute(operationContext, resultHandler);
         if (resultHandler.getFailureDescription() != null)
         {
            throw new OperationException(operationName, "Failure '" + resultHandler.getFailureDescription() + "' encountered executing " + OperationNames.READ_RESOURCE);
         }

         Object model = resultHandler.getResult();
         if (! (model instanceof ReadResourceModel) )
         {
            throw new RuntimeException("Was expecting " + ReadResourceModel.class + " to be returned for operation " + OperationNames.READ_RESOURCE + " at address " + address);
         }

         for (String child : ((ReadResourceModel) model).getChildren())
         {
            final PathAddress childAddress = address.append(child);
            OperationContext childContext = new ChildOperationContext(childAddress, operationContext);
            executeHandlers(resource, childContext, childAddress, operationName, stepResultHandler.next(childAddress));
         }
      }
   }

   private String getStepMessage(Throwable t, PathAddress originalAddress, StepResultHandler<ExportResourceModel> stepResultHandler)
   {
      String message = (t.getMessage() == null) ? "Step operation failure" : t.getMessage();
      if (originalAddress.equals(stepResultHandler.getCurrentAddress()))
      {
         return message;
      }
      else
      {
         return message + " [Step Address: " + stepResultHandler.getCurrentAddress() + "]";
      }
   }

   public static final ManagedDescription DESCRIPTION = new ManagedDescription()
   {
      @Override
      public String getDescription()
      {
         return "Exports any resources with an export operation handler registered.  This operation is recursive until an operation handler is found.";
      }
   };

   private static class ChildOperationContext implements OperationContext
   {
      private final PathAddress childAddress;
      private final OperationContext operationContext;

      public ChildOperationContext(PathAddress childAddress, OperationContext operationContext)
      {
         this.childAddress = childAddress;
         this.operationContext = operationContext;
      }

      @Override
      public PathAddress getAddress()
      {
         return childAddress;
      }

      @Override
      public String getOperationName()
      {
         return operationContext.getOperationName();
      }

      @Override
      public ManagedResource getManagedResource()
      {
         return operationContext.getManagedResource();
      }

      @Override
      public RuntimeContext getRuntimeContext()
      {
         return operationContext.getRuntimeContext();
      }

      @Override
      public OperationAttachment getAttachment(int index)
      {
         return operationContext.getAttachment(index);
      }

      @Override
      public int getAttachmentsCount()
      {
         return operationContext.getAttachmentsCount();
      }

      @Override
      public BindingProvider getBindingProvider()
      {
         return operationContext.getBindingProvider();
      }

      @Override
      public ContentType getContentType()
      {
         return operationContext.getContentType();
      }
   }
}
