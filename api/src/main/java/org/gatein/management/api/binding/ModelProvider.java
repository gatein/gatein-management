/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.gatein.management.api.binding;

import org.gatein.management.api.model.Model;
import org.gatein.management.api.model.ModelValue;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public interface ModelProvider
{
   /**
    * Retrieve the model mapper for the given class.
    *
    * @param type the class which can be mapped to a ModelValue
    * @return the mapper responsible for mapping to a ModelValue and from the object itself.
    */
   <T> ModelMapper<T> getModelMapper(Class<T> type);

   /**
    * Used to retrieve a mapper by name for annotated mgmt operations using <code>@Model</code>
    * @param modelName the name of the identifier used in the <code>@Model</code> annotations
    * @return the mapper responsible for mapping to a ModelValue and from the object itself.
    */
   ModelMapper<?> getModelMapper(String modelName);

   public static interface ModelMapper<T>
   {
      /**
       * Create the object from the given <code>ModelValue</code>
       *
       * @param value used to create the object
       * @return the object representing the <code>ModelValue</code>
       */
      T from(ModelValue value);

      /**
       * Create the <code>ModelValue</code> from the given object.
       * @param model the unset/undefined model value to set
       * @param object the object used to map to a <code>ModelValue</code>
       * @return the <code>ModelValue</code>
       */
      ModelValue to(Model model, T object);
   }
}