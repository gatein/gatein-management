package org.gatein.management.api;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public interface ExternalContext
{
   /**
    * Returns the the external environment-specific request, i.e. a servlet request
    *
    * @return the external request
    */
   Object getRequest();

   /**
    * Returns the remote user of the external environment-specific request
    *
    * @return the remote user of the request
    */
   String getRemoteUser();

   /**
    * Returns a boolean indicating whether the user is included in the specified logical "role". If user is not
    * authenticated this returns false.
    *
    * @param role the name of the role
    * @return true if the user belongs to a given role or false if user is not authenticated
    */
   boolean isUserInRole(String role);
}
