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

import org.gatein.management.api.PathAddress;
import org.gatein.management.api.model.Model;
import org.gatein.management.api.model.ModelNumber;
import org.gatein.management.api.model.ModelValue;
import org.jboss.dmr.ModelNode;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class DmrModelTest
{
   @Test
   public void testUndefined()
   {
      assertFalse(newModel().isDefined());
      assertEquals(ModelValue.ModelValueType.UNDEFINED, newModel().getValueType());
   }

   @Test
   public void testString()
   {
      assertTrue(newModel().set("foo").isDefined());
      assertEquals(ModelValue.ModelValueType.STRING, newModel().set("foo").getValueType());
      assertEquals("foo", newModel().set("foo").getValue());
   }

   @Test
   public void testNullString()
   {
      assertFalse(newModel().set((String) null).isDefined());
      assertNull(newModel().set((String) null).getValue());
   }

   @Test
   public void testNumber()
   {
      assertTrue(newModel().set(3).isDefined());
      assertEquals(ModelValue.ModelValueType.NUMBER, newModel().set(3).getValueType());
      assertEquals(ModelNumber.ModelNumberType.INT, newModel().set(3).getNumberType());
      assertEquals(3, newModel().set(3).getInt());

      assertTrue(newModel().set(3L).isDefined());
      assertEquals(ModelValue.ModelValueType.NUMBER, newModel().set(3L).getValueType());
      assertEquals(ModelNumber.ModelNumberType.LONG, newModel().set(3L).getNumberType());
      assertEquals(3L, newModel().set(3L).getInt());

      assertTrue(newModel().set(Math.PI).isDefined());
      assertEquals(ModelValue.ModelValueType.NUMBER, newModel().set(Math.PI).getValueType());
      assertEquals(ModelNumber.ModelNumberType.DOUBLE, newModel().set(Math.PI).getNumberType());
      assertEquals(Math.PI, newModel().set(Math.PI).getDouble(), 0);

      BigInteger bi = new BigInteger("3");
      assertTrue(newModel().set(bi).isDefined());
      assertEquals(ModelValue.ModelValueType.NUMBER, newModel().set(bi).getValueType());
      assertEquals(ModelNumber.ModelNumberType.BIG_INTEGER, newModel().set(bi).getNumberType());
      assertEquals(BigInteger.valueOf(3), newModel().set(bi).getBigInteger());

      BigDecimal bd = new BigDecimal(String.valueOf(Math.PI));
      assertTrue(newModel().set(bd).isDefined());
      assertEquals(ModelValue.ModelValueType.NUMBER, newModel().set(bd).getValueType());
      assertEquals(ModelNumber.ModelNumberType.BIG_DECIMAL, newModel().set(bd).getNumberType());
      assertEquals(BigDecimal.valueOf(Math.PI), newModel().set(bd).getBigDecimal());
   }

   @Test
   public void testNullNumber()
   {
      assertFalse(newModel().set((BigInteger) null).isDefined());
      assertNull(newModel().set((BigInteger) null).getBigInteger());

      assertFalse(newModel().set((BigDecimal) null).isDefined());
      assertNull(newModel().set((BigDecimal) null).getBigDecimal());
   }

   @Test
   public void testBoolean()
   {
      assertTrue(newModel().set(true).isDefined());
      assertEquals(ModelValue.ModelValueType.BOOLEAN, newModel().set(true).getValueType());
      assertTrue(newModel().set(true).getValue());
      assertFalse(newModel().set(false).getValue());
   }

   @Test
   public void testObject()
   {
      assertTrue(newModel().setEmptyObject().isDefined());
      assertEquals(ModelValue.ModelValueType.OBJECT, newModel().setEmptyObject().getValueType());
   }

   @Test
   public void testList()
   {
      assertTrue(newModel().setEmptyList().isDefined());
      assertEquals(ModelValue.ModelValueType.LIST, newModel().setEmptyList().getValueType());
   }

   @Test
   public void testReference()
   {
      assertTrue(newModel().set(PathAddress.pathAddress("foo", "bar")).isDefined());
      assertEquals(ModelValue.ModelValueType.REFERENCE, newModel().set(PathAddress.pathAddress("foo", "bar")).getValueType());
   }

   private static Model newModel()
   {
      return new DmrModel(new ModelNode());
   }
}
