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
import org.crsh.cmdline.annotations.Required
import org.crsh.cmdline.annotations.Usage
import org.gatein.management.api.ContentType
import org.gatein.management.api.controller.ManagedResponse
import org.gatein.management.api.operation.OperationNames
import org.gatein.management.cli.crash.arguments.FileOption
import org.gatein.management.cli.crash.arguments.ImportModeOption
import org.gatein.management.cli.crash.arguments.ImportModeOption.ImportModeCompleter
import org.gatein.management.cli.crash.commands.ManagementCommand

import java.text.SimpleDateFormat
import javax.script.ScriptException

class importfile extends ManagementCommand
{
  private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

  @Usage("imports an exported zip file to the portal")
  @Man("""\
The import command invokes the 'import-resource' operation on the given resource.
""")
  @Command
  public Object main(@Required @FileOption String file, @ImportModeOption String importMode, @Argument String path) throws ScriptException
  {
    assertConnected()
    def actualFile = new File(file);
    if (!actualFile.exists()) return "File $actualFile does not exist.";

    def before = address;
    def pathAddress = getAddress(address, path);

    if (importMode != null && ! ImportModeCompleter.modes.contains(importMode))
    {
      return "Invalid import mode. Valid values are: " + ImportModeCompleter.modes;
    }

    def attributes = [:]
    if (importMode != null) attributes["importMode"] = [importMode];

    execute(OperationNames.IMPORT_RESOURCE, pathAddress, ContentType.ZIP, attributes, new FileInputStream(actualFile), { result, error ->
      address = before;

      if (error != null)
      {
        def resp = response as ManagedResponse;
        def baos = new ByteArrayOutputStream();
        resp.writeResult(baos, true);
        return new String(baos.toByteArray());
      }
      else
      {
        "Successfully imported file $actualFile";
      }
    });
  }
}