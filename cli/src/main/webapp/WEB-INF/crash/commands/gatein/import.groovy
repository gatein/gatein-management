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
import org.gatein.management.cli.crash.commands.ManagementCommand
import javax.script.ScriptException
import org.crsh.command.ScriptException
import javax.script.ScriptException
import java.text.SimpleDateFormat

class importfile extends ManagementCommand
{
  private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

  @Usage("imports an exported zip file to the portal")
  @Man("""\
The import command invokes the 'import-resource' operation on the given resource.
""")
  @Command
  public Object main(@Argument String pathArg, @Argument String fileArg, @Argument List<String> attributesArg) throws ScriptException
  {
    assertConnected()

    //TODO: This logic is duplicated in mgmt exec, we should have a base class responsible for parsing common mgmt requests.
    def path = pathArg;
    def file = fileArg;
    def attributes = new HashMap<String, List<String>>();
    if (attributesArg != null)
    {
      for (String attr: attributesArg)
      {
        addAttribute(attr, attributes)
      }
    }

    if (fileArg == null)
    {
      file = pathArg;
      path = null;
    }
    else if (fileArg.contains("="))
    {
      addAttribute(fileArg, attributes)
      file = pathArg;
      path = null;
    }

    if (file == null) return "File is required.";


    def actualFile = new File(file);
    if (!actualFile.exists()) return "File $actualFile does not exist.";

    def before = address;
    def pathAddress = getAddress(address, path);

    execute(OperationNames.IMPORT_RESOURCE, pathAddress, ContentType.ZIP, attributes, new FileInputStream(actualFile), { result ->
      address = before;
      def resp = response as ManagedResponse;

      String failure = resp.outcome.failureDescription;

      return (failure != null) ? failure : "Successfully imported file $actualFile";
    });
  }

  //TODO: This is duplicated too in mgmt exec, much refactoring is needed.
  private void addAttribute(String attr, HashMap<String, List<String>> attributes)
  {
    if (attr ==~ /[^=]*=.*/)
    {
      String key = attr.substring(0, attr.indexOf('='));
      String value = attr.substring(attr.indexOf('=') + 1, attr.length());

      List<String> list = attributes.get(key);
      if (list == null)
      {
        list = new ArrayList<String>();
        attributes.put(key, list);
      }
      list.add(value);
    }
    else
    {
      throw new groovy.util.ScriptException("Invalid attribute arguement '$attr'");
    }
  }
}