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
import org.gatein.management.api.ExternalContext;
import org.gatein.management.api.ManagedUser;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.RuntimeContext;
import org.gatein.management.api.annotations.Managed;
import org.gatein.management.api.annotations.ManagedContext;
import org.gatein.management.api.annotations.ManagedOperation;
import org.gatein.management.api.annotations.ManagedRole;
import org.gatein.management.api.annotations.MappedAttribute;
import org.gatein.management.api.annotations.MappedPath;
import org.gatein.management.api.binding.Marshaller;
import org.gatein.management.api.exceptions.NotAuthorizedException;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.model.ModelProvider;
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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.gatein.management.core.spi.AnnotatedResource.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class AnnotatedOperation implements OperationHandler
{
   private static final Logger log = LoggerFactory.getLogger("org.gatein.management.core.spi");

   private final AnnotatedResource owner;
   final Method method;
   private final String methodName;
   private final String managedRole;

   public AnnotatedOperation(AnnotatedResource owner, Method method)
   {
      this.owner = owner;
      this.method = method;
      this.methodName = getName();
      ManagedRole role = method.getAnnotation(ManagedRole.class);
      managedRole = (role == null) ? null : role.value();
   }

   public void registerOperation(AbstractManagedResource managedResource)
   {
      final AbstractManagedResource resource = registerOrGetResource(managedResource, method.getAnnotation(Managed.class));
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
      if (subManaged != null)
      {
         if ("".equals(subManaged.value()))
         {
            AnnotatedResource ar = new AnnotatedResource(returnType, owner, this);
            ar.register(resource);
         }
         else
         {
            throw new RuntimeException("Cannot register method " + methodName + " for class " + owner.managedClass.getName()
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
      if (log.isDebugEnabled())
      {
         log.debug(String.format("Executing operation handler for annotated method %s for address %s", methodName, operationContext.getAddress()));
      }

      // Make sure user is authorized to invoke operation
      if (!isAuthorized(operationContext.getExternalContext(), managedRole, owner.managedRole))
      {
         throw new NotAuthorizedException(operationContext.getUser(), operationContext.getOperationName());
      }

      invokeBefore(operationContext);
      try
      {
         Object result = invokeOperation(operationContext);
         if (method.getReturnType() == void.class)
         {
            resultHandler.completed(NoResultModel.INSTANCE);
         }
         else
         {
            if (result == null)
            {
               log.error("Result returned was null and method " + methodName + " for managed component " + owner.managedClass + " is not void.");
               throw new ResourceNotFoundException("Resource not found.");
            }
            else
            {
               resultHandler.completed(result);
            }
         }
      }
      finally
      {
         invokeAfter(operationContext);
      }
   }

   Object invokeOperation(OperationContext context)
   {
      return invokeMethod(context, owner.getInstance(context),  method);
   }

   private void invokeBefore(OperationContext context)
   {
      if (owner.parent != null && owner.operation != null)
      {
         owner.operation.invokeBefore(context);
      }

      Object instance = owner.getInstance(context);
      if (owner.beforeMethod != null && instance != null)
      {
         invokeMethod(context, instance, owner.beforeMethod);
      }
   }

   private void invokeAfter(OperationContext context)
   {
      Object instance = owner.getInstance(context);
      if (owner.afterMethod != null && instance != null)
      {
         invokeMethod(context, instance, owner.afterMethod);
      }
      owner.discardInstance();

      if (owner.parent != null && owner.operation != null)
      {
         owner.operation.invokeAfter(context);
      }
   }

   private Object invokeMethod(OperationContext context, Object instance, Method method)
   {
      if (method == null || instance == null) return null;

      Object[] params = getParameters(context, method, getName(method), owner.managedClass);
      try
      {
         return method.invoke(instance, params);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException("Cannot access method " + this.method + " on object " + instance, e);
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
         throw new RuntimeException("Could not invoke method " + this.method + " on object " + instance, e);
      }
   }

   private static boolean isAuthorized(ExternalContext context, String operationRole, String resourceRole)
   {
      if (operationRole != null)
      {
         return context.isUserInRole(operationRole);
      }

      return resourceRole == null || context.isUserInRole(resourceRole);
   }

   private static Object[] getParameters(OperationContext operationContext, Method method, String methodName, Class<?> managedClass)
   {
      boolean debug = log.isDebugEnabled();
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
            else if (ManagedUser.class == parameterType)
            {
               params[i] = operationContext.getUser();
            }
            else if (ModelValue.class.isAssignableFrom(parameterType))
            {
               if ( (attachment = operationContext.getAttachment(true)) != null)
               {
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
                  throw new OperationException(operationName, "Data stream not available.");
               }
            }
            else if (ModelProvider.class.isAssignableFrom(parameterType))
            {
               @SuppressWarnings("unchecked")
               Class<? extends ModelValue> type = (Class<? extends ModelValue>) parameterType;
               params[i] = operationContext.newModel(type);
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
               if (attachment == null) throw new OperationException(operationName, "No attachment was found for this operation.");

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
      return params;
   }

   private String getName()
   {
      return getName(method);
   }

   private static String getName(Method method)
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
