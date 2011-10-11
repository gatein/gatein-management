package org.gatein.management.cli.crash.arguments;

import org.crsh.cmdline.annotations.Man;
import org.crsh.cmdline.annotations.Option;
import org.crsh.cmdline.annotations.Usage;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
@Retention(RetentionPolicy.RUNTIME)
@Option(names = {"r", "filter"})
@Usage("Specifies the value of the filter to use during an export for example.")
@Man("Specifies the filter attribute to send as part of management request. ie: -f path-var:path")
public @interface FilterOption
{
}
