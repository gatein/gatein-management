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
@Option(names = {"u", "username"})
@Usage("the user name")
@Man("The user name")
public @interface UserName
{
}
