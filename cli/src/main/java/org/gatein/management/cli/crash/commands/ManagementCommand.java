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

package org.gatein.management.cli.crash.commands;

import groovy.lang.Closure;
import org.crsh.cmdline.IntrospectionException;
import org.crsh.cmdline.ParameterDescriptor;
import org.crsh.cmdline.spi.Completer;
import org.crsh.command.ScriptException;
import org.gatein.management.api.ContentType;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.controller.ManagedRequest;
import org.gatein.management.api.controller.ManagedResponse;
import org.gatein.management.api.controller.ManagementController;
import org.gatein.management.api.model.ModelList;
import org.gatein.management.api.model.ModelObject;
import org.gatein.management.api.model.ModelReference;
import org.gatein.management.api.model.ModelValue;
import org.gatein.management.api.operation.OperationNames;
import org.gatein.management.api.operation.model.ReadResourceModel;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class ManagementCommand extends GateInCommand implements Completer
{
   protected ManagementCommand() throws IntrospectionException
   {
      super();
   }

   @Override
   public Map<String, Boolean> complete(ParameterDescriptor<?> parameter, String prefix) throws Exception
   {
      try
      {
         Closure assertConnected = (Closure) getProperty("assertConnected");
         assertConnected.call();
      }
      catch (ScriptException e)
      {
         return Collections.emptyMap();
      }

      Closure closure = (Closure) getProperty("begin");
      closure.call();

      try
      {
         if (String.class == parameter.getJavaType())
         {
            ManagementController controller = (ManagementController) getProperty("controller");

            PathAddress address = (PathAddress) getProperty("address");
            PathAddress relative = PathAddress.pathAddress(prefix);

            // Append to current address if not relative
            if (prefix.length() == 0 || prefix.charAt(0) != '/')
            {
               address = address.append(relative);
            }
            else // Address is absolute, set address to prefix address
            {
               prefix = prefix.substring(1);
               address = relative;
            }

            // Set the prefix to the last path element
            int index = prefix.lastIndexOf("/");
            if (index != -1)
            {
               prefix = prefix.substring(index + 1);
            }

            // If prefix is not empty, then remove last element of address, since that is the prefix
            if (prefix.length() > 0 && address.size() > 0 && prefix.charAt(prefix.length() - 1) != '/')
            {
               address = address.subAddress(0, address.size() - 1);
            }

            Set<String> children = getChildren(controller, address);
            Map<String, Boolean> completions = new HashMap<String, Boolean>(children.size());
            for (String child : children)
            {
               if (child.charAt(0) == '/') child = child.substring(1);

               // Look ahead to see if there are more children
               PathAddress nextAddress = address.append(child);
               boolean more = getChildren(controller, nextAddress).size() > 0;
               if (child.startsWith(prefix))
               {
                  String suffix = child.substring(prefix.length());
                  if (more)
                  {
                     completions.put(suffix + "/", false);
                  }
                  else
                  {
                     completions.put(suffix, true);
                  }
               }
            }

            return completions;
         }

         return Collections.emptyMap();
      }
      catch (Exception e)
      {
         return Collections.emptyMap();
      }
      finally
      {
         closure = (Closure) getProperty("end");
         closure.call();
      }
   }

   protected PathAddress getAddress(PathAddress currentAddress, String path)
   {
      PathAddress pathAddress = currentAddress;
      if (path != null)
      {
         if (path.charAt(0) == '/')
         {
            pathAddress = PathAddress.pathAddress(path);
         }
         else if (path.equals(".."))
         {
            pathAddress = pathAddress.subAddress(0, pathAddress.size() - 1);
         }
         else if (path.equals("."))
         {
         }
         else
         {
            pathAddress = pathAddress.append(path);
         }
      }

      return pathAddress;
   }

   protected Set<String> getChildren(ManagementController controller, PathAddress address)
   {
      ManagedRequest request = ManagedRequest.Factory.create(OperationNames.READ_RESOURCE, address, ContentType.JSON);
      ManagedResponse response = controller.execute(request);
      if (response != null && response.getOutcome().isSuccess())
      {
         return getChildren(response.getResult(), address);
      }

      return Collections.emptySet();
   }

   protected Set<String> getChildren(Object result, PathAddress address)
   {
      if (result instanceof ReadResourceModel)
      {
         return ((ReadResourceModel) result).getChildren();
      }
      else if (result instanceof ModelValue)
      {
         Set<String> children = new LinkedHashSet<String>();
         if (result instanceof ModelList)
         {
            for (ModelValue mv : (ModelList) result)
            {
               addChildren(children, mv, address);
            }
         }
         else if (result instanceof ModelObject)
         {
            Set<String> names = ((ModelObject) result).getNames();
            for (String name : names)
            {
               addChildren(children, ((ModelObject) result).get(name), address);
            }
         }

         return children;
      }

      return Collections.emptySet();
   }

   private static void addChildren(Set<String> children, ModelValue value, PathAddress address)
   {
      if (value.getValueType() == ModelValue.ModelValueType.REFERENCE)
      {
         ModelReference modelRef = value.asValue(ModelReference.class);
         PathAddress ref = modelRef.getValue();
         if (ref.size() > 1 && ref.subAddress(0, ref.size() - 1).equals(address))
         {
            children.add(ref.getLastElement());
         }
      }
      else if (value.getValueType() == ModelValue.ModelValueType.LIST)
      {
         ModelList list = value.asValue(ModelList.class);
         Set<String> names = new LinkedHashSet<String>(list.size());
         // If the list is a direct child and all items in the list are model references, then we can conclude that
         // these should be added to the children
         for (ModelValue mv : list)
         {
            if (mv.getValueType() == ModelValue.ModelValueType.REFERENCE)
            {
               addChildren(names, mv, address);
            }
            else
            {
               names.clear();
               break;
            }
         }
         if (!names.isEmpty())
         {
            children.addAll(names);
         }
      }
   }
}
