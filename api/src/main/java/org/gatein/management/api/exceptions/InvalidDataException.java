package org.gatein.management.api.exceptions;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class InvalidDataException extends ManagementException
{
   public InvalidDataException(final String message)
   {
      super(message);
   }

   public InvalidDataException(final String message, final Throwable cause)
   {
      super(message, cause);
   }
}
