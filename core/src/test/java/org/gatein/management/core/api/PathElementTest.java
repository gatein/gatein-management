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

import org.junit.Test;

import java.text.ParseException;
import java.util.regex.PatternSyntaxException;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PathElementTest
{
   @Test
   public void testHasTemplate() throws ParseException
   {
      assertTrue(PathElement.pathElement("{foo-bar}").isTemplate());
      assertTrue(PathElement.pathElement("{foo-bar: .*}").isTemplate());
      assertTrue(PathElement.pathElement("{foo}bar").isTemplate());
      assertTrue(PathElement.pathElement("{foo: .*}bar").isTemplate());
      assertFalse(PathElement.pathElement("foo-bar").isTemplate());
   }

   @Test
   public void testMatches() throws ParseException
   {
      assertTrue(PathElement.pathElement("{path}").matches("foo"));
      assertTrue(PathElement.pathElement("{path}bar").matches("foo-foobar"));

      assertTrue(PathElement.pathElement("{path: .*}").matches("foo/bar"));
      assertTrue(PathElement.pathElement("{path: .*}/bar").matches("foo/foo-bar/bar"));
      assertFalse(PathElement.pathElement("{path: .*}/bar").matches("foo/foo-bar/"));

      assertTrue(PathElement.pathElement("{alphapath: [a-zA-Z]{3}}/{numpath: [0-9]*}").matches("foo/123"));
      assertFalse(PathElement.pathElement("{alphapath: [a-zA-Z]{3}}/{numpath: [0-9]*}").matches("foobar/123"));
      assertFalse(PathElement.pathElement("{alphapath: [a-zA-Z]{3}}/{numpath: [0-9]*}").matches("foo/a123"));

      assertTrue(PathElement.pathElement("{weird: \\{foo\\}\\/\\{bar\\}}").matches("{foo}/{bar}"));
      assertFalse(PathElement.pathElement("{weird: \\{foo\\}\\/\\{bar\\}}").matches("foo/bar"));

      assertTrue(PathElement.pathElement("foo/{required}/{optional: .*}").matches("foo/bar/"));
      assertFalse(PathElement.pathElement("foo/{bar}/{required}").matches("foo/bar"));
   }

   @Test
   public void testResolve() throws ParseException
   {
      assertEquals("foo", PathElement.pathElement("{path}").resolve("path", "/foo"));
      assertEquals("foo", PathElement.pathElement("{path}").resolve("path", "foo"));
      assertEquals("foo-foo", PathElement.pathElement("{path}bar").resolve("path", "foo-foobar"));

      assertEquals("foo/bar", PathElement.pathElement("{path: .*}").resolve("path", "/foo/bar"));
      assertEquals("foo/foo-bar", PathElement.pathElement("{path: .*}/bar").resolve("path", "foo/foo-bar/bar"));

      assertEquals("foo", PathElement.pathElement("{alphapath: [a-zA-Z]{3}}/{numpath: [0-9]*}").resolve("alphapath", "foo/123"));
      assertEquals("123", PathElement.pathElement("{alphapath: [a-zA-Z]{3}}/{numpath: [0-9]*}").resolve("numpath", "foo/123"));

      assertEquals("{foo}/{bar}", PathElement.pathElement("something/{weird: \\{foo\\}\\/\\{bar\\}}/else").resolve("weird", "something/{foo}/{bar}/else"));

      assertEquals("bar", PathElement.pathElement("foo/{required}/{optional: .*}").resolve("required", "foo/bar/"));
      assertNull(PathElement.pathElement("foo/{required}/{optional: .*}").resolve("optional", "foo/bar"));
      assertEquals("blah", PathElement.pathElement("foo/{required}/{optional: .*}").resolve("optional", "foo/bar/blah"));
   }

   @Test
   public void testBadTemplateExpression()
   {
      try
      {
         PathElement.pathElement("{bad *}");
         fail();
      }
      catch (PatternSyntaxException e)
      {
      }

      try
      {
         PathElement.pathElement("{foo: *}/something/{bad: {{{}");
         fail();
      }
      catch (PatternSyntaxException e)
      {
      }
   }
}
