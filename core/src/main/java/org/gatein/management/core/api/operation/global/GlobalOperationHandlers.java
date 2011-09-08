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
import org.gatein.management.api.operation.model.NamedDescription;
import org.gatein.management.api.operation.model.ReadResourceModel;

import java.util.Map;
import java.util.Set;

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

         Set<String> children = resource.getSubResourceNames(address);
         ReadResourceModel readResourceModel = new ReadResourceModel("Available operations and children (sub-resources).", children);

         // Set children descriptions
         for (String child : children)
         {
            ManagedDescription desc = resource.getResourceDescription(address.append(child));
            readResourceModel.setChildDescription(child, desc.getDescription());
         }

         // Set operation descriptions
         Map<String, ManagedDescription> descriptions = resource.getOperationDescriptions(address);
         for (Map.Entry<String, ManagedDescription> desc : descriptions.entrySet())
         {
            readResourceModel.addOperation(new NamedDescription(desc.getKey(), desc.getValue().getDescription()));
         }

         return readResourceModel;
      }

      @Override
      public String getDescription()
      {
         return "Lists information about a managed resource, including available operations and children (sub-resources).";
      }
   }
}
