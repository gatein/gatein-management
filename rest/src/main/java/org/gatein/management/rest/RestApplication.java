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

import org.gatein.management.api.ManagementService;
import org.gatein.management.api.controller.ManagementController;
import org.gatein.management.rest.providers.BindingProviderResolver;
import org.gatein.management.rest.providers.ManagedComponentProvider;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class RestApplication extends Application
{
   public static final String API_ENTRY_POINT = "/managed-components";

   private final ManagementController controller;
   private final ManagementService service;

   public RestApplication(ManagementService service, ManagementController controller)
   {
      this.controller = controller;
      this.service = service;
   }

   @Override
   public Set<Object> getSingletons()
   {
      Set<Object> singletons = new HashSet<Object>(2);
      singletons.add(new BindingProviderResolver(service));
      singletons.add(new RestController(controller));

      return singletons;
   }

   @Override
   public Set<Class<?>> getClasses()
   {
      Set<Class<?>> classes = new HashSet<Class<?>>(1);
      classes.add(ManagedComponentProvider.class);
      return classes;
   }
}
