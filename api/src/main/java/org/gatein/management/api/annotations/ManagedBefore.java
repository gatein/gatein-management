package org.gatein.management.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method on a managed resource to be called after the managed operation is invoked.
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ManagedBefore
{
}
