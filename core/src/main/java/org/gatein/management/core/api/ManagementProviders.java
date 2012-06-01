/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.gatein.management.core.api;

import org.gatein.management.api.binding.BindingProvider;
import org.gatein.management.api.binding.ModelProvider;
import org.gatein.management.api.exceptions.ManagementException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class ManagementProviders
{
   private ConcurrentMap<String, BindingProvider> bindingProviders = new ConcurrentHashMap<String, BindingProvider>();
   private ConcurrentMap<String, ModelProvider> modelProviders = new ConcurrentHashMap<String, ModelProvider>();

   public void register(String componentName, BindingProvider bindingProvider)
   {
      BindingProvider bp;
      if ( (bp = bindingProviders.putIfAbsent(componentName, bindingProvider)) != null)
      {
         throw new ManagementException("Binding provider " + bp + " already registered for component " + componentName);
      }
   }

   public BindingProvider getBindingProvider(String componentName)
   {
      return bindingProviders.get(componentName);
   }

   public void register(String componentName, ModelProvider modelProvider)
   {
      ModelProvider mp;
      if ( (mp = modelProviders.putIfAbsent(componentName, modelProvider)) != null)
      {
         throw new ManagementException("Model provider " + mp + " already registered for component " + componentName);
      }
   }

   public ModelProvider getModelProvider(String componentName)
   {
      return modelProviders.get(componentName);
   }
}
