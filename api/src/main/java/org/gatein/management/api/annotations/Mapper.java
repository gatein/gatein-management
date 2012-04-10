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

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public abstract class Mapper<T>
{
   protected Mapper(){}

   /**
    * Used as a custom mapper to resolve parameters annotated with {@link Mapped}
    *
    * @param address used to resolve {@link ManagedPath} annotations by calling {@link PathAddress#resolvePathTemplate(String)}
    * @param attributes all operation attributes
    * @return the object to be passed as the parameter annotated by {@link Mapped}
    */
   public abstract T map(PathAddress address, OperationAttributes attributes);
}
