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

package org.gatein.management.core.api.operation;

import org.gatein.management.api.model.Model;
import org.gatein.management.api.model.ModelValue;
import org.gatein.management.api.operation.ResultHandler;
import org.gatein.management.core.api.model.DmrModelValue;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class BasicResultHandler implements ResultHandler
{
   private Object result;
   private String failureDescription;
   private ModelValue failure;

   @Override
   public void completed(Object result)
   {
      this.result = result;
   }

   @Override
   public Model completed()
   {
      this.result = DmrModelValue.newModel();
      return (Model) result;
   }

   @Override
   public void failed(String failureDescription)
   {
      this.failureDescription = failureDescription;
   }

   @Override
   public Model failed()
   {
      Model failure = DmrModelValue.newModel();
      this.failure = failure;
      return failure;
   }

   public Object getResult()
   {
      return result;
   }

   public String getFailureDescription()
   {
      return failureDescription;
   }

   public ModelValue getFailure()
   {
      return failure;
   }
}