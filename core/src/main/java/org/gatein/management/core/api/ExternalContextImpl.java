package org.gatein.management.core.api;

import org.gatein.management.api.ExternalContext;
import org.gatein.management.api.controller.ExternalManagedRequest;
import org.gatein.management.api.controller.ManagedRequest;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class ExternalContextImpl implements ExternalContext
{
   private final Object request;
   private final String user;
   private final ExternalManagedRequest.RoleResolver roleResolver;

   public ExternalContextImpl(ManagedRequest request)
   {
      if (request instanceof ExternalManagedRequest)
      {
         ExternalManagedRequest req = (ExternalManagedRequest) request;
         this.request = req.getRequest();
         this.user = req.getRemoteUser();
         this.roleResolver = req.getRoleResolver();
      }
      else
      {
         this.request = null;
         this.user = null;
         this.roleResolver = null;
      }
   }

   @Override
   public Object getRequest()
   {
      return request;
   }

   @Override
   public String getRemoteUser()
   {
      return user;
   }

   @Override
   public boolean isUserInRole(String role)
   {
      return roleResolver != null && roleResolver.isUserInRole(role);
   }
}
