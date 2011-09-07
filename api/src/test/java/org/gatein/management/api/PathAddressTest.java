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

package org.gatein.management.api;

import org.junit.Test;

import static org.junit.Assert.*;
/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PathAddressTest
{
   @Test
   public void testPathAddressEquals()
   {
      PathAddress address1 = PathAddress.pathAddress("one", "two", "three");
      PathAddress address2 = PathAddress.pathAddress("one", "two", "three");
      PathAddress address3 = PathAddress.pathAddress("one", "two", "three");
      PathAddress address4 = PathAddress.pathAddress("one", "two", "blah");
      
      assertEquals(address1, address2);
      assertEquals(address1, address3);
      assertEquals(address2, address3);
      
      assertFalse(address1.equals(address4));
   }

   @Test
   public void testPathAddressString()
   {
      PathAddress address = PathAddress.pathAddress("one", "two", "three");
      assertEquals("/one/two/three", address.toString());

      assertEquals(address, PathAddress.pathAddress(address.toString()));
      assertEquals(address, PathAddress.pathAddress("one/two/three"));
      assertEquals(address, PathAddress.pathAddress("one/two/three/"));
      assertEquals(address, PathAddress.pathAddress("/one/two/three"));
      assertEquals(address, PathAddress.pathAddress("/one/two/three/"));
   }

   @Test
   public void testPathAddressSlashes()
   {
      PathAddress address = PathAddress.pathAddress("one", "two", "three");

      assertEquals(address, PathAddress.pathAddress("one/two", "three"));
      assertEquals(address, PathAddress.pathAddress("one/two/", "three"));
      assertEquals(address, PathAddress.pathAddress("/one/two/", "three"));
      assertEquals(address, PathAddress.pathAddress("one", "/two/three"));
   }

   @Test
   public void testSubAddress()
   {
      PathAddress address = PathAddress.pathAddress("one", "two", "three");
      assertEquals(PathAddress.pathAddress("two", "three"), address.subAddress(1));

      PathAddress sub1 = address.subAddress(1, 3);
      assertEquals(PathAddress.pathAddress("two", "three"), sub1);
      PathAddress sub2 = address.subAddress(2, 3);
      assertEquals(PathAddress.pathAddress("three"), sub2);
   }
   
   @Test
   public void testAddressCopy()
   {
      PathAddress address = PathAddress.pathAddress("one", "two", "three");
      assertEquals(address, address.copy());
      assertFalse(address == address.copy());
   }
}
