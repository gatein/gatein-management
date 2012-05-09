/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.gatein.management.core.api.model;

import org.gatein.management.api.model.ModelList;
import org.gatein.management.api.model.ModelNumber;
import org.gatein.management.api.model.ModelString;
import org.jboss.dmr.ModelNode;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class DmrModelStringTest
{
   @Test
   public void testInitial_Value()
   {
      assertNull(modelString().getValue());
   }

   @Test
   public void testSet_Value()
   {
      assertEquals("foo", modelString().set("foo").getValue());
   }

   @Test
   public void testSet_Multiple_Values()
   {
      assertEquals("bar", modelString().set("foo").set("bar").getValue());
   }

   @Test
   public void testSet_Mutability()
   {
      ModelString string = modelString().set("foo");
      ModelString string2 = string.set("bar");
      ModelString string3 = string.set("foobar");

      assertTrue(string == string2);
      assertTrue(string2 == string3);
   }

   @Test
   public void testAsValue()
   {
      ModelString expected = modelString();
      ModelString actual = expected.asValue(ModelString.class);

      assertTrue(expected == actual);
   }

   @Test
   public void testAsValue_Number()
   {
      assertEquals(3, modelString().set("3").asValue(ModelNumber.class).getInt());
   }

   @Test(expected = NumberFormatException.class)
   public void testAsValue_Invalid_Number()
   {
      modelString().set("foo").asValue(ModelNumber.class).getInt();
   }

   @Test(expected = IllegalArgumentException.class)
   public void testAsValue_Invalid_Type()
   {
      new DmrModelNumber(new ModelNode()).set(3).asValue(ModelList.class);
   }

   private static ModelString modelString()
   {
      return new DmrModelString(new ModelNode());
   }
}
