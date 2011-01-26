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

package org.gatein.management.portalobjects.exportimport.impl;

import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PortalObjectsContextTest
{

   @Test
   public void testEmptyContext()
   {
      PortalObjectsContext context = new PortalObjectsContext();
      assertTrue(context.getPortalConfigs().isEmpty());
      assertTrue(context.getPages().isEmpty());
      assertTrue(context.getNavigations().isEmpty());
   }

   @Test
   public void testAddToContext()
   {
      PortalObjectsContext context = new PortalObjectsContext();

      // Portal config
      context.addToContext(new PortalConfig("ownerType", "ownerId"));
      assertEquals(1, context.getPortalConfigs().size());
      context.addToContext(new PortalConfig("ownerType", "another-ownerId"));
      assertEquals(2, context.getPortalConfigs().size());

      context = new PortalObjectsContext();
      // Pages
      context.addToContext(new Page("ownerType1", "ownerId1", "name1"));
      assertEquals(1, context.getPages().size());
      context.addToContext(new Page("ownerType1", "ownerId2", "name1"));
      assertEquals(2, context.getPages().size());
      context.addToContext(new Page("ownerType1", "ownerId2", "name2"));
      assertEquals(2, context.getPages().size());
      assertEquals(1, context.getPages().get(0).size());
      assertEquals(2, context.getPages().get(1).size());

      context = new PortalObjectsContext();
      // Navigation
      context.addToContext(newPageNavigation("ownerType1", "ownerId1", 1));
      assertEquals(1, context.getNavigations().size());
      context.addToContext(newPageNavigation("ownerType1", "ownerId2", 1));
      assertEquals(2, context.getNavigations().size());
      context.addToContext(newPageNavigation("ownerType2", "ownerId2", 1));
      assertEquals(3, context.getNavigations().size());
   }

   @Test
   public void testReplacement()
   {
      // Portal config
      PortalObjectsContext context = new PortalObjectsContext();
      context.addToContext(new PortalConfig("ownerType1", "ownerId1"));
      context.addToContext(new PortalConfig("ownerType1", "ownerId2"));
      {
         // Verify that this replaces the previous portal config added to context.
         PortalConfig pc = new PortalConfig("ownerType1", "ownerId2");
         pc.setLocale("test");
         context.addToContext(pc);
         assertEquals(2, context.getPortalConfigs().size());
         pc = context.getPortalConfigs().get(1);
         assertEquals("test", pc.getLocale());
      }

      // Pages
      context = new PortalObjectsContext();
      context.addToContext(new Page("ownerType1", "ownerId1", "name1"));
      context.addToContext(new Page("ownerType1", "ownerId2", "name1"));
      {
         // Verify first page gets replaced by this one
         context.addToContext(new Page("ownerType1", "ownerId1", "name1"));
         assertEquals(2, context.getPages().size());
         assertEquals(1, context.getPages().get(0).size());
         assertEquals("name1", context.getPages().get(0).get(0).getName());

         // Verify this page is added
         context.addToContext(new Page("ownerType1", "ownerId1", "name2"));
         assertEquals(2, context.getPages().size());
         assertEquals(2, context.getPages().get(0).size());
      }

      // Navigation
      context = new PortalObjectsContext();
      context.addToContext(newPageNavigation("ownerType1", "ownerId1", 1));
      context.addToContext(newPageNavigation("ownerType2", "ownerId1", 2));
      {
         // Verify 1st navigation is replaced, but size is still 2
         context.addToContext(newPageNavigation("ownerType1", "ownerId1", 123));
         assertEquals(2, context.getNavigations().size());
         assertEquals(123, context.getNavigations().get(0).getPriority());

         // Verify 2nd navigation is replaced, but size is still 2
         context.addToContext(newPageNavigation("ownerType2", "ownerId1", 222));
         assertEquals(2, context.getNavigations().size());
         assertEquals(222, context.getNavigations().get(1).getPriority());

         // Verify this gets added, size increases to 3
         context.addToContext(newPageNavigation("ownerType2", "ownerId2", 333));
         assertEquals(3, context.getNavigations().size());
      }
   }

   @Test
   public void testPageGrouping()
   {
      PortalObjectsContext context = new PortalObjectsContext();
      context.addToContext(new Page("ownerType1", "ownerId1", "name1"));
      context.addToContext(new Page("ownerType1", "ownerId2", "name1"));
      context.addToContext(new Page("ownerType2", "ownerId1", "name1"));
      context.addToContext(new Page("ownerType1", "ownerId1", "name2"));
      context.addToContext(new Page("ownerType2", "ownerId1", "name2"));
      context.addToContext(new Page("ownerType1", "ownerId2", "name2"));

      assertEquals(3, context.getPages().size());
      assertEquals(2, context.getPages().get(0).size());
      assertEquals(2, context.getPages().get(1).size());
      assertEquals(2, context.getPages().get(2).size());

      // This verifies that the list of lists are grouped according to the ownerType and ownerId.  In other words
      // every list of pages has the same ownerType and ownerId.
      List<Page> pages = context.getPages().get(0);
      for (Page page : pages)
      {
         assertEquals("ownerType1", page.getOwnerType());
         assertEquals("ownerId1", page.getOwnerId());
      }
      pages = context.getPages().get(1);
      for (Page page : pages)
      {
         assertEquals("ownerType1", page.getOwnerType());
         assertEquals("ownerId2", page.getOwnerId());
      }
      pages = context.getPages().get(2);
      for (Page page : pages)
      {
         assertEquals("ownerType2", page.getOwnerType());
         assertEquals("ownerId1", page.getOwnerId());
      }
   }

   @Test
   public void testImmutability()
   {
      PortalObjectsContext context = new PortalObjectsContext();
      PortalConfig pc = new PortalConfig("ownerType", "ownerId");
      context.addToContext(pc);

      pc.setName("another-name");

      PortalConfig ctxpc = context.getPortalConfigs().get(0);

      assertFalse(pc == ctxpc);
      assertEquals("ownerType", ctxpc.getType());
      assertEquals("ownerId", ctxpc.getName());

      Page page = new Page("ownerType", "ownerId", "pageName");
      context.addToContext(page);
      page.setName("foo");

      Page ctxPage = context.getPages().get(0).get(0);
      assertEquals("ownerType", ctxPage.getOwnerType());
      assertEquals("ownerId", ctxPage.getOwnerId());
      assertEquals("pageName", ctxPage.getName());

      PageNode node = new PageNode();
      node.setName("node-name");

      PageNavigation nav = new PageNavigation();
      nav.setOwnerType("navOwnerType");
      nav.setOwnerId("navOwnerId");
      nav.setPriority(425);
      nav.addNode(node);
      context.addToContext(nav);
      nav.setPriority(33);
      nav.setOwnerType("blah");
      node.setName("blah-name");

      PageNavigation ctxNav = context.getNavigations().get(0);
      assertEquals("navOwnerType", ctxNav.getOwnerType());
      assertEquals("navOwnerId", ctxNav.getOwnerId());
      assertEquals(425, ctxNav.getPriority());
      assertEquals(1, ctxNav.getNodes().size());
      assertNotNull(ctxNav.getNode("node-name"));
   }

   private PageNavigation newPageNavigation(String ownerType, String ownerId, int priority)
   {
      PageNavigation nav = new PageNavigation();
      nav.setOwnerType(ownerType);
      nav.setOwnerId(ownerId);
      nav.setPriority(priority);
      return nav;
   }
}
