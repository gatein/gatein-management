/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.gatein.management.portalobjects.cli;

import org.gatein.management.portalobjects.cli.exporter.ExportMain;
import org.gatein.management.portalobjects.cli.importer.ImportMain;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class Main
{
   public static void main(String[] args) throws Exception
   {
      String program = getProgram(args);
      if (program == null)
      {
         program = Utils.getUserInput("Is this an 'import' or an 'export' ?", 0).toLowerCase();
      }

      String[] programArgs = new String[0];
      if (args != null && args.length > 0)
      {
         programArgs = new String[args.length-1];
         System.arraycopy(args, 1, programArgs, 0, programArgs.length);
      }
      if (program.equals("import"))
      {
         ImportMain.main(programArgs);
      }
      else if (program.equals("export"))
      {
         ExportMain.main(programArgs);
      }
      else
      {
         System.out.println("Either 'export' or 'import' must be specified as the first argument.");
         System.out.println("For example: java -jar <jar-file>.jar export --help");
      }
   }

   private static String getProgram(String[] args)
   {
      if (args == null || args.length < 1)
      {
         return null;
      }

      return args[0];
   }
}
