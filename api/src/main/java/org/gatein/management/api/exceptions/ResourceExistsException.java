package org.gatein.management.api.exceptions;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class ResourceExistsException extends ManagementException
{
   public ResourceExistsException(final String message)
   {
      super(message);
   }

   public ResourceExistsException(final String message, final Throwable cause)
   {
      super(message, cause);
   }
}
