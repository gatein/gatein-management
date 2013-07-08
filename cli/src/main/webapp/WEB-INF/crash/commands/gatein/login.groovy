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

import org.crsh.command.ScriptException
import org.gatein.management.api.ContentType
import org.gatein.management.api.PathAddress
import org.gatein.management.api.controller.ManagedRequest
import org.gatein.management.api.exceptions.OperationException
import org.gatein.management.api.exceptions.ResourceNotFoundException

assertConnected = {
  if (!connected) throw new ScriptException("Not connected to a portal container, try executing mgmt connect first.");
};

execute = { String operationName, PathAddress pathAddress, ContentType contentType, Map<String, List<String>> attributes, InputStream data, Closure printResult ->
  assertConnected();

  if (controller == null) throw new ScriptException("Management controller not available.");

  begin();

  try
  {
    response = controller.execute(ManagedRequest.Factory.create(operationName, pathAddress, attributes, data, contentType));
    if (response == null) return "No response for path $address";

    if (response.outcome.isSuccess())
    {
      address = pathAddress;
      return printResult(response.result, null);
    }
    else
    {
      return printResult(null, response.result);
    }
  }
  catch (ResourceNotFoundException e)
  {
    logger.error("Resource not found for address $pathAddress.", e);
    return "No resource found for path '$pathAddress'";
  }
  catch (OperationException e)
  {
    logger.error("Operation exception for operation $operationName and address $pathAddress", e);
    return "Operation exception for operation " + e.getOperationName() + ", Message: " + e.getMessage();
  }
  catch (Exception e)
  {
    throw new ScriptException("Management request failed for operation $operationName and path '$pathAddress'", e);
  }
  finally
  {
    end();
  }
};

parseAttributes = { List<String> attributes ->
  def map = [:] as Map<String, List<String>>;
  for (attr in attributes)
  {
    if (attr ==~ /[^=]*=.*/)
    {
      String key = attr.substring(0, attr.indexOf('='));
      String value = attr.substring(attr.indexOf('=') + 1, attr.length());

      List<String> list = map[key];
      if (list == null)
      {
        list = new ArrayList<String>();
        map[key] = list;
      }
      list.add(value);
    }
    else
    {
      throw new ScriptException("Invalid attribute arguement '$attr'");
    }
  }

  return map;
}