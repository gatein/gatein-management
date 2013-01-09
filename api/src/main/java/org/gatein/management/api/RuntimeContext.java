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

package org.gatein.management.api;

/**
 * Access to GateIn runtime.
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public interface RuntimeContext
{
   /**
    * Provides access to runtime components.
    *
    * @param componentClass the class (type) of the component.
    * @return the runtime component
    */
   <T> T getRuntimeComponent(Class<T> componentClass);

   /**
    * Returns a boolean indicating whether the user is included in the specified logical "role". If user is not
    * authenticated this returns false.
    *
    * @param role the name of the role
    * @return true if the user belongs to a given role or false if user is not authenticated
    */
   boolean isUserInRole(String role);
}
