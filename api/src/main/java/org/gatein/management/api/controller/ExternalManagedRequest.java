package org.gatein.management.api.controller;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public interface ExternalManagedRequest extends ManagedRequest
{
   String getRemoteUser();

   Object getRequest();

   RoleResolver getRoleResolver();

   interface RoleResolver
   {
      boolean isUserInRole(String role);
   }
}
