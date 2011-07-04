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

import org.gatein.management.api.PathAddress;
import org.gatein.management.api.binding.ContentType;

import java.io.InputStream;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public interface ManagedRequest
{
   String getOperationName();

   PathAddress getAddress();

   InputStream getDataStream();

   ContentType getContentType();

//   public static class Factory
//   {
//      public static ManagedRequest create(final String operationName, final PathAddress address)
//      {
//         return create(operationName, address, null);
//      }
//
//      public static ManagedRequest create(final String operationName, final PathAddress address, final Object model)
//      {
//         return new ManagedRequest()
//         {
//            @Override
//            public String getOperationName()
//            {
//               return operationName;
//            }
//
//            @Override
//            public PathAddress getAddress()
//            {
//               return address;
//            }
//
//            @Override
//            public Object getModel()
//            {
//               return model;
//            }
//         };
//      }
//   }
}
