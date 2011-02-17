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

package org.gatein.management.gadget.server.util;

/**
 * {@code ProcessException}
 * <p/>
 * Created on Feb 4, 2011, 10:47:24 AM
 *
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 * @version 1.0
 */
public class ProcessException extends Exception
{

   /**
    * Create a new instance of {@code ProcessException}
    */
   public ProcessException()
   {
      super();
   }

   /**
    * Create a new instance of {@code ProcessException}
    *
    * @param message the exception message
    */
   public ProcessException(String message)
   {
      super(message);
   }

   /**
    * Create a new instance of {@code ProcessException}
    *
    * @param cause the exception cause
    */
   public ProcessException(Throwable cause)
   {
      super(cause);
   }

   /**
    * Create a new instance of {@code ProcessException}
    *
    * @param message the exception message
    * @param cause   the exception cause
    */
   public ProcessException(String message, Throwable cause)
   {
      super(message, cause);
   }
}
