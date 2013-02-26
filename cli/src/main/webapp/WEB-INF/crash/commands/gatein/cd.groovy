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

import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Man
import org.crsh.cmdline.annotations.Usage
import org.crsh.command.ScriptException
import org.gatein.management.api.ContentType
import org.gatein.management.api.operation.OperationNames
import org.gatein.management.api.operation.model.ReadResourceModel
import org.gatein.management.cli.crash.commands.ManagementCommand

class cd extends ManagementCommand
{
  @Usage("changes the current address")
  @Man("""\
The cd command changes the current resource address the content of a managed resource. By default it lists the direct children of the resource.
""")
  @Command
  public Object main(@Argument String path) throws ScriptException
  {
    assertConnected()

    if ("..".equals(path) && address.size() == 0) return "";

    def pathAddress = getAddress(address, path);

    execute(OperationNames.READ_RESOURCE, pathAddress, ContentType.JSON, null, null, { result, error ->
      if (result == null) return "$path: no such path"

      return "";
    });
  }
}