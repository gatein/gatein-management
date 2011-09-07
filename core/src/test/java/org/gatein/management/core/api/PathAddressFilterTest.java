package org.gatein.management.core.api;

import junit.framework.Assert;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.PathTemplateResolver;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PathAddressFilterTest
{
   @Test
   public void testPathTemplateFilter() throws Exception
   {
      String filterString = "foo:bar,foo-bar";
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
      PathAddressFilter filter = PathAddressFilter.parse(Collections.singletonList(filterString));
      Assert.assertTrue(filter.accept(address));

      address = PathAddress.pathAddress("blah");
      address.addPathTemplateResolver(new PathTemplateResolver()
      {
         @Override
         public String resolve(String templateName)
         {
            if (templateName.equals("foo")) return "foo-bar";
            return null;
         }
      });
      filter = PathAddressFilter.parse(Collections.singletonList(filterString));
      Assert.assertTrue(filter.accept(address));
   }

   @Test
   public void testPathTemplateFilterExclusion() throws Exception
   {
      String filterString = "foo:!bar,foo-bar";
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
      PathAddressFilter filter = PathAddressFilter.parse(Collections.singletonList(filterString));
      Assert.assertFalse(filter.accept(address));

      address = PathAddress.pathAddress("blah");
      address.addPathTemplateResolver(new PathTemplateResolver()
      {
         @Override
         public String resolve(String templateName)
         {
            if (templateName.equals("foo")) return "foo-bar";
            return null;
         }
      });
      filter = PathAddressFilter.parse(Collections.singletonList(filterString));
      Assert.assertFalse(filter.accept(address));
   }

   @Test
   public void testPathTemplateFilterMultiple() throws Exception
   {
      List<String> filterStrings = Arrays.asList("foo:bar", "abc:123");
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
      address.addPathTemplateResolver(new PathTemplateResolver()
      {
         @Override
         public String resolve(String templateName)
         {
            if (templateName.equals("abc")) return "123";
            return null;
         }
      });

      Assert.assertTrue(PathAddressFilter.parse(filterStrings).accept(address));
   }

   @Test
   public void testPathTemplateFilterExclusionPriority() throws Exception
   {
      List<String> filterStrings = Arrays.asList("foo:bar", "abc:!123");
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
      address.addPathTemplateResolver(new PathTemplateResolver()
      {
         @Override
         public String resolve(String templateName)
         {
            if (templateName.equals("abc")) return "123";
            return null;
         }
      });

      Assert.assertFalse(PathAddressFilter.parse(filterStrings).accept(address));
   }

   @Test
   public void testPathTemplateFilterWildcard() throws Exception
   {
      String filter = "foo:*";
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

      Assert.assertTrue(PathAddressFilter.parse(Collections.singletonList(filter)).accept(address));
   }


   @Test
   public void testPathFilter() throws Exception
   {
      String filterString = "bar,foo-bar,path/no/leading-slash,/path/with/leading-slash";

      PathAddress address = PathAddress.pathAddress("bar");
      PathAddressFilter filter = PathAddressFilter.parse(Collections.singletonList(filterString));
      Assert.assertTrue(filter.accept(address));

      address = PathAddress.pathAddress("foo-bar");
      filter = PathAddressFilter.parse(Collections.singletonList(filterString));
      Assert.assertTrue(filter.accept(address));

      address = PathAddress.pathAddress("path/no/leading-slash");
      filter = PathAddressFilter.parse(Collections.singletonList(filterString));
      Assert.assertTrue(filter.accept(address));

      address = PathAddress.pathAddress("/path/with/leading-slash");
      filter = PathAddressFilter.parse(Collections.singletonList(filterString));
      Assert.assertTrue(filter.accept(address));
   }

   @Test
   public void testPathFilterExclusion() throws Exception
   {
      String filterString = "!bar";

      PathAddress address = PathAddress.pathAddress("bar");
      PathAddressFilter filter = PathAddressFilter.parse(Collections.singletonList(filterString));
      Assert.assertFalse(filter.accept(address));
   }

   @Test
   public void testPathFilterExclusionPriority() throws Exception
   {
      List<String> filterStrings = Arrays.asList("foo", "!bar", "blah");
      PathAddress address = PathAddress.pathAddress("bar");
      Assert.assertFalse(PathAddressFilter.parse(filterStrings).accept(address));
   }

   @Test
   public void testPathFilterMultiple() throws Exception
   {
      List<String> filterStrings = Arrays.asList("foo", "bar", "/foo/bar");

      PathAddressFilter filter = PathAddressFilter.parse(filterStrings);
      Assert.assertTrue(filter.accept(PathAddress.pathAddress("foo")));
      Assert.assertTrue(filter.accept(PathAddress.pathAddress("bar")));
      Assert.assertTrue(filter.accept(PathAddress.pathAddress("/foo/bar")));
   }

   @Test
   public void testMixedFilter() throws Exception
   {
      List<String> filterStrings = Arrays.asList("foo:bar", "blah", "foo/bar");
      PathAddressFilter filter = PathAddressFilter.parse(filterStrings);

      Assert.assertTrue(filter.accept(PathAddress.pathAddress("blah")));
      Assert.assertTrue(filter.accept(PathAddress.pathAddress("foo/bar")));

      PathAddress address = PathAddress.pathAddress("some", "address");
      address.addPathTemplateResolver(new PathTemplateResolver()
      {
         @Override
         public String resolve(String templateName)
         {
            if (templateName.equals("foo")) return "bar";
            return null;
         }
      });
      Assert.assertTrue(filter.accept(address));
   }

   @Test
   public void testPathFilterWildcard() throws Exception
   {
      String filter = "*";
      PathAddress address = PathAddress.pathAddress("blah", "foo", "bar");

      Assert.assertTrue(PathAddressFilter.parse(Collections.singletonList(filter)).accept(address));

      filter = "!*";
      address = PathAddress.pathAddress("blah", "foo", "bar");

      Assert.assertFalse(PathAddressFilter.parse(Collections.singletonList(filter)).accept(address));
   }
}
