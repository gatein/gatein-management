/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.gatein.management.pomdata.core.rest;

import org.gatein.management.binding.api.BindingProvider;
import org.gatein.management.binding.rest.BindingRestProvider;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
//TODO: Make this extensible and move out of pomdata module
public class POMDataRestApplication extends Application
{
   private BindingProvider bindingProvider;

   public POMDataRestApplication(BindingProvider bindingProvider)
   {
      this.bindingProvider = bindingProvider;
   }

   @Override
   public Set<Object> getSingletons()
   {
      Set<Object> singletons = new HashSet<Object>();
      singletons.add(new BindingRestProvider(bindingProvider));

      return singletons;
   }

   @Override
   public Set<Class<?>> getClasses()
   {
      Set<Class<?>> classes = new HashSet<Class<?>>();
      classes.add(POMDataResource.class);

      return classes;
   }
}
