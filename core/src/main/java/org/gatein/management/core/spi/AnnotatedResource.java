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

package org.gatein.management.core.spi;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.api.ManagedDescription;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.annotations.Managed;
import org.gatein.management.core.api.AbstractManagedResource;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class AnnotatedResource
{
   static final Logger log = LoggerFactory.getLogger("org.gatein.management.core.spi");

   //
   private List<AnnotatedOperation> methods;

   //
   final Class<?> managedClass;
   final AnnotatedResource parent;

   AnnotatedResource(Class<?> managedClass)
   {
      this(managedClass, null);
   }

   AnnotatedResource(Class<?> managedClass, AnnotatedResource parent)
   {
      this.managedClass = managedClass;
      this.parent = parent;
   }

   public void register(AbstractManagedResource resource)
   {
      AbstractManagedResource amr = register(resource, managedClass.getAnnotation(Managed.class));
      for (AnnotatedOperation method : getAnnotatedMethods())
      {
         method.register(amr);
      }
   }

   public List<AnnotatedOperation> getAnnotatedMethods()
   {
      if (methods == null)
      {
         List<AnnotatedOperation> methods = new ArrayList<AnnotatedOperation>();
         for (Method method : managedClass.getMethods())
         {
            if (method.isAnnotationPresent(Managed.class))
            {
               methods.add(new AnnotatedOperation(this, method));
            }
         }
         this.methods = methods;
      }

      return methods;
   }

   static AbstractManagedResource register(AbstractManagedResource resource, Managed managed)
   {
      PathAddress address = PathAddress.pathAddress(managed.value());
      for (Iterator<String> iterator = address.iterator(); iterator.hasNext();)
      {
         String path = iterator.next();
         String description = "";
         if (iterator.hasNext())
         {
            description = managed.description();
         }
         AbstractManagedResource child = (AbstractManagedResource) resource.getSubResource(path);
         if (child == null)
         {
            if (log.isDebugEnabled()) log.debug("Registering managed resource " + path);
            child = (AbstractManagedResource) resource.registerSubResource(path, description(description));
         }

         resource = child;
      }

      return resource;
   }

   static ManagedDescription description(final String description)
   {
      return new ManagedDescription()
      {
         @Override
         public String getDescription()
         {
            return description;
         }
      };
   }
}
