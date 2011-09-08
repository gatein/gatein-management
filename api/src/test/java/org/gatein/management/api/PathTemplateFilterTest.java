package org.gatein.management.api;

import junit.framework.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PathTemplateFilterTest
{
   @Test
   public void testFilter()
   {
      PathTemplateFilter filter = PathTemplateFilter.create("foo").include("bar").build();
      PathAddress address = PathAddress.pathAddress("some", "path");

      address.addPathTemplateResolver(new PathTemplateResolver()
      {
         @Override
         public String resolve(String templateName)
         {
            if (templateName.equals("foo")) return "bar";
            return null;
         }
      });

      Assert.assertTrue(address.accepts(filter));

      filter = PathTemplateFilter.create("foo").include("bar", "foobar").build();
      address = PathAddress.pathAddress("some", "path");

      address.addPathTemplateResolver(new PathTemplateResolver()
      {
         @Override
         public String resolve(String templateName)
         {
            if (templateName.equals("foo")) return "foobar";
            return null;
         }
      });

      Assert.assertTrue(address.accepts(filter));
   }

   @Test
   public void testFilterNoMatch()
   {
      PathTemplateFilter filter = PathTemplateFilter.create("foo").include("bar").build();
      PathAddress address = PathAddress.pathAddress("some", "path");

      address.addPathTemplateResolver(new PathTemplateResolver()
      {
         @Override
         public String resolve(String templateName)
         {
            if (templateName.equals("foo")) return "blah";
            return null;
         }
      });

      Assert.assertFalse(address.accepts(filter));

      filter = PathTemplateFilter.create("foo").include("foo").and("abc").include("123").build();
      address = PathAddress.pathAddress("some", "path");
      address.addPathTemplateResolver(new PathTemplateResolver()
      {
         @Override
         public String resolve(String templateName)
         {
            if (templateName.equals("foo")) return "blah";
            return null;
         }
      });

      Assert.assertFalse(address.accepts(filter));
   }

   @Test
   public void testFilterMultiple()
   {
      PathTemplateFilter filter = PathTemplateFilter.create("foo").include("bar").and("abc").include("123").build();
      PathAddress address = PathAddress.pathAddress("some", "path");

      address.addPathTemplateResolver(new PathTemplateResolver()
      {
         @Override
         public String resolve(String templateName)
         {
            if (templateName.equals("foo")) return "bar";
            return null;
         }
      });

      Assert.assertTrue(address.accepts(filter));

      address.addPathTemplateResolver(new PathTemplateResolver()
      {
         @Override
         public String resolve(String templateName)
         {
            if (templateName.equals("abc")) return "123";
            return null;
         }
      });

      Assert.assertTrue(address.accepts(filter));
   }

   @Test
   public void testFilterMultipleNoMatch()
   {
      PathTemplateFilter filter = PathTemplateFilter.create("foo").include("bar").and("abc").include("123").build();
      PathAddress address = PathAddress.pathAddress("some", "path");

      address.addPathTemplateResolver(new PathTemplateResolver()
      {
         @Override
         public String resolve(String templateName)
         {
            if (templateName.equals("foo")) return "bar";
            return null;
         }
      });
      address.addPathTemplateResolver(new PathTemplateResolver()
      {
         @Override
         public String resolve(String templateName)
         {
            if (templateName.equals("abc")) return "1234";
            return null;
         }
      });

      Assert.assertFalse(address.accepts(filter));
   }

   @Test
   public void testFilterExclusion()
   {
      PathTemplateFilter filter = PathTemplateFilter.create("foo").exclude("bar").build();
      PathAddress address = PathAddress.pathAddress("some", "path");
      address.addPathTemplateResolver(new PathTemplateResolver()
      {
         @Override
         public String resolve(String templateName)
         {
            if (templateName.equals("foo")) return "bar";
            return null;
         }
      });

      Assert.assertFalse(address.accepts(filter));

      address = PathAddress.pathAddress("some", "path");
      address.addPathTemplateResolver(new PathTemplateResolver()
      {
         @Override
         public String resolve(String templateName)
         {
            if (templateName.equals("foo")) return "foobar";
            return null;
         }
      });

      Assert.assertTrue(address.accepts(filter));
   }

   @Test
   public void testFilterMultipleExclusion()
   {
      PathTemplateFilter filter = PathTemplateFilter.create("foo").exclude("bar").and("abc").exclude("123").build();

      PathAddress address = PathAddress.pathAddress("some", "path");
      address.addPathTemplateResolver(new PathTemplateResolver()
      {
         @Override
         public String resolve(String templateName)
         {
            if (templateName.equals("foo")) return "bar";
            return null;
         }
      });

      Assert.assertFalse(address.accepts(filter));

      address = PathAddress.pathAddress("some", "path");
      address.addPathTemplateResolver(new PathTemplateResolver()
      {
         @Override
         public String resolve(String templateName)
         {
            if (templateName.equals("abc")) return "123";
            return null;
         }
      });

      Assert.assertFalse(address.accepts(filter));
   }

   @Test
   public void testFilterExclusionPriority()
   {
      PathTemplateFilter filter = PathTemplateFilter.create("foo").include("bar").and("abc").exclude("123").build();

      PathAddress address = PathAddress.pathAddress("blah");
      address.addPathTemplateResolver(new PathTemplateResolver()
      {
         @Override
         public String resolve(String templateName)
         {
            if (templateName.equals("foo")) return "bar";
            return null;
         }
      });

      Assert.assertTrue(address.accepts(filter));

      address.addPathTemplateResolver(new PathTemplateResolver()
      {
         @Override
         public String resolve(String templateName)
         {
            if (templateName.equals("abc")) return "123";
            return null;
         }
      });

      Assert.assertFalse(address.accepts(filter));
   }

   @Test
   public void testFilterWildcard()
   {
      PathTemplateFilter filter = PathTemplateFilter.create("foo").includeAll().build();
      PathAddress address = PathAddress.pathAddress("blah");
      address.addPathTemplateResolver(new PathTemplateResolver()
      {
         @Override
         public String resolve(String templateName)
         {
            if (templateName.equals("foo")) return "bar";
            return null;
         }
      });

      Assert.assertTrue(address.accepts(filter));
   }

   @Test
   public void testFilterNoTemplate()
   {
      PathTemplateFilter filter = PathTemplateFilter.create("foo").include("bar").build();
      PathAddress address = PathAddress.pathAddress("blah");

      Assert.assertTrue(address.accepts(filter));
   }

   @Test
   public void testNoTemplateWithExclusion()
   {
      PathTemplateFilter filter = PathTemplateFilter.create("foo").exclude("bar").build();
      PathAddress address = PathAddress.pathAddress("blah");

      Assert.assertTrue(address.accepts(filter));
   }

   @Test
   public void testParser() throws ParseException
   {
      PathTemplateFilter actual = PathTemplateFilter.parse(Collections.singletonList("foo:bar"));
      PathTemplateFilter expected = PathTemplateFilter.create("foo").include("bar").build();
      Assert.assertEquals(expected, actual);

      actual = PathTemplateFilter.parse(Collections.singletonList("foo:foo-bar,foobar"));
      expected = PathTemplateFilter.create("foo").include("foo-bar", "foobar").build();
      Assert.assertEquals(expected, actual);

      actual = PathTemplateFilter.parse(Collections.singletonList("foo:!bar"));
      expected = PathTemplateFilter.create("foo").exclude("bar").build();
      Assert.assertEquals(expected, actual);

      actual = PathTemplateFilter.parse(Collections.singletonList("foo:!foo-bar,foobar"));
      expected = PathTemplateFilter.create("foo").exclude("foo-bar", "foobar").build();
      Assert.assertEquals(expected, actual);

      actual = PathTemplateFilter.parse(Collections.singletonList(" foo : foo-bar"));
      expected = PathTemplateFilter.create("foo").include("foo-bar").build();
      Assert.assertEquals(expected, actual);

      actual = PathTemplateFilter.parse(Collections.singletonList(" foo : !foo-bar, foobar"));
      expected = PathTemplateFilter.create("foo").exclude("foo-bar", "foobar").build();
      Assert.assertEquals(expected, actual);

      actual = PathTemplateFilter.parse(Arrays.asList("foo:bar,foobar", "bar:!foo,barfoo"));
      expected = PathTemplateFilter.create("foo").include("bar", "foobar").and("bar").exclude("foo", "barfoo").build();
      Assert.assertEquals(expected, actual);
   }

   @Test
   public void testParserWithSemicolon() throws ParseException
   {
      PathTemplateFilter actual = PathTemplateFilter.parse(Collections.singletonList("foo:bar;bar:foo"));
      PathTemplateFilter expected = PathTemplateFilter.create("foo").include("bar").and("bar").include("foo").build();
      Assert.assertEquals(expected, actual);

      actual = PathTemplateFilter.parse(Collections.singletonList("foo:bar;bar:!foo;foo-bar:foobar"));
      expected = PathTemplateFilter.create("foo").include("bar").
         and("bar").exclude("foo").
         and("foo-bar").include("foobar").build();

      Assert.assertEquals(expected, actual);
   }

   @Test
   public void testAllowNullsOnParse() throws ParseException
   {
      PathTemplateFilter filter = PathTemplateFilter.parse(null);
      Assert.assertNotNull(filter);

      // No attributes means no filter, so PathAddress should accept filter.
      Assert.assertTrue(PathAddress.pathAddress("some", "path").accepts(filter));
   }
   
   @Test
   public void testAllowEmptyListOnParse() throws ParseException
   {
      PathTemplateFilter filter = PathTemplateFilter.parse(Collections.<String>emptyList());

      // No attributes means no filter, so PathAddress should accept filter.
      Assert.assertTrue(PathAddress.pathAddress("some", "path").accepts(filter));
   }

   @Test(expected = ParseException.class)
   public void testNoPathTemplateOnParse() throws ParseException
   {
      PathTemplateFilter.parse(Collections.singletonList("no-path-template"));
   }

   @Test(expected = ParseException.class)
   public void testMultipleExclusions() throws ParseException
   {
      // exclusion applies to all elements, no need for multiple !'s, hence an exception
      PathTemplateFilter.parse(Collections.singletonList("template:!first,!second"));
   }
}
