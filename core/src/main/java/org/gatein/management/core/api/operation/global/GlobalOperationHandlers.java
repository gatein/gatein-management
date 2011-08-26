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

import org.gatein.management.api.ManagedDescription;
import org.gatein.management.api.ManagedResource;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.QueryOperationHandler;
import org.gatein.management.api.operation.model.OperationInfo;
import org.gatein.management.api.operation.model.ReadResourceModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class GlobalOperationHandlers
{
   private GlobalOperationHandlers(){}

   public static final ExportResource EXPORT_RESOURCE = new ExportResource();

   public static final ReadResource READ_RESOURCE = new ReadResource();

   public static final class ReadResource extends QueryOperationHandler<ReadResourceModel> implements ManagedDescription
   {
      @Override
      public ReadResourceModel execute(OperationContext operationContext)
      {
         ManagedResource resource = operationContext.getManagedResource();
         PathAddress address = operationContext.getAddress();

         Map<String, ManagedDescription> descriptions = resource.getOperationDescriptions(address);
         List<OperationInfo> operations = new ArrayList<OperationInfo>(descriptions.size());
         for (Map.Entry<String, ManagedDescription> desc : descriptions.entrySet())
         {
            operations.add(new OperationInfo(desc.getKey(), desc.getValue().getDescription()));
         }

         return new ReadResourceModel("Lists registered managed components.", resource.getChildNames(address), operations);
      }

      @Override
      public String getDescription()
      {
         return "Lists information about a managed resource, including available operations and children (sub-resources).";
      }
   }
}
