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

import org.gatein.management.api.ManagedDescription;
import org.gatein.management.api.ManagedResource;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.RuntimeContext;
import org.gatein.management.api.operation.OperationHandler;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class SimpleManagedResourceTest implements RuntimeContext.Factory
{
   private RuntimeContext runtimeContext;

   @Before
   public void init()
   {
      runtimeContext = mock(RuntimeContext.class);
   }

   @Test
   public void rootRegistration()
   {
      SimpleManagedResource root = createRootResource();
      assertEquals(ROOT_DESC.getDescription(), root.getResourceDescription(PathAddress.EMPTY_ADDRESS).getDescription());
   }

   @Test
   public void testSubResourceRegistration()
   {
      SimpleManagedResource root = createRootResource();

      SimpleManagedResource a = (SimpleManagedResource) root.registerSubResource("a", A_DESC);
      SimpleManagedResource a_1 = (SimpleManagedResource) a.registerSubResource("a-1", A_1_DESC);
      SimpleManagedResource a_1_1 = (SimpleManagedResource) a_1.registerSubResource("a-1-1", A_1_1_DESC);
      SimpleManagedResource a_2 = (SimpleManagedResource) a.registerSubResource("a-2", A_2_DESC);

      SimpleManagedResource b = (SimpleManagedResource) root.registerSubResource("b", B_DESC);

      SimpleManagedResource c = (SimpleManagedResource) root.registerSubResource("c", C_DESC);
      SimpleManagedResource c1 = (SimpleManagedResource) c.registerSubResource("c-1", C_1_DESC);


      assertNotNull(root.getSubResource(PathAddress.pathAddress("a")));
      assertNotNull(root.getSubResource(PathAddress.pathAddress("a", "a-1")));
      assertNotNull(root.getSubResource(PathAddress.pathAddress("a", "a-1", "a-1-1")));
      assertNotNull(root.getSubResource(PathAddress.pathAddress("a", "a-2")));
      assertNotNull(root.getSubResource(PathAddress.pathAddress("b")));
      assertNotNull(root.getSubResource(PathAddress.pathAddress("c")));
      assertNotNull(root.getSubResource(PathAddress.pathAddress("c", "c-1")));
   }

   @Test
   public void testPathTemplate()
   {
      SimpleManagedResource root = createRootResource();
      ManagedResource.Registration foo = root.registerSubResource("foo", description("foo description"));

      foo.registerSubResource("bar/{name: [a-zA-Z]*}", description("bar description"));
      ManagedResource.Registration barname = foo.registerSubResource("bar/{name: [a-zA-Z]*}/foo/{param: .*}", description("bar name description"));
      barname.registerSubResource("child", description("bar name child description"));

      PathAddress address = PathAddress.pathAddress("foo", "bar", "nick");
      assertNotNull(root.getSubResource(address));
      assertNotNull(root.getResourceDescription(address));
      assertEquals("bar description", root.getResourceDescription(address).getDescription());

      address = PathAddress.pathAddress("foo", "bar", "nick", "foo", "blah");
      assertNotNull(root.getSubResource(address));
      assertNotNull(root.getResourceDescription(address));
      assertEquals("bar name description", root.getResourceDescription(address).getDescription());

      address = PathAddress.pathAddress("foo", "bar", "nick", "foo", "blah", "child");
      assertNotNull(root.getSubResource(address));
      assertNotNull(root.getResourceDescription(address));
      assertEquals("bar name child description", root.getResourceDescription(address).getDescription());
   }

   @Test
   public void testPathTemplate2()
   {
      SimpleManagedResource root = createRootResource();
      ManagedResource.Registration foo = root.registerSubResource("{foo-param: [a-zA-Z]*}", description("foo description"));
      ManagedResource.Registration bar1 = foo.registerSubResource("bar1", description("bar 1 description"));
      bar1.registerSubResource("foo1", description("foo 1 description"));

      ManagedResource.Registration bar2 = foo.registerSubResource("bar2", description("bar 2 description"));
      bar2.registerSubResource("{bar-param: [0-9]*}", description("bar param description"));

      PathAddress address = PathAddress.pathAddress("foo");
      assertNotNull(root.getSubResource(address));
      assertNotNull(root.getResourceDescription(address));
      assertEquals("foo description", root.getResourceDescription(address).getDescription());

      address = PathAddress.pathAddress("foo", "bar1");
      assertNotNull(root.getSubResource(address));
      assertNotNull(root.getResourceDescription(address));
      assertEquals("bar 1 description", root.getResourceDescription(address).getDescription());

      address = PathAddress.pathAddress("foo", "bar1", "foo1");
      assertNotNull(root.getSubResource(address));
      assertNotNull(root.getResourceDescription(address));
      assertEquals("foo 1 description", root.getResourceDescription(address).getDescription());

      address = PathAddress.pathAddress("foo", "bar2");
      assertNotNull(root.getSubResource(address));
      assertNotNull(root.getResourceDescription(address));
      assertEquals("bar 2 description", root.getResourceDescription(address).getDescription());

      address = PathAddress.pathAddress("foo", "bar2", "123");
      assertNotNull(root.getSubResource(address));
      assertNotNull(root.getResourceDescription(address));
      assertEquals("bar param description", root.getResourceDescription(address).getDescription());
   }

   @Test
   public void testPathTemplateMop()
   {
      OperationHandler globalOh = mock(OperationHandler.class);
      OperationHandler siteTypesOh = mock(OperationHandler.class);
      OperationHandler pagesOh = mock(OperationHandler.class);
      OperationHandler navOh = mock(OperationHandler.class);

      SimpleManagedResource root = createRootResource();
      root.registerOperationHandler("read-resource", globalOh, description("global read resource"), true);

      ManagedResource.Registration mop = root.registerSubResource("mop", description("mop description"));
      ManagedResource.Registration sitetypes = mop.registerSubResource("{site-type}sites", description("site type description"));
      sitetypes.registerOperationHandler("read-resource", siteTypesOh, description("site types read resource description"));

      ManagedResource.Registration sites = sitetypes.registerSubResource("{site-name: [-_\\w\\/]*}", description("site names description"));

      ManagedResource.Registration pages = sites.registerSubResource("pages", description("page description"));
      pages.registerOperationHandler("read-resource", pagesOh, description("page read resource description"));

      ManagedResource.Registration navigation = sites.registerSubResource("navigation", description("nav description"));
      navigation.registerOperationHandler("read-resource", navOh, description("nav read resource description"));

      navigation.registerSubResource("{nav-uri: .*}", description("nav uri description"));

      PathAddress address = PathAddress.pathAddress("mop/portalsites");
      assertNotNull(root.getSubResource(address));
      assertEquals("portal", address.resolvePathTemplate("site-type"));
      assertEquals("site type description", root.getResourceDescription(address).getDescription());
      assertNotNull(root.getOperationHandler(address, "read-resource"));
      assertEquals(siteTypesOh, root.getOperationHandler(address, "read-resource"));

      address = PathAddress.pathAddress("mop/portalsites/classic");
      assertNotNull(root.getSubResource(address));
      assertEquals("portal", address.resolvePathTemplate("site-type"));
      assertEquals("classic", address.resolvePathTemplate("site-name"));
      assertEquals("site names description", root.getResourceDescription(address).getDescription());
      assertNotNull(root.getOperationHandler(address, "read-resource"));
      assertEquals(globalOh, root.getOperationHandler(address, "read-resource"));

      address = PathAddress.pathAddress("mop/groupsites/platform/administrators");
      assertNotNull(root.getSubResource(address));
      assertEquals("group", address.resolvePathTemplate("site-type"));
      assertEquals("platform/administrators", address.resolvePathTemplate("site-name"));
      assertEquals("site names description", root.getResourceDescription(address).getDescription());
      assertNotNull(root.getOperationHandler(address, "read-resource"));
      assertEquals(globalOh, root.getOperationHandler(address, "read-resource"));

      address = PathAddress.pathAddress("mop/groupsites/organization/management/executive-board");
      assertNotNull(root.getSubResource(address));
      assertEquals("group", address.resolvePathTemplate("site-type"));
      assertEquals("organization/management/executive-board", address.resolvePathTemplate("site-name"));
      assertEquals("site names description", root.getResourceDescription(address).getDescription());
      assertNotNull(root.getOperationHandler(address, "read-resource"));
      assertEquals(globalOh, root.getOperationHandler(address, "read-resource"));

      address = PathAddress.pathAddress("mop/groupsites/platform/administrators/pages");
      assertNotNull(root.getSubResource(address));
      assertEquals("group", address.resolvePathTemplate("site-type"));
      assertEquals("platform/administrators", address.resolvePathTemplate("site-name"));
      assertEquals("page description", root.getResourceDescription(address).getDescription());
      assertNotNull(root.getOperationHandler(address, "read-resource"));
      assertEquals(pagesOh, root.getOperationHandler(address, "read-resource"));

      address = PathAddress.pathAddress("mop/portalsites/classic/navigation");
      assertNotNull(root.getSubResource(address));
      assertEquals("nav description", root.getResourceDescription(address).getDescription());

      address = PathAddress.pathAddress("mop/portalsites/classic/navigation/foo");
      assertNotNull(root.getSubResource(address));
      assertEquals("nav uri description", root.getResourceDescription(address).getDescription());
      assertEquals("foo", address.resolvePathTemplate("nav-uri"));

      address = PathAddress.pathAddress("mop/portalsites/classic/navigation/foo-bar/blah");
      assertNotNull(root.getSubResource(address));
      assertEquals("nav uri description", root.getResourceDescription(address).getDescription());
      assertEquals("foo-bar/blah", address.resolvePathTemplate("nav-uri"));
   }

   @Test
   public void testSubResourceDescription()
   {
      SimpleManagedResource root = createRootResource();

      SimpleManagedResource a = (SimpleManagedResource) root.registerSubResource("a", A_DESC);
      SimpleManagedResource a_1 = (SimpleManagedResource) a.registerSubResource("a-1", A_1_DESC);
      SimpleManagedResource a_1_1 = (SimpleManagedResource) a_1.registerSubResource("a-1-1", A_1_1_DESC);

      SimpleManagedResource c = (SimpleManagedResource) root.registerSubResource("c", C_DESC);
      SimpleManagedResource c_1 = (SimpleManagedResource) c.registerSubResource("c-1", C_1_DESC);
      SimpleManagedResource c_1_1 = (SimpleManagedResource) c_1.registerSubResource("c-1-1", C_1_1_DESC);


      assertEquals(A_DESC.getDescription(), root.getResourceDescription(PathAddress.pathAddress("a")).getDescription());
      assertEquals(A_1_DESC.getDescription(), root.getResourceDescription(PathAddress.pathAddress("a", "a-1")).getDescription());
      assertEquals(A_1_1_DESC.getDescription(), root.getResourceDescription(PathAddress.pathAddress("a", "a-1", "a-1-1")).getDescription());

      assertEquals(C_DESC.getDescription(), root.getResourceDescription(PathAddress.pathAddress("c")).getDescription());
      assertEquals(C_1_DESC.getDescription(), root.getResourceDescription(PathAddress.pathAddress("c", "c-1")).getDescription());
      assertEquals(C_1_1_DESC.getDescription(), root.getResourceDescription(PathAddress.pathAddress("c", "c-1", "c-1-1")).getDescription());
   }

   @Test
   public void testChildNames()
   {
      SimpleManagedResource root = createRootResource();

      SimpleManagedResource a = (SimpleManagedResource) root.registerSubResource("a", A_DESC);
      SimpleManagedResource a_1 = (SimpleManagedResource) a.registerSubResource("a-1", A_1_DESC);
      SimpleManagedResource a_1_1 = (SimpleManagedResource) a_1.registerSubResource("a-1-1", A_1_1_DESC);
      SimpleManagedResource a_2 = (SimpleManagedResource) a.registerSubResource("a-2", A_2_DESC);

      SimpleManagedResource b = (SimpleManagedResource) root.registerSubResource("b", B_DESC);

      SimpleManagedResource c = (SimpleManagedResource) root.registerSubResource("c", C_DESC);
      SimpleManagedResource c_1 = (SimpleManagedResource) c.registerSubResource("c-1", C_1_DESC);

      assertChildNames(root, asSet("a-1", "a-2"), "a");
      assertChildNames(root, asSet("a-1-1"), "a", "a-1");
      assertChildNames(root, asSet(), "a", "a-1", "a-1-1");
      assertChildNames(root, asSet(), "a", "a-2");

      assertChildNames(root, asSet(), "b");

      assertChildNames(root, asSet("c-1"), "c");
      assertChildNames(root, asSet(), "c", "c-1");
   }

   @Test
   public void testOperationHandler()
   {
      ManagedDescription adesc = mock(ManagedDescription.class);
      OperationHandler aHandler = mock(OperationHandler.class);
      ManagedDescription aopdesc = mock(ManagedDescription.class);

      ManagedDescription a1desc = mock(ManagedDescription.class);
      OperationHandler a1Handler = mock(OperationHandler.class);
      ManagedDescription a1opdesc = mock(ManagedDescription.class);

      SimpleManagedResource root = createRootResource();

      ManagedResource.Registration areg = root.registerSubResource("a", adesc);
      areg.registerOperationHandler("a-op-name", aHandler, aopdesc);
      
      ManagedResource.Registration a1reg = areg.registerSubResource("a1", a1desc);
      a1reg.registerOperationHandler("a1-op-name", a1Handler, a1opdesc);
      
      OperationHandler op = root.getOperationHandler(PathAddress.pathAddress("a"), "a-op-name");
      assertNotNull(op);
      op.execute(null, null);

      op = root.getOperationHandler(PathAddress.pathAddress("a", "a1"), "a1-op-name");
      assertNotNull(op);
      op.execute(null, null);

      verify(aHandler).execute(null, null);
      verify(a1Handler).execute(null, null);
   }

   @Test
   public void testInheritedOperationHandler()
   {
      ManagedDescription adesc = mock(ManagedDescription.class);
      OperationHandler aHandler = mock(OperationHandler.class);
      ManagedDescription aopdesc = mock(ManagedDescription.class);

      ManagedDescription a1desc = mock(ManagedDescription.class);
      OperationHandler a1Handler = mock(OperationHandler.class);
      ManagedDescription a1opdesc = mock(ManagedDescription.class);

      SimpleManagedResource root = createRootResource();

      ManagedResource.Registration areg = root.registerSubResource("a", adesc);
      areg.registerOperationHandler("a-op-name", aHandler, aopdesc, true);

      ManagedResource.Registration a1reg = areg.registerSubResource("a1", a1desc);
      a1reg.registerOperationHandler("a1-op-name", a1Handler, a1opdesc);

      OperationHandler op = root.getOperationHandler(PathAddress.pathAddress("a"), "a-op-name");
      assertNotNull(op);
      op.execute(null, null);

      op = root.getOperationHandler(PathAddress.pathAddress("a", "a1"), "a1-op-name");
      assertNotNull(op);
      op.execute(null, null);

      op = root.getOperationHandler(PathAddress.pathAddress("a", "a1"), "a-op-name");
      assertNotNull(op);
      op.execute(null, null);

      verify(aHandler, times(2)).execute(null, null);
      verify(a1Handler).execute(null, null);
   }

   @Test
   public void testOverwriteInheritedOperationHandler()
   {
      ManagedDescription adesc = mock(ManagedDescription.class);
      OperationHandler aHandler = mock(OperationHandler.class);
      ManagedDescription aopdesc = mock(ManagedDescription.class);

      ManagedDescription a1desc = mock(ManagedDescription.class);
      OperationHandler a1Handler = mock(OperationHandler.class);
      ManagedDescription a1opdesc = mock(ManagedDescription.class);

      ManagedDescription a2desc = mock(ManagedDescription.class);
      OperationHandler a2Handler = mock(OperationHandler.class);
      ManagedDescription a2opdesc = mock(ManagedDescription.class);

      SimpleManagedResource root = createRootResource();

      ManagedResource.Registration areg = root.registerSubResource("a", adesc);
      areg.registerOperationHandler("a-op-name", aHandler, aopdesc, true);

      ManagedResource.Registration a1reg = areg.registerSubResource("a1", a1desc);
      a1reg.registerOperationHandler("a1-op-name", a1Handler, a1opdesc);

      ManagedResource.Registration a2reg = areg.registerSubResource("a2", a2desc);
      a2reg.registerOperationHandler("a-op-name", a2Handler, a2opdesc);

      OperationHandler op = root.getOperationHandler(PathAddress.pathAddress("a"), "a-op-name");
      assertNotNull(op);
      op.execute(null, null);

      op = root.getOperationHandler(PathAddress.pathAddress("a", "a1"), "a1-op-name");
      assertNotNull(op);
      op.execute(null, null);

      op = root.getOperationHandler(PathAddress.pathAddress("a", "a1"), "a-op-name");
      assertNotNull(op);
      op.execute(null, null);

      op = root.getOperationHandler(PathAddress.pathAddress("a", "a2"), "a-op-name");
      assertNotNull(op);
      op.execute(null, null);

      verify(aHandler, times(2)).execute(null, null);
      verify(a1Handler).execute(null, null);
      verify(a2Handler).execute(null, null);
   }

   @Test
   public void testLocationString()
   {
      SimpleManagedResource root = createRootResource();

      SimpleManagedResource a = (SimpleManagedResource) root.registerSubResource("a", A_DESC);
      SimpleManagedResource a_1 = (SimpleManagedResource) a.registerSubResource("a-1", A_1_DESC);
      SimpleManagedResource a_1_1 = (SimpleManagedResource) a_1.registerSubResource("a-1-1", A_1_1_DESC);

      assertEquals("/a", a.getPath());
      assertEquals("/a/a-1", a_1.getPath());
      assertEquals("/a/a-1/a-1-1", a_1_1.getPath());
   }

   @Override
   public RuntimeContext createRuntimeContext()
   {
      return runtimeContext;
   }

   private ManagedDescription description(final String description)
   {
      return new ManagedDescription()
      {
         @Override
         public String getDescription()
         {
            return description;
         }
      };
   }

   private SimpleManagedResource createRootResource()
   {
      return new SimpleManagedResource(null, null, ROOT_DESC, this);
   }

   private void assertChildNames(ManagedResource root, Set<String> expected, String...path)
   {
      Set<String> actual = root.getChildNames(PathAddress.pathAddress(path));
      assertNotNull(actual);
      assertEquals(expected.size(), actual.size());

      for (String child : expected)
      {
         assertTrue(actual.contains(child));
      }
   }

   private Set<String> asSet(String...values)
   {
      return new HashSet<String>(Arrays.asList(values));
   }

   private static final ManagedDescription ROOT_DESC = new ManagedDescription()
   {
      @Override
      public String getDescription()
      {
         return "Root resource description";
      }
   };

   private static final ManagedDescription A_DESC = new ManagedDescription()
   {
      @Override
      public String getDescription()
      {
         return "Resource A description";
      }
   };

   private static final ManagedDescription A_1_DESC = new ManagedDescription()
   {
      @Override
      public String getDescription()
      {
         return "Resource A-1 description";
      }
   };

   private static final ManagedDescription A_1_1_DESC = new ManagedDescription()
   {
      @Override
      public String getDescription()
      {
         return "Resource A-1-1 description";
      }
   };

   private static final ManagedDescription A_2_DESC = new ManagedDescription()
   {
      @Override
      public String getDescription()
      {
         return "Resource A-2 description";
      }
   };

   private static final ManagedDescription B_DESC = new ManagedDescription()
   {
      @Override
      public String getDescription()
      {
         return "Resource B description";
      }
   };

   private static final ManagedDescription C_DESC = new ManagedDescription()
   {
      @Override
      public String getDescription()
      {
         return "Resource C description";
      }
   };

   private static final ManagedDescription C_1_DESC = new ManagedDescription()
   {
      @Override
      public String getDescription()
      {
         return "Resource C-1 description";
      }
   };

   private static final ManagedDescription C_1_1_DESC = new ManagedDescription()
   {
      @Override
      public String getDescription()
      {
         return "Resource C-1-1 description";
      }
   };

   private static final ManagedDescription WILDCARD_DESC = new ManagedDescription()
   {
      @Override
      public String getDescription()
      {
         return "Wildcard description";
      }
   };
}
