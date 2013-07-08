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

package org.gatein.management.cli.crash.plugins;

import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.PropertyDescriptor;
import org.crsh.ssh.AuthenticationPlugin;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class JaasAuthenticationPlugin extends CRaSHPlugin<AuthenticationPlugin> implements AuthenticationPlugin
{
   private static final Logger log = LoggerFactory.getLogger("org.gatein.management.cli");

   static final PropertyDescriptor<String> JAAS_DOMAIN = new PropertyDescriptor<String>(String.class, "jaas.domain", null, "The GateIn JAAS domain name used to authenticate the CLI.")
   {
      @Override
      protected String doParse(String s)
      {
         return s;
      }
   };

   @Override
   public AuthenticationPlugin getImplementation()
   {
      return this;
   }

   @Override
   protected Iterable<PropertyDescriptor<?>> createConfigurationCapabilities()
   {
      List<PropertyDescriptor<?>> list = new ArrayList<PropertyDescriptor<?>>(1);
      list.add(JAAS_DOMAIN);

      return list;
   }

   @Override
   public boolean authenticate(final String username, final String password) throws Exception
   {
      String domain = getContext().getProperty(JAAS_DOMAIN);
      return login(username, password, domain);
   }

   public static boolean login(final String username, final String password, final String domain) throws LoginException
   {
      if (domain != null)
      {
         log.debug("Will use the JAAS domain '" + domain + "' for authenticating user " + username +" into CRaSH.");
         LoginContext loginContext = new LoginContext(domain, new Subject(), new CallbackHandler()
         {
            @Override
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
            {
               for (Callback c : callbacks)
               {
                  if (c instanceof NameCallback)
                  {
                     ((NameCallback) c).setName(username);
                  }
                  else if (c instanceof PasswordCallback)
                  {
                     ((PasswordCallback) c).setPassword(password.toCharArray());
                  }
                  else
                  {
                     throw new UnsupportedCallbackException(c);
                  }
               }
            }
         });

         try
         {
            loginContext.login();
            loginContext.logout();
            return true;
         }
         catch (Exception e)
         {
            if (log.isDebugEnabled()) log.error("Exception authenticating to JAAS domain " + domain, e);
            return false;
         }
      }
      else
      {
         log.warn("The JAAS domain property '" + JAAS_DOMAIN.name + "' was not found. JAAS authentication disabled.");
         return true;
      }
   }
}
