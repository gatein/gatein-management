package org.gatein.management.cli.crash.commands;

import org.codehaus.groovy.tools.shell.Shell;
import org.crsh.command.CRaSHCommand;
import org.crsh.command.ScriptException;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class GateInCommand extends CRaSHCommand
{
   private static final Logger log = LoggerFactory.getLogger(GateInCommand.class);

   protected GateInCommand()
   {
   }

   protected <T> T getComponent(String containerName, Class<T> componentClass) throws ScriptException
   {
      Object container = getContainer(containerName);
      try
      {
         Method getComponentInstanceOfTypeMethod = container.getClass().getMethod("getComponentInstanceOfType", Class.class);
         return componentClass.cast(getComponentInstanceOfTypeMethod.invoke(container, componentClass));
      }
      catch (Exception e)
      {
         log.error("Exception retrieving component of type " + componentClass, e);
         return null;
      }
   }

   protected Session login(String userName, String password, String containerName) throws ScriptException
   {
      Object container = getContainer(containerName);

      // TODO: Find better way to "authenticate"
      Session session = null;
      try
      {
         Method getComponentInstanceOfTypeMethod = container.getClass().getMethod("getComponentInstanceOfType", Class.class);
         Class<?> repositoryServiceClass = Thread.currentThread().getContextClassLoader().loadClass("org.exoplatform.services.jcr.RepositoryService");
         Object repositoryService = getComponentInstanceOfTypeMethod.invoke(container, repositoryServiceClass);
         if (repositoryService != null)
         {
            Method getDefaultRepositoryMethod = repositoryService.getClass().getMethod("getDefaultRepository");
            Repository repository = (Repository) getDefaultRepositoryMethod.invoke(repositoryService);
            SimpleCredentials credentials = new SimpleCredentials(userName, password.toCharArray());
            session = repository.login(credentials, "portal-system");
         }
      }
      catch (Exception e)
      {
         log.error("Could not log into jcr repository.", e);
         throw new ScriptException("Could not authenticate for user '" + userName + "'");
      }

      if (session == null)
      {
         throw new ScriptException("Could not authenticate for user '" + userName + "'");
      }

      return session;
   }

   protected void start(String containerName)
   {
      try
      {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         Class<?> requestLifeCycleClass = cl.loadClass("org.exoplatform.container.component.RequestLifeCycle");
         Class<?> exoContainerClass = cl.loadClass("org.exoplatform.container.ExoContainer");
         Method beginMethod = requestLifeCycleClass.getMethod("begin", exoContainerClass);
         beginMethod.invoke(requestLifeCycleClass, getContainer(containerName));
      }
      catch (Exception e)
      {
         log.error("Could not start request lifecyle.", e);
         throw new ScriptException("Could not start request.");
      }
   }

   protected void end()
   {
      try
      {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         Class<?> requestLifeCycleClass = cl.loadClass("org.exoplatform.container.component.RequestLifeCycle");
         Method endMethod = requestLifeCycleClass.getMethod("end");
         endMethod.invoke(null);
      }
      catch (Exception e)
      {
         log.error("Could not end request lifecyle.", e);
         throw new ScriptException("Could not end request.");
      }
   }

   private Object getContainer(String containerName) throws ScriptException
   {
      Object container;
      try
      {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         Class<?> eXoContainerContextClass = cl.loadClass("org.exoplatform.container.ExoContainerContext");
         Method getTopContainerMethod = eXoContainerContextClass.getMethod("getTopContainer");
         container = getTopContainerMethod.invoke(null);

         if (container != null)
         {
            Method getPortalContainerMethod = container.getClass().getMethod("getPortalContainer", String.class);
            container = getPortalContainerMethod.invoke(container, containerName);
         }
      }
      catch (Exception e)
      {
         log.error("Exception obtaining kernel container.", e);
         throw new ScriptException("Could not connect, see server logs for more details.");
      }

      if (container == null) throw new ScriptException("Could not obtain portal container for container name " + containerName);

      return container;
   }
}
