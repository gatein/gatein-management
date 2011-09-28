import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Man
import org.crsh.cmdline.annotations.Usage
import org.crsh.command.ScriptException
import org.gatein.management.api.operation.OperationNames
import org.gatein.management.api.operation.model.ReadResourceModel
import org.gatein.management.cli.crash.commands.ManagementCommand
import org.crsh.shell.ui.UIBuilder
import org.gatein.management.api.ContentType

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

    execute(OperationNames.READ_RESOURCE, pathAddress, ContentType.JSON, null, null, { ReadResourceModel result ->
      if (result == null) return "$path: no such path"

      def builder = new UIBuilder();

      for (def child : result.children)
      {
        if (child.charAt(0) == '/') child = child.substring(1);
        builder.node(child);
      }

      return builder;
    });
  }
}