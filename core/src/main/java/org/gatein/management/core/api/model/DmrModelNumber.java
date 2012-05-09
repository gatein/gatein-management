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

import org.gatein.management.api.model.ModelNumber;
import org.gatein.management.api.model.ModelString;
import org.gatein.management.api.model.ModelValue;
import org.jboss.dmr.ModelNode;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class DmrModelNumber extends DmrModelValue implements ModelNumber
{
   DmrModelNumber(final ModelNode value)
   {
      super(value);
   }

   @Override
   public int getInt()
   {
      if (!isDefined()) return 0;

      return value.asInt();
   }

   @Override
   public ModelNumber set(int value)
   {
      this.value.set(value);
      return this;
   }

   @Override
   public long getLong()
   {
      if (!isDefined()) return 0L;

      return value.asLong();
   }

   @Override
   public ModelNumber set(long value)
   {
      this.value.set(value);
      return this;
   }

   @Override
   public double getDouble()
   {
      if (!isDefined()) return 0d;

      return value.asDouble();
   }

   @Override
   public ModelNumber set(double value)
   {
      this.value.set(value);
      return this;
   }

   @Override
   public BigInteger getBigInteger()
   {
      if (!isDefined()) return null;

      return value.asBigInteger();
   }

   @Override
   public ModelNumber set(BigInteger value)
   {
      this.value.set(value);
      return this;
   }

   @Override
   public BigDecimal getBigDecimal()
   {
      if (!isDefined()) return null;

      return value.asBigDecimal();
   }

   @Override
   public ModelNumber set(BigDecimal value)
   {
      this.value.set(value);
      return this;
   }

   @Override
   public ModelNumberType getNumberType()
   {
      switch (value.getType())
      {
         case INT:
            return ModelNumberType.INT;
         case LONG:
            return ModelNumberType.LONG;
         case DOUBLE:
            return ModelNumberType.DOUBLE;
         case BIG_INTEGER:
            return ModelNumberType.BIG_INTEGER;
         case BIG_DECIMAL:
            return ModelNumberType.BIG_DECIMAL;
         default:
            throw new IllegalStateException("Unknown number type for model node type " + value.getType());
      }
   }

   // Overriding to be able to represent a number as a string.
   @Override
   public <T extends ModelValue> T asValue(Class<T> valueType)
   {
      if (ModelString.class.isAssignableFrom(valueType))
      {
         return valueType.cast(new DmrModelString(new ModelNode(value.asString())));
      }
      else
      {
         return super.asValue(valueType);
      }
   }
}
