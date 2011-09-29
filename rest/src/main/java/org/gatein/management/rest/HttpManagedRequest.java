package org.gatein.management.rest;

import org.gatein.management.api.controller.ManagedRequest;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
interface HttpManagedRequest extends ManagedRequest
{
   String getHttpMethod();
}
