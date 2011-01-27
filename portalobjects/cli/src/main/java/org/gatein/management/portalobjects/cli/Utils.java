/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
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

import org.kohsuke.args4j.Option;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class Utils
{
   private Utils(){}

   public static void addPropertiesAsArgs(Class<?> clazz, Properties properties, List<String> argList, String[] fieldNames) throws Exception
   {
      for (String fieldName : fieldNames)
      {
         Field field = clazz.getDeclaredField(fieldName);
         Option option = field.getAnnotation(Option.class);
         if (option != null)
         {
            if (!containsArgument(argList, option))
            {
               String key = (option.name().startsWith("--")) ? option.name().substring(2) : option.name();
               String value = properties.getProperty("arg." + key);
               if (value != null)
               {
                  argList.add(option.name());
                  argList.add(value);
               }
            }
         }
      }
   }

   public static boolean containsArgument(List<String> argList, Option option)
   {
      List<String> options = new ArrayList<String>();
      options.add(option.name());
      options.addAll(Arrays.asList(option.aliases()));
      for (String s : options)
      {
         if (argList.contains(s)) return true;
      }
      return false;
   }

   public static String getUserInput(String text, int indentLevel)
   {
      indent(indentLevel);
      System.out.printf("%s: ", text);
      Scanner scanner = new Scanner(System.in);
      return scanner.nextLine();
   }

   public static void indent(int level)
   {
      for (int i = 0; i < level; i++)
      {
         System.out.print("   ");
      }
   }
}
