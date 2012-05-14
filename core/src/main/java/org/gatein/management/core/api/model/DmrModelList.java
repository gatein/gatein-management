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

import org.gatein.management.api.model.Model;
import org.gatein.management.api.model.ModelList;
import org.gatein.management.api.model.ModelValue;
import org.jboss.dmr.ModelNode;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class DmrModelList extends DmrModelValue implements ModelList
{
   DmrModelList(ModelNode value)
   {
      super(value);
   }

   @Override
   public ModelValue get(int index)
   {
      int size = (isDefined()) ? value.asInt() : 0;
      if (size <= index) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);

      return asValue(value.get(index));
   }

   @Override
   public <T extends ModelValue> T get(int index, Class<T> valueType)
   {
      return valueType.cast(get(index));
   }

   @Override
   public Model add()
   {
      return new DmrModel(value.add());
   }

   @Override
   public ModelList add(String value)
   {
      this.value.add(value);
      return this;
   }

   @Override
   public ModelList add(int value)
   {
      this.value.add(value);
      return this;
   }

   @Override
   public ModelList add(long value)
   {
      this.value.add(value);
      return this;
   }

   @Override
   public ModelList add(double value)
   {
      this.value.add(value);
      return this;
   }

   @Override
   public ModelList add(BigInteger value)
   {
      this.value.add(value);
      return this;
   }

   @Override
   public ModelList add(BigDecimal value)
   {
      this.value.add(value);
      return this;
   }

   @Override
   public ModelList add(boolean value)
   {
      this.value.add(value);
      return this;
   }

   @Override
   public List<ModelValue> getValues()
   {
      if (!isDefined()) return Collections.emptyList();

      List<ModelNode> list = value.asList();
      List<ModelValue> values = new ArrayList<ModelValue>(list.size());
      for (ModelNode value : list)
      {
         values.add(asValue(value));
      }

      return values;
   }

   @Override
   @SuppressWarnings("unchecked")
   public <T extends ModelValue> List<T> getValues(Class<T> valueType)
   {
      return (List<T>) getValues();
   }

   @Override
   public Iterator<ModelValue> iterator()
   {
      return getValues().iterator();
   }

   @Override
   public int size()
   {
      return getValues().size();
   }
}
