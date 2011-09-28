import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Man
import org.crsh.cmdline.annotations.Usage
import org.crsh.command.ScriptException
import org.crsh.shell.ui.UIBuilder
import org.gatein.management.api.PathAddress
import org.gatein.management.api.operation.OperationNames
import org.gatein.management.api.operation.model.ReadResourceModel
import org.gatein.management.cli.crash.commands.ManagementCommand

class ls extends ManagementCommand
{
  @Usage("list the content of a node")
  @Man("""\
The ls command displays the content of a managed resource. By default it lists the direct children of the resource.
""")
  @Command
  public Object main(@Argument String path) throws ScriptException
  {
    def pathAddress = getAddress(address, path);

    execute(OperationNames.READ_RESOURCE, pathAddress, null, null, null, { ReadResourceModel result ->
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