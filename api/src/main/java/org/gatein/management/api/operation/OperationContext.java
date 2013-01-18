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

package org.gatein.management.api.operation;

import org.gatein.management.api.ContentType;
import org.gatein.management.api.ExternalContext;
import org.gatein.management.api.ManagedResource;
import org.gatein.management.api.ManagedUser;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.RuntimeContext;
import org.gatein.management.api.binding.BindingProvider;
import org.gatein.management.api.model.Model;
import org.gatein.management.api.model.ModelValue;

import java.util.Locale;

/**
 * Information provided to an operation handler during operation execution.
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public interface OperationContext
{
   /**
    *
    * @return the user if one is associated with the request. Returns null if user it not authenticated.
    */
   ManagedUser getUser();

   /**
    *
    * @return Path of the current operation.
    */
   PathAddress getAddress();

   /**
    *
    * @return name of the operation
    */
   String getOperationName();

   /**
    *
    * @return the current managed resource
    */
   ManagedResource getManagedResource();

   /**
    *
    * @return the runtime context
    */
   RuntimeContext getRuntimeContext();

   /**
    *
    * @return the external context
    */
   ExternalContext getExternalContext();

   /**
    * Creates a new detyped model which can be automatically un/marshaled.
    * @return the new model
    */
   Model newModel();

   /**
    * Creates a new detyped model can be automatically un/marshaled.
    *
    * @param modelType the type of model value, i.e. ModelString or ModelObject
    * @return the new model value
    */
   <T extends ModelValue> T newModel(Class<T> modelType);

   /**
    * Retrieves the current attachment available.
    *
    * @param remove removes the attribute, pointing to the next attribute if more are available.
    * @return the operation attachment
    */
   OperationAttachment getAttachment(boolean remove);

   /**
    * @return the operation attributes
    */
   OperationAttributes getAttributes();

   /**
    *
    * @return the local of the operation (can be null)
    */
   Locale getLocale();

   /**
    *
    * @return the binding provider registered by an extension.
    */
   BindingProvider getBindingProvider();

   /**
    * @return the content type of the operation.  This can indicate the content type of the response or request (or both)
    */
   ContentType getContentType();
}
