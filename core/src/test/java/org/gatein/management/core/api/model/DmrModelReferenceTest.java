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
import org.gatein.management.api.model.ModelObject;
import org.gatein.management.api.model.ModelReference;
import org.gatein.management.api.model.ModelString;
import org.jboss.dmr.ModelNode;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class DmrModelReferenceTest
{
   @Test
   public void test_Init()
   {
      assertFalse(modelRef().isDefined());
      assertNull(modelRef().getValue());
   }

   @Test
   public void testModelRef()
   {
      PathAddress addr = PathAddress.pathAddress("foo", "bar");
      assertEquals(addr, modelRef().set(PathAddress.pathAddress("foo", "bar")).getValue());
   }

   @Test
   public void testFromJson() throws Exception
   {
      String json =
         "{\"page\" : {\n" +
         "    \"name\" : \"home\",\n" +
         "    \"_ref\" : \"/pages/home\"\n" +
         "}}";

      ModelObject model = DmrModelValue.readFromJsonStream(new ByteArrayInputStream(json.getBytes()), ModelObject.class);
      ModelReference pageRef = model.get("page", ModelReference.class);
      assertNotNull(pageRef);
      assertEquals(pageRef.get("name", ModelString.class).getValue(), "home");
      assertEquals(PathAddress.pathAddress("pages", "home"), pageRef.getValue());
   }

   private static ModelReference modelRef()
   {
      return new DmrModelReference(new ModelNode());
   }
}
