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

package org.gatein.management.api.annotations;

import org.gatein.management.api.PathAddress;
import org.gatein.management.api.operation.OperationAttributes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface MappedBy
{
   /**
    * Points to the class used to map the parameter. The class should have a zero arg constructor.
    * See {@link Mapper} for more details.
    */
   Class<? extends Mapper> value();

   public static interface Mapper<T>
   {
      /**
       * Used as a custom mapper to resolve parameters annotated with {@link MappedBy}
       *
       * @param address used to resolve a <code>Managed</code> path value by calling {@link org.gatein.management.api.PathAddress#resolvePathTemplate(String)}
       * @param attributes all operation attributes
       * @return the object to be passed as the parameter annotated by {@link MappedBy}
       */
      public abstract T map(PathAddress address, OperationAttributes attributes);
   }
}
