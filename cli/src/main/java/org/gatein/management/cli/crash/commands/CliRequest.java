package org.gatein.management.cli.crash.commands;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.api.ContentType;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.controller.ExternalManagedRequest;
import org.gatein.management.api.controller.ManagedRequest;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class CliRequest implements ExternalManagedRequest
{
   private static final Logger log = LoggerFactory.getLogger(CliRequest.class);

   private final String  user;
   private final ManagedRequest request;
   private final RoleResolver roleResolver;
   private Locale locale;

   public CliRequest(final String user, ManagedRequest request)
   {
      this.user =user;
      this.request = request;
      this.locale = Locale.getDefault();
      this.roleResolver = new ConversationStateRoleResolver();
   }

   @Override
   public String getRemoteUser()
   {
      return user;
   }

   @Override
   public Object getRequest()
   {
      return null;
   }

   @Override
   public RoleResolver getRoleResolver()
   {
      return roleResolver;
   }

   @Override
   public String getOperationName()
   {
      return request.getOperationName();
   }

   @Override
   public PathAddress getAddress()
   {
      return request.getAddress();
   }

   @Override
   public Map<String, List<String>> getAttributes()
   {
      return request.getAttributes();
   }

   @Override
   public Locale getLocale()
   {
      return locale;
   }

   @Override
   public void setLocale(Locale locale)
   {
      this.locale = locale;
   }

   @Override
   public InputStream getDataStream()
   {
      return request.getDataStream();
   }

   @Override
   public ContentType getContentType()
   {
      return request.getContentType();
   }

   private static class ConversationStateRoleResolver implements RoleResolver {

      @Override
      public boolean isUserInRole(String role)
      {
         try
         {
            ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            Class<?> conversationStateClass = tccl.loadClass("org.exoplatform.services.security.ConversationState");
            Method getCurrent = conversationStateClass.getMethod("getCurrent");
            Object state = getCurrent.invoke(null);
            if (state != null)
            {
               Class<?> identityClass = tccl.loadClass("org.exoplatform.services.security.Identity");
               Method getIdentity = conversationStateClass.getMethod("getIdentity");
               Object identity = getIdentity.invoke(state);
               if (identity != null)
               {
                  Method getRoles = identityClass.getMethod("getRoles");
                  @SuppressWarnings("unchecked")
                  Set<String> roles = (Set<String>) getRoles.invoke(identity);
                  if (roles != null) {
                     for (String r : roles) {
                        if (role.equals(r)) {
                           return true;
                        }
                     }
                  }
               }
            }

            return false;
         }
         catch (Exception e)
         {
            log.error("Exception determining if user is in role " + role + " for CLI request", e);
            return false;
         }
      }
   }
}
