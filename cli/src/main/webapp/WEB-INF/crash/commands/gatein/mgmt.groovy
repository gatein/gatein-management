import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Man
import org.crsh.cmdline.annotations.Usage
import org.crsh.command.ScriptException
import org.gatein.management.api.ContentType
import org.gatein.management.api.PathAddress
import org.gatein.management.api.controller.ManagedResponse
import org.gatein.management.api.controller.ManagementController
import org.gatein.management.api.operation.OperationNames
import org.gatein.management.api.operation.model.ReadResourceModel
import org.gatein.management.cli.crash.arguments.Container
import org.gatein.management.cli.crash.arguments.ContentTypeOption
import org.gatein.management.cli.crash.arguments.Password
import org.gatein.management.cli.crash.arguments.UserName
import org.gatein.management.cli.crash.commands.ManagementCommand
import org.gatein.common.logging.LoggerFactory

@Usage("gatein management commands")
class mgmt extends ManagementCommand
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
  public Object connect(@UserName String userName,
                        @Password String password,
                        @Container String containerName) throws ScriptException
  {
    if (userName != null && password == null)
    {
      password = readLine("password:", false);
    }

    if (userName == null) userName = "root";
    if (password == null) password = "gtn";
    if (containerName == null) containerName = "portal";

    session = login(userName, password, containerName);
    controller = getComponent(containerName, ManagementController.class);
    logger = LoggerFactory.getLogger("org.gatein.management.cli");

    begin = {
      start(containerName);
    }

    end = {
      end();
    }

    //TODO: Is this worthwhile ?
    host = InetAddress.getLocalHost();

    execute(OperationNames.READ_RESOURCE, PathAddress.EMPTY_ADDRESS, ContentType.JSON, null, null, { ReadResourceModel result ->
      return "Successfully connected to gatein management system: [user=$userName, container='$containerName', host='$host']"
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
    host = null;
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
  public Object exec(@ContentTypeOption String contentType, @Argument String pathArg, @Argument String operationArg, @Argument List<String> attributesArg)
  {
    def path = pathArg;
    def operation = operationArg;
    def attributes = new HashMap<String, List<String>>();

    if (attributesArg != null)
    {
      for (String attr: attributesArg)
      {
        addAttribute(attr, attributes)
      }
    }

    if (operationArg == null)
    {
      operation = pathArg;
      path = null;
    }
    else if (operationArg.contains("="))
    {
      addAttribute(operationArg, attributes)
      operation = pathArg;
      path = null;
    }

    if (operation == null) return "Operation name is required."

    def ct = (contentType == null) ? ContentType.JSON : ContentType.forName(contentType)

    def before = address;
    def addr = getAddress(address, path);

    execute(operation, addr, ct, attributes, null, { result ->
      address = before;
      def resp = response as ManagedResponse;
      def baos = new ByteArrayOutputStream();
      resp.writeResult(baos);
      return new String(baos.toByteArray());
    });
  }

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
      throw new ScriptException("Invalid attribute arguement '$attr'");
    }
  }
}