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

package org.gatein.management.core.api.controller;

import org.gatein.management.api.controller.ManagedResponse;
import org.gatein.management.api.model.ModelValue;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class FailureResponse implements ManagedResponse
{
   private final ModelValue failure;

   public FailureResponse(ModelValue failure)
   {
      this.failure = failure;
   }

   @Override
   public Outcome getOutcome()
   {
      return new FailureOutcome();
   }

   @Override
   public Object getResult()
   {
      return failure;
   }

   public void writeResult(OutputStream outputStream, boolean pretty) throws IOException
   {
      failure.toJsonStream(outputStream, pretty);
   }

   private class FailureOutcome implements Outcome
   {
      @Override
      public boolean isSuccess()
      {
         return false;
      }

      @Override
      public String getFailureDescription()
      {
         return failure.toString();
      }
   }
}
