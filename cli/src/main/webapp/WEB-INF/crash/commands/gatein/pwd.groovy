import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Man
import org.crsh.cmdline.annotations.Usage
import org.crsh.command.ScriptException
import org.gatein.management.cli.crash.commands.ManagementCommand

class pwd extends ManagementCommand
{
  @Usage("print the current path")
  @Man("""The pwd command prints the current path.""")
  @Command
  public Object main() throws ScriptException
  {
    return address;
  }
}