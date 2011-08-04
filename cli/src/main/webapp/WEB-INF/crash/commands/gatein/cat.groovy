import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Man
import org.crsh.cmdline.annotations.Usage
import org.crsh.command.ScriptException
import org.gatein.management.api.ContentType
import org.gatein.management.api.controller.ManagedResponse
import org.gatein.management.api.operation.OperationNames
import org.gatein.management.cli.crash.commands.ManagementCommand

class cat extends ManagementCommand
{
  @Usage("reads current resource as xml")
  @Man("""\
The cat command invokes the 'read-config-as-xml' operation on the given resource.
""")
  @Command
  public Object main(@Argument String path) throws ScriptException
  {
    def before = address;
    def pathAddress = getAddress(address, path);

    execute(OperationNames.READ_CONFIG_AS_XML, pathAddress, ContentType.XML, { result ->
      address = before;
      def resp = response as ManagedResponse;
      def baos = new ByteArrayOutputStream();
      resp.writeResult(baos);
      return new String(baos.toByteArray());
    });
  }
}