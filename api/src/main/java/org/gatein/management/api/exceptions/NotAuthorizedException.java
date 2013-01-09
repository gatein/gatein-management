package org.gatein.management.api.exceptions;

import org.gatein.management.api.ManagedUser;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class NotAuthorizedException extends OperationException
{
   private final ManagedUser user;

   public NotAuthorizedException(ManagedUser user, String operationName)
   {
      super(operationName, createMessage(user, operationName));
      this.user = user;
   }

   public NotAuthorizedException(ManagedUser user, String operationName, Throwable cause)
   {
      super(operationName, createMessage(user, operationName), cause);
      this.user = user;
   }

   public ManagedUser getUser()
   {
      return user;
   }

   private static String createMessage(ManagedUser user, String operationName)
   {
      if (user == null)
      {
         return "Authentication required for operation " + operationName;
      }

      return user.getUserName() + " is not authorized to execute operation " + operationName;
   }
}