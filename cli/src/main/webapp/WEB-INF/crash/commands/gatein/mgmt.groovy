import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Man
import org.crsh.cmdline.annotations.Usage
import org.crsh.command.ScriptException
import org.gatein.management.api.PathAddress
import org.gatein.management.api.controller.ManagementController
import org.gatein.management.api.operation.OperationNames
import org.gatein.management.api.operation.model.ReadResourceModel
import org.gatein.management.cli.crash.arguments.Container
import org.gatein.management.cli.crash.arguments.Password
import org.gatein.management.cli.crash.arguments.UserName
import org.gatein.management.cli.crash.commands.GateInCommand

@Usage("gatein management commands")
class mgmt extends GateInCommand
{
  @Usage("login to gatein management")
  @Man("""
This command logs you into a gatein managed component, allowing you to execute management operations.

% mgmt login -c portal mop
Connect to the MOP managed component.

% mgmt login -c portal -u root -p gtn mop
Connect to the MOP managed component using the username 'root' and password 'gtn'.  This is the default.

""")
  @Command
  Object connect(@UserName String userName,
                 @Password String password,
                 @Container String containerName) throws ScriptException
  {
    if (userName == null) userName = "root";
    if (password == null) password = "gtn";
    if (containerName == null) containerName = "portal";

    session = login(userName, password, containerName);
    controller = getComponent(containerName, ManagementController.class);

    begin = {
      start(containerName);
    }

    end = {
      end();
    }

    execute(OperationNames.READ_RESOURCE, PathAddress.EMPTY_ADDRESS, null, { ReadResourceModel result ->
      return "Successfully connected !"
    });
  }

  @Usage("disconnect from management system")
  @Man("""This command disconnects from the management system""")
  @Command
  public Object disconnect() throws ScriptException
  {
    assertConnected();
    session.logout();
    session = null;
    controller = null;
    address = null;
    container = null;
    return "Disconnected from management system";
  }
}