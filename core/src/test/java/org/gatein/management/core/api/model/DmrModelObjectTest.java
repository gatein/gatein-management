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
import org.gatein.management.api.model.ModelBoolean;
import org.gatein.management.api.model.ModelList;
import org.gatein.management.api.model.ModelNumber;
import org.gatein.management.api.model.ModelObject;
import org.gatein.management.api.model.ModelReference;
import org.gatein.management.api.model.ModelString;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import static junit.framework.Assert.*;
import static org.gatein.management.api.model.ModelValue.ModelValueType.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class DmrModelObjectTest
{
   @Test
   public void testGet_Set()
   {
      // String
      assertEquals("bar", modelObject().get("foo").set("bar").getValue());

      // Number
      assertEquals(3, modelObject().get("foo").set(3).getInt());
      assertEquals(3L, modelObject().get("foo").set(3).getLong());
      assertEquals(3.14159265, modelObject().get("foo").set(3.14159265).getDouble(), 0);
      assertEquals(BigInteger.valueOf(3), modelObject().get("foo").set(new BigInteger("3")).getBigInteger());
      assertEquals(BigDecimal.valueOf(3.14159265), modelObject().get("foo").set(new BigDecimal("3.14159265")).getBigDecimal());

      // Boolean
      assertTrue(modelObject().get("foo").set(true).getValue());
      assertFalse(modelObject().get("foo").set(false).getValue());

      // ModelObject
      assertTrue(modelObject().get("foo").setEmptyObject().getNames().isEmpty());

      // ModelList
      assertEquals(0, modelObject().get("foo").setEmptyList().size());

      // ModelReference
      assertEquals(PathAddress.pathAddress("foo", "bar"), modelObject().get("foo").set(PathAddress.pathAddress("foo", "bar")).getValue());
   }

   @Test
   public void testGet_Set_Null()
   {
      // String
      assertNull(modelObject().get("foo").set((String) null).getValue());

      // BigInteger
      assertNull(modelObject().get("foo").set((BigInteger) null).getBigInteger());

      // BigDecimal
      assertNull(modelObject().get("foo").set((BigDecimal) null).getBigDecimal());

      // Reference
      assertNull(modelObject().get("foo").set((PathAddress) null).getValue());
   }

   @Test
   public void testSet()
   {
      // String
      assertEquals("bar", modelObject().set("foo", "bar").get("foo", ModelString.class).getValue());

      // Number
      assertEquals(3, modelObject().set("foo", 3).get("foo", ModelNumber.class).getInt());
      assertEquals(3L, modelObject().set("foo", 3).get("foo", ModelNumber.class).getLong());
      assertEquals(3.14159265, modelObject().set("foo", 3.14159265).get("foo", ModelNumber.class).getDouble(), 0);
      assertEquals(BigInteger.valueOf(3), modelObject().set("foo", new BigInteger("3")).get("foo", ModelNumber.class).getBigInteger());
      assertEquals(BigDecimal.valueOf(3.14159265), modelObject().set("foo", new BigDecimal("3.14159265")).get("foo", ModelNumber.class).getBigDecimal());

      // Boolean
      assertTrue(modelObject().set("foo", true).get("foo", ModelBoolean.class).getValue());
      assertFalse(modelObject().set("foo", false).get("foo", ModelBoolean.class).getValue());

      // Reference
      assertEquals(PathAddress.pathAddress("foo", "bar"), modelObject().set("foo", PathAddress.pathAddress("foo", "bar")).get("foo", ModelReference.class).getValue());
   }

   @Test
   public void testSet_Null()
   {
      // String
      assertFalse(modelObject().set("foo", (String) null).get("foo").isDefined());
      assertNull(modelObject().set("foo", (String) null).get("foo", ModelString.class).getValue());

      // BigInteger
      assertFalse(modelObject().set("foo", (BigInteger) null).get("foo").isDefined());
      assertNull(modelObject().set("foo", (BigInteger) null).get("foo", ModelNumber.class).getBigInteger());

      // BigDecimal
      assertFalse(modelObject().set("foo", (BigDecimal) null).get("foo").isDefined());
      assertNull(modelObject().set("foo", (BigDecimal) null).get("foo", ModelNumber.class).getBigDecimal());

      // Reference
      assertFalse(modelObject().set("foo", (PathAddress) null).get("foo").isDefined());
      assertNull(modelObject().set("foo", (PathAddress) null).get("foo", ModelReference.class).getValue());
   }

   @Test
   public void testGet_AsValue_Set()
   {
      // String
      assertEquals("bar", modelObject().get("foo").asValue(ModelString.class).set("bar").getValue());

      // Number
      assertEquals(3, modelObject().get("foo").asValue(ModelNumber.class).set(3).getInt());
      assertEquals(3L, modelObject().get("foo").asValue(ModelNumber.class).set(3).getLong());
      assertEquals(3.14159265, modelObject().get("foo").asValue(ModelNumber.class).set(3.14159265).getDouble(), 0);
      assertEquals(BigInteger.valueOf(3), modelObject().get("foo").asValue(ModelNumber.class).set(new BigInteger("3")).getBigInteger());
      assertEquals(BigDecimal.valueOf(3.14159265), modelObject().get("foo").asValue(ModelNumber.class).set(new BigDecimal("3.14159265")).getBigDecimal());

      // Boolean
      assertTrue(modelObject().get("foo").asValue(ModelBoolean.class).set(true).getValue());
      assertFalse(modelObject().get("foo").asValue(ModelBoolean.class).set(false).getValue());

      // ModelObject
      assertTrue(modelObject().get("foo").asValue(ModelObject.class).getNames().isEmpty());

      // ModelObject
      assertEquals(0, modelObject().get("foo").asValue(ModelList.class).size());

      // ModelReference
      assertEquals(PathAddress.pathAddress("bar"), modelObject().get("foo").asValue(ModelReference.class).set(PathAddress.pathAddress("bar")).getValue());
   }

   @Test
   public void testValueType()
   {
      // Undefined
      assertEquals(UNDEFINED, modelObject().get("foo").getValueType());

      // String
      assertEquals(STRING, modelObject().get("foo").set("bar").getValueType());

      // Number
      assertEquals(NUMBER, modelObject().get("foo").set(3).getValueType());
      assertEquals(NUMBER, modelObject().get("foo").set(3L).getValueType());
      assertEquals(NUMBER, modelObject().get("foo").set(3.14159265).getValueType());
      assertEquals(NUMBER, modelObject().get("foo").set(new BigInteger("3")).getValueType());
      assertEquals(NUMBER, modelObject().get("foo").set(new BigDecimal("3.14159265")).getValueType());

      // Boolean
      assertEquals(BOOLEAN, modelObject().get("foo").set(true).getValueType());
      assertEquals(BOOLEAN, modelObject().get("foo").set(false).getValueType());

      // ModelObject
      assertEquals(OBJECT, modelObject().get("foo").setEmptyObject().getValueType());

      // ModelList
      assertEquals(LIST, modelObject().get("foo").setEmptyList().getValueType());

      // ModelReference
      assertEquals(REFERENCE, modelObject().get("foo").set(PathAddress.pathAddress("foo")).getValueType());
   }

   @Test
   public void testIsDefined()
   {
      assertFalse(modelObject().isDefined());
      assertFalse(modelObject().asValue(ModelString.class).isDefined());
      assertFalse(modelObject().get("foo", ModelString.class).isDefined());
      assertTrue(modelObject().asValue(ModelString.class).set("foo").isDefined());
   }

   @Test
   public void testHasDefined()
   {
      assertFalse(modelObject().hasDefined("foo"));
      ModelObject model = modelObject();
      model.get("foo");
      assertFalse(model.hasDefined("foo"));
      model.set("foo", "bar");
      assertTrue(model.hasDefined("foo"));
   }

   @Test
   public void testHasName()
   {
      assertFalse(modelObject().has("foo"));
      ModelObject model = modelObject();
      model.get("foo");
      assertTrue(model.has("foo"));
   }

   @Test
   public void testRemove()
   {
      assertNull(modelObject().remove("foo"));
      ModelObject model = modelObject();
      model.set("foo", "bar");

      ModelString string = model.get("foo", ModelString.class);
      assertEquals("bar", string.getValue());
   }

   @Test
   public void testGet_Multiple()
   {
      assertFalse(modelObject().get("foo", "bar", "baz").asValue(ModelObject.class).isDefined());

      ModelObject model = modelObject();
      model.get("foo", ModelObject.class).get("bar", ModelObject.class).get("baz", ModelString.class).set("foo-bar-baz");

      assertEquals("foo-bar-baz", model.get("foo", "bar", "baz").asValue(ModelString.class).getValue());
   }

   @Test
   public void testToString()
   {
      ModelObject model = modelObject();
      model.get("foo").set("bar");

      assertEquals(model.toString(), model.toJsonString(false));
   }

   @Test
   public void testEquals() throws IOException
   {
      ModelObject one = modelObject();
      one.get("foo").set("bar");

      ModelObject two = modelObject();
      two.get("foo").set("bar");

      ModelObject three = modelObject().fromJsonStream(new ByteArrayInputStream("{\"foo\":\"bar\"}".getBytes()), ModelObject.class);
      ModelObject four = modelObject().fromJsonStream(new ByteArrayInputStream("{\n\"foo\"\n      :   \"bar\"\n       }".getBytes()), ModelObject.class);

      assertEquals(one, two);
      assertEquals(one, two.fromJsonStream(new ByteArrayInputStream(two.toString().getBytes()), ModelObject.class));
      assertEquals(one, three);
      assertEquals(one, four);
   }

   private static ModelObject modelObject()
   {
      return new DmrModelObject();
   }
}
