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

package org.gatein.management.binding.core.api;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.binding.api.BindingContext;
import org.gatein.management.binding.api.BindingException;
import org.gatein.management.binding.api.BindingProvider;
import org.gatein.management.binding.api.Bindings;
import org.gatein.management.binding.api.Marshaller;
import org.gatein.management.binding.spi.BindingExtension;
import org.picocontainer.Startable;

import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class BindingProviderImpl implements BindingProvider, Startable
{
   private static final Logger log = LoggerFactory.getLogger(BindingProvider.class);

   private ArrayDeque<Class<? extends Marshaller>> marshallerClasses = new ArrayDeque<Class<? extends Marshaller>>();

   @Override
   public synchronized void registerMarshaller(Class<? extends Marshaller> marshallerClass) throws BindingException
   {
      if (marshallerClass == null) throw new BindingException("Cannot register null marshaller class.");

      if (marshallerClasses.contains(marshallerClass))
         throw new BindingException("Cannot register multiple marshallers of the same class " + marshallerClass);

      if (!marshallerClass.isAnnotationPresent(Bindings.class))
         throw new BindingException("Cannot register marshaller without Bindings annotation.");

      log.debug("Registering marshaller class " + marshallerClass);

      // Last marshaller registered takes priority when choosing which marshaller to create based on @Bindings
      // TODO: We could add a priority to register method
      marshallerClasses.addFirst(marshallerClass);
   }

   @Override
   public <T> BindingContext<T> createContext(Class<T> type) throws BindingException
   {
      Set<Class<? extends Marshaller>> marshallers = new LinkedHashSet<Class<? extends Marshaller>>();
      marshallers.addAll(marshallerClasses);

      return new BindingContextImpl<T>(type, marshallers);
   }

   @Override
   public void start()
   {
      load();
   }

   @Override
   public void stop()
   {
      unload();
   }

   @Override
   public void load()
   {
      log.trace("Loading binding extensions.");

      ServiceLoader<BindingExtension> extensions = ServiceLoader.load(BindingExtension.class, Thread.currentThread().getContextClassLoader());
      if (extensions == null)
      {
         log.warn("No binding extensions registered.");
         return;
      }

      // Initialize extensions
      for (BindingExtension extension : extensions)
      {
         try
         {
            log.debug("Initializing binding extension " + extension.getClass());
            extension.initialize(this);
         }
         catch (Exception e)
         {
            log.error("Could not initialize binding extension " + extension, e);
         }
      }

      log.trace("Finished loading binding extensions.");
   }

   @Override
   public void unload()
   {
      marshallerClasses.clear();
   }
}
