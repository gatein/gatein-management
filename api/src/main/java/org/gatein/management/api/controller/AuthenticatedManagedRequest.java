package org.gatein.management.api.controller;

import org.gatein.management.api.ManagedUser;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public interface AuthenticatedManagedRequest extends ManagedRequest
{
   ManagedUser getUser();
}
