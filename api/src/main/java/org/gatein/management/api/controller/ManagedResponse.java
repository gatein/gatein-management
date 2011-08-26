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

package org.gatein.management.api.controller;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public interface ManagedResponse
{
   /**
    * @return outcome of the response.  If it failed, a failure description must be provided.
    */
   Outcome getOutcome();

   /**
    * The result of an operation.  This object is the object an {@link org.gatein.management.api.operation.OperationHandler}
    * sets on the {@link org.gatein.management.api.operation.ResultHandler} object.  This result is specific to the operation
    * handler, and the extension it belongs to.
    * @return the result.
    */
   Object getResult();

   /**
    * Will write the result to the outputStream.  This will use the {@link org.gatein.management.api.binding.BindingProvider}
    * registered for an extension.
    *
    * @param outputStream the stream to write the result to.
    * @throws IOException if an exception occurred writing to the stream.
    */
   void writeResult(OutputStream outputStream) throws IOException;

   public static interface Outcome
   {
      /**
       * Indicates a successful outcome
       * @return true if the outcome was a success.
       */
      boolean isSuccess();

      /**
       * Indicates the outcome was a failure, providing a description.
       * @return the description of the failure.
       */
      String getFailureDescription();
   }
}
