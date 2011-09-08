package org.gatein.management.cli.crash.commands;

import groovy.lang.Closure;
import org.crsh.cmdline.IntrospectionException;
import org.crsh.cmdline.ParameterDescriptor;
import org.crsh.cmdline.spi.Completer;
import org.crsh.command.ScriptException;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.controller.ManagedRequest;
import org.gatein.management.api.controller.ManagedResponse;
import org.gatein.management.api.controller.ManagementController;
import org.gatein.management.api.operation.OperationNames;
import org.gatein.management.api.operation.model.ReadResourceModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

   //TODO: Support children with / in the name
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
               prefix = prefix.substring(index+1);
            }

            // If prefix is not empty, then remove last element of address, since that is the prefix
            if (prefix.length() > 0 && address.size() > 0 && prefix.charAt(prefix.length()-1) != '/')
            {
               address = address.subAddress(0, address.size() - 1);
            }

            Set<String> children = getChildren(controller, address);
            Map<String, Boolean> completions = new HashMap<String, Boolean>(children.size());
            for (String child : children)
            {
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
      ManagedResponse response = controller.execute(ManagedRequest.Factory.create(OperationNames.READ_RESOURCE, address, null));
      if (response != null && response.getOutcome().isSuccess() && response.getResult() instanceof ReadResourceModel)
      {
         ReadResourceModel readResource = (ReadResourceModel) response.getResult();
         return readResource.getChildren();
      }

      return Collections.emptySet();
   }
}
