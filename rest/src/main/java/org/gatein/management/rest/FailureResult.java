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

package org.gatein.management.rest;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
@XmlRootElement
public class FailureResult
{
   private String failure;
   private String operationName;

   public FailureResult(){}

   public FailureResult(String failure, String operationName)
   {
      this.failure = failure;
      this.operationName = operationName;
   }

   public String getFailure()
   {
      return failure;
   }

   public void setFailure(String failure)
   {
      this.failure = failure;
   }

   public String getOperationName()
   {
      return operationName;
   }

   public void setOperationName(String operationName)
   {
      this.operationName = operationName;
   }
}
