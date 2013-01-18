package org.gatein.management.api.exceptions;

import org.gatein.management.api.ManagedUser;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class NotAuthorizedException extends OperationException
{
   public NotAuthorizedException(final String operationName, final String message)
   {
      super(operationName, message);
   }

   public NotAuthorizedException(final String operationName, final String message, final Throwable cause)
   {
      super(operationName, message, cause);
   }

   public NotAuthorizedException(final ManagedUser user, final String operationName)
   {
      super(operationName, createMessage(user, operationName));
   }

   public NotAuthorizedException(final ManagedUser user, final String operationName, final Throwable cause)
   {
      super(operationName, createMessage(user, operationName), cause);
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