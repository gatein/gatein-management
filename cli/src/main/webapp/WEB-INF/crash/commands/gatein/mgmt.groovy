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
import org.crsh.command.InvocationContext
import org.crsh.command.ScriptException
import org.gatein.common.logging.LoggerFactory
import org.gatein.management.api.ContentType
import org.gatein.management.api.PathAddress
import org.gatein.management.api.controller.ManagedResponse
import org.gatein.management.api.controller.ManagementController
import org.gatein.management.api.operation.OperationNames
import org.gatein.management.api.operation.model.ReadResourceModel
import org.gatein.management.cli.crash.arguments.AttributeOption
import org.gatein.management.cli.crash.arguments.Container
import org.gatein.management.cli.crash.arguments.ContentTypeOption
import org.gatein.management.cli.crash.arguments.Input
import org.gatein.management.cli.crash.arguments.OperationOption
import org.gatein.management.cli.crash.arguments.Output
import org.gatein.management.cli.crash.arguments.Password
import org.gatein.management.cli.crash.arguments.UserName
import org.gatein.management.cli.crash.commands.ManagementCommand
import org.gatein.management.cli.crash.plugins.JaasAuthenticationPlugin

@Usage("gatein management commands")
class mgmt extends ManagementCommand
{
  @Usage("connect to the gatein management system")
  @Man("""
This command connects you into the gatein management system, allowing you to execute management operations. The default
container is 'portal' if no container option is specified.

% mgmt connect -c portal
Connect to portal container 'portal'. This is default behavior.

% mgmt connect -c sample-portal
Connect to portal container 'sample-portal'

""")
  @Command
  public Object connect(@UserName String userName, @Password String password, @Container String containerName, InvocationContext<Void, Void> ctx) throws ScriptException
  {
    if (userName != null && userName.trim().length() == 0) {
      return "User name cannot be empty"
    }

    def currentUser = user;
    if (currentUser == null) {
      currentUser = ctx.getProperty("USER");
    }
    userName = (userName == null) ? currentUser : userName;

    def currentContainer = container;
    if (currentContainer == null) {
      currentContainer = container = "portal";
    }
    containerName = (containerName == null) ? currentContainer : containerName;

    def rootContainer = org.exoplatform.container.RootContainer.getInstance()
    def portalContainer = rootContainer.getPortalContainer(containerName);

    controller = portalContainer.getComponentInstanceOfType(ManagementController.class);
    logger = LoggerFactory.getLogger("org.gatein.management.cli");

    def jaasDomain = portalContainer.realmName;
    def authenticate = (userName != currentUser) || (containerName != currentContainer);

    if (authenticate) {
      boolean auth = false;
      for (i in 1..3) {
        if (password == null) {
          password = readLine("Password for $userName: ", false);
        }
        auth = JaasAuthenticationPlugin.login(userName, password, jaasDomain);

        if (!auth) {
          password = null;
        } else {
          break;
        }
      }

      if (!auth) {
        ctx.getWriter().println("Invalid credentials for $userName");
        return disconnect();
        //return "Invalid credentials for $userName";
      }
    }

    begin = {
      start(userName, containerName);
    }

    end = {
      end();
    }

    connectionInfo = "[user=$userName, container='$containerName', host='$hostName']";
    container = containerName;
    user = userName;
    connected = true;

    execute(OperationNames.READ_RESOURCE, PathAddress.EMPTY_ADDRESS, ContentType.JSON, null, null, { ReadResourceModel result, error ->
      return "Successfully connected to gatein management system: $connectionInfo"
    });
  }

  @Usage("disconnect from management system")
  @Man("""This command disconnects from the management system""")
  @Command
  public Object disconnect() throws ScriptException
  {
    controller = null;
    connected = false;
    address = null;
    connectionInfo = null;
    return "Disconnected from management system.";
  }

  @Usage("Manually executes a management operation")
  @Man("""
This command executes management operations on the management system.

% mgmt exec /some/node read-resource
Executes the read-resource operation for address (path) /some/node

% mgmt exec read-resource foo=bar
Executes the read-resource operation of the current address (path) passing in attribute foo=bar

""")
  @Command
  public Object exec(@ContentTypeOption String contentType, @Input String input, @Output String output, @AttributeOption List<String> attributes, @Required @OperationOption String operation, @Argument String path)
  {
    assertConnected()
    def ct = (contentType == null) ? ContentType.JSON : ContentType.forName(contentType);
    if (ct == null) return "Invalid content type '$contentType'.";

    InputStream inputStream = null;
    if (input != null)
    {
      def actualFile = new File(input);
      if (!actualFile.exists()) return "Input file $actualFile does not exist.";
      inputStream = new FileInputStream(actualFile);
    }

    def outputFile = (output == null) ? null : new File(output);
    if (outputFile != null)
    {
      if (outputFile.isDirectory())
      {
        if (!outputFile.exists()) return "Directory " + outputFile + " does not exist.";
        outputFile = File.createTempFile("gatein-cli-", ".${ct.name}", outputFile);
      }
      else
      {
        if (!outputFile.getParentFile().exists()) return "Cannot write to " + outputFile.getParentFile() + " because it does not exist.";
        if (outputFile.exists()) return "Output file $outputFile already exists.";
      }
    }

    def before = address;
    def addr = getAddress(address, path);

    execute(operation, addr, ct, parseAttributes(attributes),  inputStream, { result, error ->
      address = before;
      def resp = response as ManagedResponse;

      if (outputFile == null)
      {
        def baos = new ByteArrayOutputStream();
        resp.writeResult(baos, true);

        String data = new String(baos.toByteArray());
        return (data.length() == 0) ? "Operation '$operation' at address '$addr' was successful." : data;
      }
      else
      {
        def fos = new FileOutputStream(outputFile);
        try
        {
          resp.writeResult(fos, true);
          fos.flush();
          return "Output successfully written to " + outputFile;
        }
        catch (Throwable t)
        {
          if (outputFile.exists())
          {
            outputFile.delete();
          }
          throw t;
        }
        finally
        {
          fos.close();
        }
      }
    });
  }
}
