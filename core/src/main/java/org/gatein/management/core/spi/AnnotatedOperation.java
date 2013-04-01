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

import org.gatein.management.api.PathAddress;
import org.gatein.management.api.RuntimeContext;
import org.gatein.management.api.annotations.Managed;
import org.gatein.management.api.annotations.ManagedContext;
import org.gatein.management.api.annotations.ManagedOperation;
import org.gatein.management.api.annotations.MappedAttribute;
import org.gatein.management.api.annotations.MappedPath;
import org.gatein.management.api.binding.Marshaller;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.model.ModelValue;
import org.gatein.management.api.operation.OperationAttachment;
import org.gatein.management.api.operation.OperationAttributes;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.OperationHandler;
import org.gatein.management.api.operation.OperationNames;
import org.gatein.management.api.operation.ResultHandler;
import org.gatein.management.api.operation.model.NoResultModel;
import org.gatein.management.core.api.AbstractManagedResource;
import org.gatein.management.core.api.model.DmrModelValue;
import org.gatein.management.core.api.operation.BasicResultHandler;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class AnnotatedOperation extends AnnotatedResource implements OperationHandler
{
   private final Method method;
   private final String methodName;

   public AnnotatedOperation(AnnotatedResource owner, Method method)
   {
      super(method.getDeclaringClass(), owner.parent);
      this.method = method;
      this.methodName = getName();
   }

   @Override
   public void register(AbstractManagedResource managedResource)
   {
      final AbstractManagedResource resource = register(managedResource, method.getAnnotation(Managed.class));
      ManagedOperation mo = method.getAnnotation(ManagedOperation.class);
      String operationName = OperationNames.READ_RESOURCE;
      String desc = "";
      if (mo != null)
      {
         operationName = mo.name();
         desc = mo.description();
      }

      final boolean debug = log.isDebugEnabled();
      if (debug) log.debug("Registering operation " + operationName + " for path " + resource.getPath());

      Class<?> returnType = method.getReturnType();
      Managed subManaged = returnType.getAnnotation(Managed.class);
      if (returnType.isAnnotationPresent(Managed.class))
      {
         if ("".equals(subManaged.value()))
         {
            AnnotatedResource ar = new AnnotatedResource(returnType, this);
            ar.register(resource);
         }
         else
         {
            throw new RuntimeException("Cannot register method " + methodName + " for class " + managedClass.getName()
               + " because return type " + returnType.getName() + " is annotated with a value for the @" + Managed.class.getSimpleName() + " annotation.");
         }
      }
      else
      {
         resource.registerOperationHandler(operationName, this, description(desc));
      }
   }

   @Override
   public void execute(OperationContext operationContext, ResultHandler resultHandler) throws ResourceNotFoundException, OperationException
   {
      final boolean debug = log.isDebugEnabled();
      if (debug) log.debug("Executing operation handler for annotated method " + methodName + " for address " + operationContext.getAddress());

      String operationName = operationContext.getOperationName();

      Annotation[][] parameterAnnotations = method.getParameterAnnotations();
      Object[] params = new Object[parameterAnnotations.length];
      OperationAttachment attachment = null;
      for (int i = 0; i < parameterAnnotations.length; i++)
      {
         MappedPath pathTemplate;
         MappedAttribute managedAttribute;
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
               throw new RuntimeException("The parameter type " + method.getParameterTypes()[i] +
                  " cannot be annotated by @" + MappedAttribute.class.getName() + ". Only List<String> and String are allowed.");
            }

            if (debug) log.debug("Resolved attribute " + managedAttribute.value() + "=" + params[i]);
         }
         // Method wants something from the OperationContext, or the entire OperationContext object.
         else if ((getAnnotation(parameterAnnotations[i], ManagedContext.class)) != null)
         {
            Class<?> parameterType = method.getParameterTypes()[i];

            if (RuntimeContext.class == parameterType)
            {
               params[i] = operationContext.getRuntimeContext();
            }
            else if (PathAddress.class == parameterType)
            {
               params[i] = operationContext.getAddress();
            }
            else if (OperationAttributes.class == parameterType)
            {
               params[i] = operationContext.getAttributes();
            }
            else if (ModelValue.class.isAssignableFrom(parameterType))
            {
               if (attachment == null)
               {
                  attachment = operationContext.getAttachment(true);
                  try
                  {
                     params[i] = DmrModelValue.readFromJsonStream(attachment.getStream());
                  }
                  catch (IOException e)
                  {
                     log.error("IOException reading from JSON stream for detyped model.", e);
                     throw new OperationException(operationName, "Could not properly read data stream. See log for more details.", e);
                  }
               }
               else
               {
                  @SuppressWarnings("unchecked")
                  Class<? extends ModelValue> type = (Class<? extends ModelValue>) parameterType;
                  params[i] = DmrModelValue.newModel().asValue(type);
               }
            }
            else if (OperationContext.class == parameterType)
            {
               params[i] = operationContext;
            }
         }
         else
         {
            Class<?> marshalClass = method.getParameterTypes()[i];
            if (debug) log.debug("Encountered unannotated parameter. Will try and find marshaller for type " + marshalClass);

            // Currently only one attachment is supported, and that's the data stream (input) of the management operation.
            if (attachment != null)
            {
               throw new RuntimeException("Cannot unmarshal " + marshalClass + " for method " + methodName +
                  " and component " + managedClass.getName() + ". This is because input stream was already consumed. " +
                  "This can happen if the marshaled type is not declared before @ManagedContext for detyped ModelValue type.");
            }
            Marshaller<?> marshaller = operationContext.getBindingProvider().getMarshaller(marshalClass, operationContext.getContentType());
            if (marshaller != null)
            {
               attachment = operationContext.getAttachment(true);
               params[i] = marshaller.unmarshal(attachment.getStream());
               if (debug) log.debug("Successfully unmarshaled object of type " + marshalClass);
            }
            else
            {
               throw new RuntimeException("Could not find marshaller for " + marshalClass +
                  " and therefore cannot pass parameter of this type to method " + methodName + " for component " + managedClass.getName());
            }
         }
      }

      Object component = null;
      if (parent == null)
      {
         component = operationContext.getRuntimeContext().getRuntimeComponent(managedClass);
      }
      else if (parent instanceof AnnotatedOperation)
      {
         AnnotatedOperation op = (AnnotatedOperation) parent;
         BasicResultHandler brh = new BasicResultHandler();
         op.execute(operationContext, brh);

         component = brh.getResult();
         if (component == null) throw new OperationException(operationName, "Cannot return null for method " + op.method + " when result is annotated with " + Managed.class);
      }
      if (component == null)
      {
         // try and invoke it ourselves...
         try
         {
            component = managedClass.newInstance();
         }
         catch (Exception e)
         {
            throw new RuntimeException("Could not create new instance of class " + managedClass.getName(), e);
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
               log.error("Result returned was null and method " + methodName + " for managed component " + managedClass + " is not void.");
               throw new ResourceNotFoundException("Resource not found.");
            }
            else
            {
               resultHandler.completed(result);
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

   private String getName()
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

   private static <A extends Annotation> A getAnnotation(Annotation[] annotations, Class<A> type)
   {
      for (Annotation annotation : annotations)
      {
         if (annotation.annotationType() == type) return type.cast(annotation);
      }

      return null;
   }
}
