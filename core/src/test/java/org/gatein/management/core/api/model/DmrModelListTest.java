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
import org.gatein.management.api.model.ModelObject;
import org.gatein.management.api.model.ModelString;
import org.gatein.management.api.model.ModelValue;
import org.jboss.dmr.ModelNode;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class DmrModelListTest
{
   @Test
   public void testInitial_Value()
   {
      assertFalse(modelList().isDefined());
      assertEquals(0, modelList().size());
      assertTrue(modelList().getValues().isEmpty());
   }

   @Test
   public void testGetIndex()
   {
      int index = 0;
      ModelList list = modelList();

      // String
      assertEquals("3", list.add("3").get((index += 1) - 1, ModelString.class).getValue());

      // Number
      assertEquals(3, list.add(3).get((index += 1) - 1, ModelNumber.class).getInt());
      assertEquals(3L, list.add(3L).get((index += 1) - 1, ModelNumber.class).getInt());
      assertEquals(3.14159265, list.add(3.14159265).get((index += 1) - 1, ModelNumber.class).getDouble(), 0);
      assertEquals(BigInteger.ONE, list.add(new BigInteger("1")).get((index += 1) - 1, ModelNumber.class).getBigInteger());
      assertEquals(BigDecimal.TEN, list.add(new BigDecimal("10")).get((index += 1) - 1, ModelNumber.class).getBigDecimal());

      // Boolean
      assertTrue(list.add(true).get((index += 1) - 1, ModelBoolean.class).getValue());
      assertFalse(list.add(false).get((index += 1) - 1, ModelBoolean.class).getValue());

      // ModelObject
      list.add().setEmptyObject();
      assertTrue(list.get((index += 1) - 1, ModelObject.class).isDefined());

      list.add().setEmptyList();
      assertTrue(list.get((index += 1) - 1, ModelList.class).isDefined());
   }

   @Test(expected = IndexOutOfBoundsException.class)
   public void testGetIndex_OutOfBounds()
   {
      modelList().get(0);
   }

   @Test
   public void testSize()
   {
      assertEquals(3, modelList().add(1).add(2).add(3).size());
      assertEquals(3, modelList().add(1).add(2).add(3).getValues().size());
   }

   @Test
   public void testSameElements()
   {
      ModelList list = modelList().add(1).add(2).add(3);

      int index = 0;
      for (ModelNumber number : list.getValues(ModelNumber.class))
      {
         assertEquals(index += 1, number.getInt());
      }

      list = modelList().add("1").add("2").add("3");

      index = 0;
      for (ModelString string : list.getValues(ModelString.class))
      {
         assertEquals(index += 1, Integer.parseInt(string.getValue()));
      }
   }

   @Test
   public void testDiffElements()
   {
      ModelList list = modelList().add(1).add("3").add(true);
      for (ModelValue value : list)
      {
         switch (value.getValueType())
         {
            case NUMBER:
               assertEquals(1, value.asValue(ModelNumber.class).getInt());
               break;
            case STRING:
               assertEquals("3", value.asValue(ModelString.class).getValue());
               break;
            case BOOLEAN:
               assertTrue(value.asValue(ModelBoolean.class).getValue());
               break;
            default:
               fail("Expected number, string, or boolean for value type of list");
         }
      }
   }

   @Test(expected = ClassCastException.class)
   public void testDiffElements_AsValue()
   {
      for (ModelNumber number : modelList().add(1).add("3").add(true).getValues(ModelNumber.class))
      {
      }
      fail("Should not be able to cast different elements in list to ModelNumber");
   }

   private static ModelList modelList()
   {
      return new DmrModelList(new ModelNode());
   }
}
