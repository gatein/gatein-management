package org.gatein.management.api;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public interface FilteredAddress
{
   /**
    * Indicates if the filter applied should filter the address.  For example this should return false if a path template
    * variable indicated during filtering does not resolve to anything.
    * @return true if the address should be filtered.
    */
   abstract boolean isFiltered();

   /**
    * Indicates whether or not the filter matches.  This will only be called if the <code>isFiltered</code> method returns true.
    * @return true if the filter matches
    */
   abstract boolean matches();
}
