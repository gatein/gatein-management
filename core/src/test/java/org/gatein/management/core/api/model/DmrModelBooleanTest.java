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

import org.gatein.management.api.model.ModelBoolean;
import org.gatein.management.api.model.ModelList;
import org.gatein.management.api.model.ModelNumber;
import org.gatein.management.api.model.ModelString;
import org.jboss.dmr.ModelNode;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class DmrModelBooleanTest
{
   @Test
   public void testInitial_Value()
   {
      assertFalse(modelBoolean().getValue());
   }

   @Test
   public void testSet_Value()
   {
      assertTrue(modelBoolean().set(true).getValue());
      assertFalse(modelBoolean().set(false).getValue());
   }

   @Test
   public void testSet_Multiple_Values()
   {
      assertTrue(modelBoolean().set(false).set(true).getValue());
      assertFalse(modelBoolean().set(true).set(false).getValue());
   }

   @Test
   public void testSet_Mutability()
   {
      ModelBoolean bool1 = modelBoolean().set(true);
      ModelBoolean bool2 = bool1.set(false);
      ModelBoolean bool3 = bool2.set(true);

      assertTrue(bool1 == bool2);
      assertTrue(bool2 == bool3);
   }

   @Test
   public void testAsValue()
   {
      ModelBoolean expected = modelBoolean();
      ModelBoolean actual = expected.asValue(ModelBoolean.class);

      assertTrue(expected == actual);
   }

   @Test
   public void testAsValue_Number()
   {
      assertEquals(0, modelBoolean().set(false).asValue(ModelNumber.class).getInt());
      assertEquals(1, modelBoolean().set(true).asValue(ModelNumber.class).getInt());
   }

   @Test
   public void testAsValue_String()
   {
      assertEquals("false", modelBoolean().set(false).asValue(ModelString.class).getValue());
      assertEquals("true", modelBoolean().set(true).asValue(ModelString.class).getValue());
   }

   @Test(expected = IllegalArgumentException.class)
   public void testAsValue_Invalid_Type()
   {
      new DmrModelNumber(new ModelNode()).set(3).asValue(ModelList.class);
   }

   private static ModelBoolean modelBoolean()
   {
      return new DmrModelBoolean(new ModelNode());
   }
}
