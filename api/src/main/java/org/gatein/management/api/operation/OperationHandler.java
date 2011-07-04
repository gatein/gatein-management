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

import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.exceptions.ResourceNotFoundException;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public interface OperationHandler
{
   /**
    * Execute the operation, passing the result to {@code resultHandler}.  This method <b>must</b> invoke one of
    * the completion methods on {@code resultHandler} regardless of the outcome of the operation.
    *
    * @param operationContext the context for this operation
    * @param resultHandler    the result handler to invoke when the operation is complete
    * @throws org.gatein.management.api.exceptions.ResourceNotFoundException this is to allow implementations who are registered via path template to
    *                               throw this exception when resolved template value does not match a known value.
    * @throws OperationException    if the operation fails to execute correctly
    */
   void execute(OperationContext operationContext, ResultHandler resultHandler) throws ResourceNotFoundException, OperationException;
}
