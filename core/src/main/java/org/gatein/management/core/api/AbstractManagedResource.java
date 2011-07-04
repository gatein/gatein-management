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

import org.gatein.management.api.ManagedDescription;
import org.gatein.management.api.ManagedResource;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.PathAddressIterator;
import org.gatein.management.api.operation.OperationHandler;

import java.util.Set;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public abstract class AbstractManagedResource implements ManagedResource, ManagedResource.Registration
{
   protected final PathElement pathElement;
   protected final AbstractManagedResource parent;

   protected AbstractManagedResource(PathElement pathElement, AbstractManagedResource parent)
   {
      this.pathElement = pathElement;
      this.parent = parent;
   }

   @Override
   public final ManagedDescription getResourceDescription(PathAddress address)
   {
      return getResourceDescription(address.iterator());
   }

   @Override
   public final OperationHandler getOperationHandler(PathAddress address, String operationName)
   {
      OperationEntry entry = getOperationEntry(address.iterator(), null, operationName);

      return (entry == null) ? null : entry.getOperationHandler();
   }

   @Override
   public final ManagedDescription getOperationDescription(PathAddress address, String operationName)
   {
      OperationEntry entry = getOperationEntry(address.iterator(), null, operationName);

      return (entry == null) ? null : entry.getDescription();
   }

//   @Override
//   public Map<String, ManagedDescription> getOperationDescriptions(PathAddress address)
//   {
//      return getOperationDescriptions(address.iterator());
//   }

   @Override
   public final ManagedResource getSubResource(PathAddress address)
   {
      return getSubResource(address.iterator());
   }

   @Override
   public final Set<String> getChildNames(PathAddress address)
   {
      return getChildNames(address.iterator());
   }

   protected abstract ManagedDescription getResourceDescription(PathAddressIterator iterator);

   protected abstract OperationEntry getOperationEntry(PathAddressIterator iterator, OperationEntry inherited, String operationName);

   protected abstract AbstractManagedResource getSubResource(PathAddressIterator iterator);

   protected abstract Set<String> getChildNames(PathAddressIterator iterator);

   protected String getPath()
   {
      if (parent == null)
      {
         return (pathElement == null) ? "" : pathElement.getValue();
      }
      else
      {
         return parent.getPath() + "/" + pathElement.getValue();
      }
   }

   protected static final class OperationEntry
   {
      private final OperationHandler operationHandler;
      private final ManagedDescription description;
      private final boolean inherited;

      protected OperationEntry(final OperationHandler operationHandler, final ManagedDescription description, boolean inherited)
      {
         this.operationHandler = operationHandler;
         this.description = description;
         this.inherited = inherited;
      }

      protected OperationHandler getOperationHandler()
      {
         return operationHandler;
      }

      protected ManagedDescription getDescription()
      {
         return description;
      }

      protected boolean isInherited()
      {
         return inherited;
      }
   }
}
