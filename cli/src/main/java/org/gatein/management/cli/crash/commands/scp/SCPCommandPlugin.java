package org.gatein.management.cli.crash.commands.scp;

import org.apache.sshd.server.Command;
import org.crsh.cmdline.ClassDescriptor;
import org.crsh.cmdline.CommandFactory;
import org.crsh.cmdline.matcher.CommandMatch;
import org.crsh.cmdline.matcher.InvocationContext;
import org.crsh.cmdline.matcher.Matcher;
import org.crsh.ssh.term.FailCommand;
import org.crsh.ssh.term.scp.CommandPlugin;
import org.crsh.ssh.term.scp.SCPAction;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class SCPCommandPlugin extends CommandPlugin
{
   @Override
   public Command createCommand(String command)
   {
      if (command.startsWith("scp "))
      {
         SCPAction action = parseScpCommand(command.substring(4));
         if (action == null) return new FailCommand("Unrecognized command " + command);

         if (Boolean.TRUE.equals(action.isSource()))
         {
            return new SourceCommand(action);
         }
         else if (Boolean.TRUE.equals(action.isSink()))
         {
            return new SinkCommand(action);
         }
      }

      return new FailCommand("Cannot execute command " + command);
   }

   private SCPAction parseScpCommand(String command)
   {
      try
      {
         SCPAction action = new SCPAction();
         ClassDescriptor<SCPAction> descriptor = CommandFactory.create(SCPAction.class);
         Matcher<SCPAction> analyzer = Matcher.createMatcher("main", descriptor);
         CommandMatch<SCPAction, ?, ?> match = analyzer.match(command);
         match.invoke(new InvocationContext(), action);

         return action;
      }
      catch (Exception e)
      {
         return null;
      }
   }
}
