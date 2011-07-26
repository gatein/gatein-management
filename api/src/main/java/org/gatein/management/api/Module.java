package org.gatein.management.api;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public interface Module
{
   void load();

   void unload();
}
