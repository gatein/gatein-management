package org.gatein.management.api;

/**
 * Interface to identify something that can be loaded and unloaded during runtime.
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public interface Module
{
   void load();

   void unload();
}
