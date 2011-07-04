/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.gatein.management.core.api;

import org.gatein.management.api.PathAddress;
import org.gatein.management.api.PathTemplateResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PathElement
{
   private static final String TEMPLATE_NAME_REGEX = "\\w[\\w\\.-]*";
   private static final String TEMPLATE_PARAM_REGEX_REGEX = "[^{}][^{}]*";

   private static final String TEMPLATE_PARAM_REGEX = "\\{\\s*(" + TEMPLATE_NAME_REGEX + ")\\s*(:\\s*(" + TEMPLATE_PARAM_REGEX_REGEX + "))?\\}";
   private static final Pattern TEMPLATE_PARAM_PATTERN = Pattern.compile(TEMPLATE_PARAM_REGEX);

   private static final String TEMPLATE_PARAM_DEFAULT_REGEX = "[^/]+";

   public static PathElement pathElement(String path) throws PatternSyntaxException
   {
      // logic based off resteasy for path templating
      String replacedCurlySegment = replaceEnclosedCurlyBraces(path);
      String[] split = TEMPLATE_PARAM_PATTERN.split(replacedCurlySegment);
      Matcher withPathParam = TEMPLATE_PARAM_PATTERN.matcher(replacedCurlySegment);

      int i = 0;
      StringBuilder buffer = new StringBuilder();
      if (i < split.length) buffer.append(split[i++]);

      int groupNumber = 1;
      List<Group> groups = new ArrayList<Group>();
      while (withPathParam.find())
      {
         String name = withPathParam.group(1);
         buffer.append("(");
         if (withPathParam.group(3) == null)
         {
            buffer.append(TEMPLATE_PARAM_DEFAULT_REGEX);
            groups.add(new Group(name, groupNumber++));
         }
         else
         {
            String expr = withPathParam.group(3);
            expr = recoverEnclosedCurlyBraces(expr);
            buffer.append(expr);
            groups.add(new Group(name, groupNumber++));
            groupNumber += groupCount(expr);
         }
         buffer.append(")");
         if (i < split.length) buffer.append(split[i++]);
      }

      return new PathElement(path, groups, buffer.toString());
   }

   private final String value;
   private final List<Group> groups;
   private final String regex;
   private final Pattern pattern;

   public PathElement(String value, List<Group> groups, String regex)
   {
      this.value = value;
      this.groups = groups;
      this.regex = regex;
      this.pattern = Pattern.compile(regex);
   }

   public String getValue()
   {
      return value;
   }

   public boolean isTemplate()
   {
      return !value.equals(regex);
   }

   public boolean matches(String path)
   {
      int start = 0;
      if (path.charAt(0) == '/') start++;

      Matcher matcher = pattern.matcher(path);
      matcher.region(start, path.length());
      return matcher.matches();
   }

   public String resolve(String name, String path)
   {
      Matcher matcher = pattern.matcher(path);
      int start = 0;
      if (path.charAt(0) == '/') start++;

      for (Group group : groups)
      {
         if (matcher.find(start) && group.name.equals(name))
         {
            String s = matcher.group(group.group);
            if ("".equals(s)) return null;

            return s;
         }
      }

      return null;
   }

   private static final Pattern GROUP = Pattern.compile("[^\\\\]\\(");

   private static int groupCount(String regex)
   {
      Matcher matcher = GROUP.matcher(regex);
      int count = 0;
      if (regex.startsWith("("))
      {
         count++;
      }
      while (matcher.find())
      {
         count++;
      }

      return count;
   }

   private static final char openCurlyReplacement = 6;
   private static final char closeCurlyReplacement = 7;

   private static String replaceEnclosedCurlyBraces(String str)
   {
      char[] chars = str.toCharArray();
      int open = 0;
      for (int i = 0; i < chars.length; i++)
      {
         if (chars[i] == '{')
         {
            if (open != 0) chars[i] = openCurlyReplacement;
            open++;
         }
         else if (chars[i] == '}')
         {
            open--;
            if (open != 0)
            {
               chars[i] = closeCurlyReplacement;
            }
         }
      }
      return new String(chars);
   }

   private static String recoverEnclosedCurlyBraces(String str)
   {
      return str.replace(openCurlyReplacement, '{').replace(closeCurlyReplacement, '}');
   }

   private static final class Group
   {
      private String name;
      private int group;

      private Group(String name, int group)
      {
         this.name = name;
         this.group = group;
      }

      @Override
      public String toString()
      {
         return "Group{" +
            "name='" + name + '\'' +
            ", group=" + group +
            '}';
      }
   }
}
