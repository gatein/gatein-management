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
   ManagedDescription getResourceDescription(PathAddress address);

   OperationHandler getOperationHandler(PathAddress address, String operationName);

   ManagedDescription getOperationDescription(PathAddress address, String operationName);

   Map<String, ManagedDescription> getOperationDescriptions(PathAddress address);

   ManagedResource getSubResource(PathAddress address);

   Set<String> getChildNames(PathAddress address);

   public static interface Registration
   {
      Registration registerSubResource(String name, ManagedDescription description);

      void registerOperationHandler(String operationName, OperationHandler operationHandler, ManagedDescription description);

      void registerOperationHandler(String operationName, OperationHandler operationHandler, ManagedDescription description, boolean inherited);
   }
}
