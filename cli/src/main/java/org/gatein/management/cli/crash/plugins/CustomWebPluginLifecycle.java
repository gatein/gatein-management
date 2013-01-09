package org.gatein.management.cli.crash.plugins;

import org.crsh.plugin.WebPluginLifeCycle;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import javax.servlet.ServletContextEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Used to bootstrap crash and also provide access to crash.properties data.
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class CustomWebPluginLifecycle extends WebPluginLifeCycle
{
   private static final Properties crashProperties = new Properties();
   private static final Logger log = LoggerFactory.getLogger(CustomWebPluginLifecycle.class);

   public static Properties getCrashProperties()
   {
      return crashProperties;
   }

   @Override
   public void contextInitialized(ServletContextEvent sce)
   {
      super.contextInitialized(sce);
      String path = "/WEB-INF/crash/crash.properties";
      InputStream in = sce.getServletContext().getResourceAsStream(path);
      try
      {
         crashProperties.load(in);
      }
      catch (IOException e)
      {
         log.error("Exception reading file " + path, e);
      }
      finally
      {
         if (in != null)
         {
            try
            {
               in.close();
            }
            catch (IOException e)
            {
            }
         }
      }
   }

   @Override
   public void contextDestroyed(ServletContextEvent sce)
   {
      super.contextDestroyed(sce);
      crashProperties.clear();
   }
}
