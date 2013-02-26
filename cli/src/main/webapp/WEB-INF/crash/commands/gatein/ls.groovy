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
import org.crsh.cmdline.annotations.Option
import org.crsh.cmdline.annotations.Usage
import org.crsh.command.ScriptException
import org.crsh.shell.ui.UIBuilder
import org.gatein.management.api.ContentType
import org.gatein.management.api.PathAddress
import org.gatein.management.api.operation.OperationNames
import org.gatein.management.cli.crash.arguments.AttributeOption
import org.gatein.management.cli.crash.arguments.ContentTypeOption

import org.gatein.management.cli.crash.commands.ManagementCommand

class ls extends ManagementCommand
{
  @Usage("list the content of a node")
  @Man("""\
The ls command displays the content of a managed resource. By default it lists the direct children of the resource.
""")
  @Command
  public Object main(@Option(names = ["l", "list"]) Boolean list, @ContentTypeOption String contentType, @AttributeOption List<String> attributes, @Argument String path) throws ScriptException
  {
    assertConnected()

    def ct = (contentType == null) ? ContentType.JSON : ContentType.forName(contentType);
    def before = address;
    def pathAddress = getAddress(address, path);

    execute(OperationNames.READ_RESOURCE, pathAddress, ct, parseAttributes(attributes), null, { result, error ->
      address = before;

      if (list && pathAddress.size() > 0)
      {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.writeResult(baos, true);
        return new String(baos.toByteArray());
      }
      else
      {
        def builder = new UIBuilder();
        for (def child : getChildren((Object) result, (PathAddress) address))
        {
          if (child.charAt(0) == '/') child = child.substring(1);
          builder.node(child);
        }

        return builder;
      }
    });
  }
}