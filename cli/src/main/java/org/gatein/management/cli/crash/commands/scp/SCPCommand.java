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
import org.gatein.management.cli.crash.plugins.CustomWebPluginLifecycle;
import org.gatein.management.cli.crash.plugins.JaasAuthenticationPlugin;

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
   private String jaasDomain;

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
         jaasDomain = CustomWebPluginLifecycle.getCrashProperties().getProperty("crash.jaas.domain", "gatein-domain");
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
      String userName = session.getAttribute(SSHLifeCycle.USERNAME);
      String password = session.getAttribute(SSHLifeCycle.PASSWORD);

      // Log in
      log.debug("Attempting to authenticate user " + userName);
      JaasAuthenticationPlugin jaas = new JaasAuthenticationPlugin();
      boolean authenticated = jaas.login(userName, password, jaasDomain);

      if (!authenticated)
      {
         throw new Exception("Could not authenticate for user " + userName);
      }

      scpManagementCommand.start(userName, containerName);
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
      if (matcher.matches())
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
      protected void start(String userName, String containerName)
      {
         super.start(userName, containerName);
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
