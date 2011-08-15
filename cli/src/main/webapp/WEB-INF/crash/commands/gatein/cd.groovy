import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Man
import org.crsh.cmdline.annotations.Usage
import org.crsh.command.ScriptException
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
    def pathAddress = getAddress(address, path);

    execute(OperationNames.READ_RESOURCE, pathAddress, null, { ReadResourceModel result ->
      if (result == null) return "$path: no such path"

      return "";
    });
  }
}