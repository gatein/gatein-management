package org.gatein.management.cli.crash.commands.scp;

import org.apache.sshd.common.SshConstants;
import org.apache.sshd.server.Environment;
import org.crsh.cmdline.IntrospectionException;
import org.crsh.command.ScriptException;
import org.crsh.ssh.term.AbstractCommand;
import org.crsh.ssh.term.SSHLifeCycle;
import org.crsh.ssh.term.scp.SCPAction;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.api.controller.ManagementController;
import org.gatein.management.cli.crash.commands.ManagementCommand;

import javax.jcr.Session;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public abstract class SCPCommand extends AbstractCommand implements Runnable
{
   private static final Logger log = LoggerFactory.getLogger(SCPCommand.class);

   private String path;
   private String containerName;

   private SCPManagementCommand scpManagementCommand;
   private Thread thread;

   protected SCPCommand(SCPAction action)
   {
      parseSCPAction(action);
   }

   @Override
   public void start(Environment environment) throws IOException
   {
      try
      {
         scpManagementCommand = new SCPManagementCommand();
      }
      catch (IntrospectionException e)
      {
         throw new RuntimeException(e);
      }

      thread = new Thread(this, "CRaSH");
      thread.start();
   }

   @Override
   public void destroy()
   {
      thread.interrupt();
   }

   @Override
   public void run()
   {
      int status = 0;
      String exitMessage = null;
      try
      {
         execute();
      }
      catch (Throwable t)
      {
         log.error("Exception during command execution.", t);
         status = SshConstants.SSH2_DISCONNECT_BY_APPLICATION;
         exitMessage = t.getMessage();
      }
      finally
      {
         if (callback != null) callback.onExit(status, exitMessage + "\n");
      }
   }

   private void execute() throws Exception
   {
      // Log in
      String userName = session.getAttribute(SSHLifeCycle.USERNAME);
      String password = session.getAttribute(SSHLifeCycle.PASSWORD);

      log.debug("Attempting to authenticate user " + userName);

      Session jcrSession = scpManagementCommand.login(userName, password, containerName);
      if (jcrSession == null) throw new Exception("JCR session was null.");

      scpManagementCommand.start(containerName);
      try
      {
         // Parse attributes
         Map<String, List<String>> attributes = new HashMap<String, List<String>>();
         if (path.contains("?"))
         {
            String query = path.substring(path.indexOf("?") + 1, path.length());
            path = path.substring(0, path.indexOf("?"));
            try
            {
               for (String q : query.split("&"))
               {
                  String[] param = q.split("=");
                  if (param.length != 2) throw new Exception();

                  List<String> values = attributes.get(param[0]);
                  if (values == null)
                  {
                     values = new ArrayList<String>();
                     attributes.put(param[0], values);
                  }
                  values.add(param[1]);
               }
            }
            catch (Exception e)
            {
               throw new Exception("Could not parse attribute query: " + query);
            }
         }
         ManagementController controller = scpManagementCommand.getComponent(containerName, ManagementController.class);
         execute(controller, path, attributes);
      }
      finally
      {
         scpManagementCommand.end();
         if (jcrSession.isLive())
         {
            jcrSession.logout();
         }
      }
   }

   protected abstract void execute(ManagementController controller, String path, Map<String, List<String>> attributes) throws Exception;

   protected String getFileName()
   {
      int index = path.lastIndexOf("/");
      if (index != -1)
      {
         return path.substring(index+1, path.length());
      }
      else
      {
         return path;
      }
   }

   protected void ack() throws IOException
   {
      out.write(0);
      out.flush();
   }

   protected void readAck() throws IOException
   {
      int c = in.read();
      switch (c)
      {
         case 0:
            break;
         case 1:
            log.debug("Received warning: " + readLine());
            break;
         case 2:
            throw new IOException("Received nack: " + readLine());
      }
   }

   protected String readLine() throws IOException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      while (true)
      {
         int c = in.read();
         if (c == '\n')
         {
            return baos.toString();
         }
         else if (c == -1)
         {
            throw new IOException("End of stream");
         }
         else
         {
            baos.write(c);
         }
      }
   }

   private static final String REGEX = "([\\w\\.-]*):(.*)";
   private static final Pattern PATTERN = Pattern.compile(REGEX);

   private void parseSCPAction(SCPAction action)
   {
      String target = action.getTarget();
      Matcher matcher = PATTERN.matcher(target);
      if (PATTERN.matcher(target).matches())
      {
         containerName = matcher.group(1);
         path = matcher.group(2);
      }
      else
      {
         containerName = "portal";
         path = target;
      }

      if (path.charAt(0) == '/') path = path.substring(1);
   }

   private static class SCPManagementCommand extends ManagementCommand
   {
      protected SCPManagementCommand() throws IntrospectionException
      {
         super();
      }

      @Override
      protected Session login(String userName, String password, String containerName) throws ScriptException
      {
         return super.login(userName, password, containerName);
      }

      @Override
      protected void start(String containerName)
      {
         super.start(containerName);
      }

      @Override
      protected void end()
      {
         super.end();
      }

      @Override
      protected <T> T getComponent(String containerName, Class<T> componentClass) throws ScriptException
      {
         return super.getComponent(containerName, componentClass);
      }
   }

}
