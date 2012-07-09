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

package org.gatein.management.core.spi;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.api.ComponentRegistration;
import org.gatein.management.api.ManagedDescription;
import org.gatein.management.api.ManagedResource;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.annotations.Managed;
import org.gatein.management.api.annotations.ManagedModel;
import org.gatein.management.api.annotations.ManagedOperation;
import org.gatein.management.api.annotations.MappedAttribute;
import org.gatein.management.api.annotations.MappedBy;
import org.gatein.management.api.annotations.MappedPath;
import org.gatein.management.api.binding.BindingProvider;
import org.gatein.management.api.binding.Marshaller;
import org.gatein.management.api.binding.ModelProvider;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.OperationHandler;
import org.gatein.management.api.operation.OperationNames;
import org.gatein.management.api.operation.ResultHandler;
import org.gatein.management.api.operation.model.NoResultModel;
import org.gatein.management.core.api.AbstractManagedResource;
import org.gatein.management.core.api.ManagementProviders;
import org.gatein.management.core.api.operation.global.ExportResource;
import org.gatein.management.core.api.operation.global.GlobalOperationHandlers;
import org.gatein.management.spi.ExtensionContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class ExtensionContextImpl implements ExtensionContext
{
   private static final Logger log = LoggerFactory.getLogger("org.gatein.management.spi");

   private final AbstractManagedResource rootResource;
   private final ManagementProviders providers;

   public ExtensionContextImpl(AbstractManagedResource rootResource, ManagementProviders providers)
   {
      this.rootResource = rootResource;
      this.providers = providers;
   }

   @Override
   public ComponentRegistration registerManagedComponent(final String name) throws IllegalArgumentException
   {
      if (name == null) throw new IllegalArgumentException("name is null");

      return new ComponentRegistration()
      {
         @Override
         public ManagedResource.Registration registerManagedResource(ManagedDescription description)
         {
            ManagedResource.Registration registration = rootResource.registerSubResource(name, description);
            registration.registerOperationHandler(OperationNames.EXPORT_RESOURCE, GlobalOperationHandlers.EXPORT_RESOURCE, ExportResource.DESCRIPTION, true);

            return registration;
         }

         @Override
         public void registerBindingProvider(BindingProvider bindingProvider)
         {
            providers.register(name, bindingProvider);
         }

         @Override
         public void registerModelProvider(ModelProvider modelProvider)
         {
            providers.register(name, modelProvider);
         }
      };
   }

   @Override
   public ComponentRegistration registerManagedComponent(Class<?> component)
   {
      boolean debug = log.isDebugEnabled();
      if (debug) log.debug("Processing managed annotations for class " + component);

      Managed managed = component.getAnnotation(Managed.class);
      if (managed == null) throw new RuntimeException(Managed.class + " annotation not present on " + component);

      String componentName = managed.value();
      if ("".equals(componentName)) throw new RuntimeException(Managed.class + " annotation must have a value (path) for component class " + component);
      if (debug) log.debug("Registering managed component " + componentName);

      ComponentRegistration registration = registerManagedComponent(componentName);
      registration.registerManagedResource(description(managed.description()));

      // Register operations
      AbstractManagedResource resource = registerManaged(managed, rootResource);
      Method[] methods = component.getMethods();
      for (Method method : methods)
      {
         Managed managedMethod = method.getAnnotation(Managed.class);
         if (managedMethod != null)
         {
            if (debug) log.debug("Processing managed method " + getMethodName(method));
            registerManagedOperation(registerManaged(managedMethod, resource), method, component, componentName);
         }
      }

      return registration;
   }

   private AbstractManagedResource registerManaged(Managed managed, AbstractManagedResource resource)
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
            if (log.isDebugEnabled()) log.debug("Registering sub resource " + path);
            child = (AbstractManagedResource) resource.registerSubResource(path, description(description));
         }

         resource = child;
      }

      return resource;
   }

   private void registerManagedOperation(AbstractManagedResource resource, final Method method, final Class<?> componentClass, final String componentName)
   {
      ManagedOperation mo = method.getAnnotation(ManagedOperation.class);
      String operationName = OperationNames.READ_RESOURCE;
      String description = "";
      if (mo != null)
      {
         operationName = mo.name();
         description = mo.description();
      }

      final boolean debug = log.isDebugEnabled();
      if (debug) log.debug("Registering operation " + operationName + " for path " + resource.getPath());

      resource.registerOperationHandler(operationName, new OperationHandler()
      {
         @Override
         public void execute(OperationContext operationContext, ResultHandler resultHandler) throws ResourceNotFoundException, OperationException
         {
            if (debug) log.debug("Executing operation handler for annotated method " + getMethodName(method) + " for address " + operationContext.getAddress());

            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            Object[] params = new Object[parameterAnnotations.length];
            for (int i = 0; i < parameterAnnotations.length; i++)
            {
               MappedPath pathTemplate;
               MappedAttribute managedAttribute;
               MappedBy mappedBy;
               // Resolve path template and set as parameter to method
               if ((pathTemplate = getAnnotation(parameterAnnotations[i], MappedPath.class)) != null)
               {
                  params[i] = operationContext.getAddress().resolvePathTemplate(pathTemplate.value());
                  if (debug) log.debug("Resolved path template " + pathTemplate.value() + "=" + params[i]);
               }
               // Resolve attribute name and set as parameter to method
               else if ((managedAttribute = getAnnotation(parameterAnnotations[i], MappedAttribute.class)) != null)
               {
                  if (List.class == method.getParameterTypes()[i])
                  {
                     params[i] = operationContext.getAttributes().getValues(managedAttribute.value());
                  }
                  else if (String.class == method.getParameterTypes()[i])
                  {
                     params[i] = operationContext.getAttributes().getValue(managedAttribute.value());
                  }
                  else
                  {
                     throw new RuntimeException("The parameter type " + method.getParameterTypes()[i] + " cannot be annotated by @" + MappedAttribute.class.getName() + ". Only List<String> and String are allowed.");
                  }

                  if (debug) log.debug("Resolved attribute " + managedAttribute.value() + "=" + params[i]);
               }
               // Call custom mapper and set value as parameter to method
               else if ((mappedBy = getAnnotation(parameterAnnotations[i], MappedBy.class)) != null)
               {
                  MappedBy.Mapper<?> mapper;
                  try
                  {
                     mapper = mappedBy.value().newInstance();
                  }
                  catch (Exception e)
                  {
                     throw new RuntimeException("Could not create mapper class " + mappedBy.value() + " for parameter type " + method.getParameterTypes()[i], e);
                  }

                  params[i] = mapper.map(operationContext.getAddress(), operationContext.getAttributes());
               }
               else
               {
                  Class<?> marshalClass = method.getParameterTypes()[i];
                  if (debug) log.debug("Encountered unannotated parameter. Will try and find marshaller for type " + marshalClass);

                  Marshaller<?> marshaller = operationContext.getBindingProvider().getMarshaller(marshalClass, operationContext.getContentType());
                  if (marshaller != null)
                  {
                     params[i] = marshaller.unmarshal(operationContext.getAttachment(true).getStream());
                     if (debug) log.debug("Successfully unmarshaled object of type " + marshalClass);
                  }
                  else
                  {
                     throw new RuntimeException("Could not find marshaller for " + marshalClass +
                        " and therefore cannot pass parameter of this type to method " + getMethodName(method) + " for component " + componentClass);
                  }
               }
            }

            Object component = operationContext.getRuntimeContext().getRuntimeComponent(componentClass);
            if (component == null)
            {
               // try and invoke it ourselves...
               try
               {
                  component = componentClass.newInstance();
               }
               catch (Exception e)
               {
                  throw new RuntimeException("Could not create new instance of class " + componentClass, e);
               }
            }
            try
            {
               Object result = method.invoke(component, params);
               if (method.getReturnType() == void.class)
               {
                  resultHandler.completed(NoResultModel.INSTANCE);
               }
               else
               {
                  if (result == null)
                  {
                     throw new ResourceNotFoundException("Resource not found.");
                  }
                  else
                  {
                     ManagedModel managedModel = method.getAnnotation(ManagedModel.class);
                     if (managedModel != null)
                     {
                        @SuppressWarnings("unchecked")
                        ModelProvider.ModelMapper<Object> modelMapper = (ModelProvider.ModelMapper<Object>)
                           providers.getModelProvider(componentName).getModelMapper(managedModel.value());

                        if (modelMapper != null)
                        {
                           modelMapper.to(resultHandler.completed(), result);
                        }
                        else
                        {
                           throw new RuntimeException("Could not find a ModelMapper for model name " + managedModel.value() +
                              " while trying to map method " + getMethodName(method) + " for component class " + componentClass);
                        }
                     }
                     else
                     {
                        resultHandler.completed(result);
                     }
                  }
               }
            }
            catch (IllegalAccessException e)
            {
               throw new RuntimeException("Cannot access method " + method + " on object " + component, e);
            }
            catch (InvocationTargetException e)
            {
               if (e.getCause() instanceof ResourceNotFoundException)
               {
                  throw (ResourceNotFoundException) e.getCause();
               }
               else if (e.getCause() instanceof OperationException)
               {
                  throw (OperationException) e.getCause();
               }
               throw new RuntimeException("Could not invoke method " + method + " on object " + component, e);
            }
         }
      }, description(description));
   }

   private <A extends Annotation> A getAnnotation(Annotation[] annotations, Class<A> type)
   {
      for (Annotation annotation : annotations)
      {
         if (annotation.annotationType() == type) return type.cast(annotation);
      }

      return null;
   }

   private String getMethodName(Method method)
   {
      String name = method.getName();
      StringBuilder sb = new StringBuilder();
      sb.append(name).append("(");
      Class<?>[] parameters = method.getParameterTypes();
      for (int i=0; i<parameters.length; i++)
      {
         sb.append(parameters[i].getName());
         if (i != parameters.length-1)
         {
            sb.append(", ");
         }
      }
      sb.append(")");

      return sb.toString();
   }

   private ManagedDescription description(final String description)
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
