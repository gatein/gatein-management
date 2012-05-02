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
import org.gatein.management.api.controller.ManagedResponse
import org.gatein.management.api.operation.OperationNames
import org.gatein.management.cli.crash.arguments.ContentTypeOption
import org.gatein.management.cli.crash.commands.ManagementCommand

class cat extends ManagementCommand
{
  @Usage("reads current resource as xml")
  @Man("""\
The cat command invokes the 'read-config-as-xml' operation on the given resource.
""")
  @Command
  public Object main(@ContentTypeOption ContentType ct,  @Argument String path) throws ScriptException
  {
    assertConnected()

    def before = address;
    def pathAddress = getAddress(address, path);

    execute(OperationNames.READ_CONFIG, pathAddress, ct, null, null, { result ->
      address = before;
      def resp = response as ManagedResponse;
      def baos = new ByteArrayOutputStream();
      resp.writeResult(baos);
      return new String(baos.toByteArray());
    });
  }
}