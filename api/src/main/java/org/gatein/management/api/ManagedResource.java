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

package org.gatein.management.api;

import org.gatein.management.api.operation.OperationHandler;

import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public interface ManagedResource
{
   /**
    * Managed resource description
    *
    * @param address address of the managed resource
    * @return description of this managed resource
    */
   ManagedDescription getResourceDescription(PathAddress address);

   /**
    * Retrieves an operation handler for a given managed resource.
    *
    * @param address address of the managed resource
    * @param operationName name of the operation
    * @return the {@link OperationHandler} responsible for executing the operation.
    */
   OperationHandler getOperationHandler(PathAddress address, String operationName);

   /**
    * Description of the operation for a given managed resource.
    *
    * @param address address of the resource
    * @param operationName name of the operation
    * @return a managed description or null if operation doesn't exist.
    */
   ManagedDescription getOperationDescription(PathAddress address, String operationName);

   /**
    * Description of all operations for a given managed resource.
    *
    * @param address address of the resource
    * @return map of descriptions with the key being the operation name.
    */
   Map<String, ManagedDescription> getOperationDescriptions(PathAddress address);

   /**
    * Retrieves a managed resource given an address.
    *
    * @param address address of the sub-resource
    * @return a manged resource or null if none was found.
    */
   ManagedResource getSubResource(PathAddress address);

   /**
    * Retrieves a managed resource given a child's name.
    *
    * @param childName name of the child sub-resource
    * @return a manged resource or null if none was found.
    */
   ManagedResource getSubResource(String childName);

   /**
    * List of direct sub-resource names for a given managed resource and address
    *
    * @param address address of the managed resource.
    * @return set of sub-resource names, or an empty set if no sub-resources exist.
    */
   Set<String> getSubResourceNames(PathAddress address);

   public static interface Registration
   {
      /**
       * Registers a sub-resource for the given managed resource.  This becomes a child of the managed resource.
       *
       * @param name name of the sub-resource
       * @param description description of the sub-resource
       * @return registration belonging to the sub-resource.
       */
      Registration registerSubResource(String name, ManagedDescription description);

      /**
       * Registers an operation handler for a given operation.
       *
       * @param operationName name of the operation
       * @param operationHandler object responsible for handling the operation
       * @param description description of the operation
       */
      void registerOperationHandler(String operationName, OperationHandler operationHandler, ManagedDescription description);

      /**
       * Registers an operation handler for a given operation.
       *
       * @param operationName name of the operation
       * @param operationHandler object responsible for handling the operation
       * @param description description of the operation
       * @param inherited indicates whether this operation should be inherited by sub-resources. Default is false.
       */
      void registerOperationHandler(String operationName, OperationHandler operationHandler, ManagedDescription description, boolean inherited);
   }
}
