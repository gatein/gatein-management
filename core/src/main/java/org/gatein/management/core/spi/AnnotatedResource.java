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
import org.gatein.management.api.annotations.ManagedAfter;
import org.gatein.management.api.annotations.ManagedBefore;
import org.gatein.management.api.annotations.ManagedContext;
import org.gatein.management.api.model.ModelProvider;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.core.api.AbstractManagedResource;
import org.gatein.management.core.api.model.DmrModelProvider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class AnnotatedResource
{
   private static final Logger log = LoggerFactory.getLogger("org.gatein.management.core.spi");

   //
   private List<AnnotatedOperation> methods;
   private Object instance;
   private boolean component;

   //
   final Class<?> managedClass;
   final Method beforeMethod;
   final Method afterMethod;
   final AnnotatedResource parent;
   final AnnotatedOperation operation; // sub operation

   AnnotatedResource(Class<?> managedClass)
   {
      this(managedClass, null, null);
   }

   AnnotatedResource(Class<?> managedClass, AnnotatedResource parent, AnnotatedOperation operation)
   {
      this.managedClass = managedClass;
      this.parent = parent;
      this.operation = operation;

      // Save before and after methods
      Method[] methods = managedClass.getDeclaredMethods();
      this.beforeMethod = getMethod(methods, ManagedBefore.class);
      this.afterMethod = getMethod(methods, ManagedAfter.class);
   }

   public void register(AbstractManagedResource resource)
   {
      AbstractManagedResource amr = registerOrGetResource(resource, managedClass.getAnnotation(Managed.class));
      for (AnnotatedOperation operation : getAnnotatedMethods())
      {
         operation.registerOperation(amr);
      }
   }

   Object getInstance(OperationContext context)
   {
      if (instance != null) return instance;

      if (parent == null)
      {
         instance = context.getRuntimeContext().getRuntimeComponent(managedClass);
         if (instance != null)
         {
            component = true;
            setModelProvider(managedClass, instance);
         }
      }
      else if (operation != null)
      {
         instance = operation.invokeOperation(context);
      }

      if (instance == null)
      {
         try
         {
            instance = managedClass.newInstance();
         }
         catch (Exception e)
         {
            throw new RuntimeException("Could not create new instance of class " + managedClass.getName(), e);
         }
      }

      if (instance != null && !component)
      {
         setModelProvider(managedClass, instance);
      }

      return instance;
   }

   void discardInstance()
   {
      if (!component) instance = null;
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

   static AbstractManagedResource registerOrGetResource(AbstractManagedResource resource, Managed managed)
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

   private static Method getMethod(Method[] methods, Class<? extends Annotation> annotation)
   {
      for (Method method : methods)
      {
         if (method.isAnnotationPresent(annotation)) return method;
      }

      return null;
   }

   private static void setModelProvider(Class<?> managedClass, Object instance)
   {
      if (instance == null) return;

      Field[] fields = managedClass.getDeclaredFields();
      for (Field field : fields)
      {
         if (field.isAnnotationPresent(ManagedContext.class))
         {
            if (field.getType() == ModelProvider.class)
            {
               if (!field.isAccessible())
               {
                  field.setAccessible(true);
               }
               try
               {
                  field.set(instance, DmrModelProvider.INSTANCE);
               }
               catch (IllegalAccessException e)
               {
                  throw new RuntimeException("Unable to set ModelProvider for managed class " + managedClass, e);
               }
            }
            else
            {
               throw new RuntimeException("Field " + field + " is annotated with @ManagedContext, however it has an unknown type " + field.getType() + ". Only ModelProvider is allowed as the type for this field.");
            }
         }
      }
   }
}
