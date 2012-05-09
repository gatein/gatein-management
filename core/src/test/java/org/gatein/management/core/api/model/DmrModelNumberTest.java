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
import org.gatein.management.api.model.ModelValue;
import org.jboss.dmr.ModelNode;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class DmrModelNumberTest
{
   @Test
   public void testInt()
   {
      ModelNumber number = modelNumber().set(3);

      assertEquals(3, number.getInt());
      assertEquals(ModelValue.ModelValueType.NUMBER, number.getValueType());
      assertEquals(ModelNumber.ModelNumberType.INT, number.getNumberType());

      // Initial value
      assertEquals(0, modelNumber().getInt());
   }

   @Test
   public void testLong()
   {
      ModelNumber number = modelNumber();
      number.set(3L);

      assertEquals(3L, number.getLong());
      assertEquals(ModelValue.ModelValueType.NUMBER, number.getValueType());
      assertEquals(ModelNumber.ModelNumberType.LONG, number.getNumberType());
   }

   private static DmrModelNumber modelNumber()
   {
      return new DmrModelNumber(new ModelNode());
   }

   @Test
   public void testDouble()
   {
      ModelNumber number = modelNumber();
      number.set(3.0);

      assertEquals(3.0, number.getDouble(), 0);
      assertEquals(ModelValue.ModelValueType.NUMBER, number.getValueType());
      assertEquals(ModelNumber.ModelNumberType.DOUBLE, number.getNumberType());
   }

   @Test
   public void testBigInteger()
   {
      ModelNumber number = modelNumber();
      number.set(new BigInteger("3"));

      assertEquals(BigInteger.valueOf(3), number.getBigInteger());
      assertEquals(ModelValue.ModelValueType.NUMBER, number.getValueType());
      assertEquals(ModelNumber.ModelNumberType.BIG_INTEGER, number.getNumberType());
   }

   @Test
   public void testBigDecimal()
   {
      ModelNumber number = modelNumber();
      number.set(new BigDecimal("3"));

      assertEquals(BigDecimal.valueOf(3), number.getBigDecimal());
      assertEquals(ModelValue.ModelValueType.NUMBER, number.getValueType());
      assertEquals(ModelNumber.ModelNumberType.BIG_DECIMAL, number.getNumberType());
   }

   @Test
   public void testAsValue()
   {
      ModelNumber expected = modelNumber();
      ModelNumber actual = expected.asValue(ModelNumber.class);

      assertTrue(expected == actual);
   }

   @Test
   public void testAsValue_String()
   {
      assertEquals("3", modelNumber().set(3).asValue(ModelString.class).getValue());
   }

   @Test(expected = IllegalArgumentException.class)
   public void testAsValue_Invalid_Type()
   {
      modelNumber().set(3).asValue(ModelList.class);
   }
}
