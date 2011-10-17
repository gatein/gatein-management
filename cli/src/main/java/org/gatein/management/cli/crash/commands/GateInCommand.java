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

package org.gatein.management.cli.crash.commands;

import org.crsh.cmdline.IntrospectionException;
import org.crsh.command.CRaSHCommand;
import org.crsh.command.ScriptException;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class GateInCommand extends CRaSHCommand
{
   private static final Logger log = LoggerFactory.getLogger(GateInCommand.class);

   protected GateInCommand() throws IntrospectionException
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
      try
      {
         Method getComponentInstanceOfTypeMethod = container.getClass().getMethod("getComponentInstanceOfType", Class.class);
         Class<?> repositoryServiceClass = Thread.currentThread().getContextClassLoader().loadClass("org.exoplatform.services.jcr.RepositoryService");
         Object repositoryService = getComponentInstanceOfTypeMethod.invoke(container, repositoryServiceClass);
         if (repositoryService != null)
         {
            Method getCurrentRepositoryMethod = repositoryService.getClass().getMethod("getCurrentRepository");
            Repository repository = (Repository) getCurrentRepositoryMethod.invoke(repositoryService);
            SimpleCredentials credentials = new SimpleCredentials(userName, password.toCharArray());
            Session session = repository.login(credentials, "portal-system");
            if (session == null)
            {
               throw new Exception("JCR Session was null.");
            }

            // This verifies the user has admin access to the JCR.
            session.getRootNode();

            return session;
         }
         else
         {
            throw new Exception("Repository service was null.");
         }
      }
      catch (Exception e)
      {
         throw new ScriptException("Could not authenticate for user '" + userName + "'", e);
      }
   }

   protected void start(String containerName)
   {
      try
      {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();

         // Set the current container
         Class<?> eXoContainerContextClass = cl.loadClass("org.exoplatform.container.ExoContainerContext");
         Class<?> eXoContainerClass = cl.loadClass("org.exoplatform.container.ExoContainer");
         Method setCurrentContainerMethod = eXoContainerContextClass.getMethod("setCurrentContainer", eXoContainerClass);
         setCurrentContainerMethod.invoke(eXoContainerContextClass, getContainer(containerName));

         // Start the RequestLifeCycle
         Class<?> requestLifeCycleClass = cl.loadClass("org.exoplatform.container.component.RequestLifeCycle");
         Class<?> exoContainerClass = cl.loadClass("org.exoplatform.container.ExoContainer");
         Method beginMethod = requestLifeCycleClass.getMethod("begin", exoContainerClass);
         beginMethod.invoke(requestLifeCycleClass, getContainer(containerName));
      }
      catch (Exception e)
      {
         throw new ScriptException("Could not start gatein request lifecycle.", e);
      }
   }

   protected void end()
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();

      // End RequestLifeCycle
      try
      {
         Class<?> requestLifeCycleClass = cl.loadClass("org.exoplatform.container.component.RequestLifeCycle");
         Method endMethod = requestLifeCycleClass.getMethod("end");
         endMethod.invoke(null);
      }
      catch (Exception e)
      {
         throw new ScriptException("Could not end gatein request lifecycle.", e);
      }
      finally
      {
         // Set current container to null
         try
         {
            Class<?> eXoContainerContextClass = cl.loadClass("org.exoplatform.container.ExoContainerContext");
            Class<?> eXoContainerClass = cl.loadClass("org.exoplatform.container.ExoContainer");
            Method setCurrentContainerMethod = eXoContainerContextClass.getMethod("setCurrentContainer", eXoContainerClass);
            setCurrentContainerMethod.invoke(eXoContainerContextClass, (Object) null);
         }
         catch (Throwable t)
         {
            log.error("Error while setting current container to null while ending request.");
         }
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
         throw new ScriptException("Could not obtain portal container for container name " + containerName, e);
      }

      if (container == null) throw new ScriptException("Could not obtain portal container for container name " + containerName);

      return container;
   }
}
