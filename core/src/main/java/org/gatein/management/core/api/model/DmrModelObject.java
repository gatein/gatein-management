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
import org.gatein.management.api.model.ModelObject;
import org.gatein.management.api.model.ModelValue;
import org.jboss.dmr.ModelNode;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Set;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class DmrModelObject extends DmrModelValue implements ModelObject
{
   DmrModelObject()
   {
      this(new ModelNode());
   }

   DmrModelObject(final ModelNode value)
   {
      super(value);
   }

   @Override
   public Model get(String name)
   {
      return new DmrModel(value.get(name));
   }

   @Override
   public Model get(String... names)
   {
      return new DmrModel(value.get(names));
   }

   @Override
   public <T extends ModelValue> T get(String name, Class<T> valueType)
   {
      return get(name).asValue(valueType);
   }

   public ModelObject set(String name, String value)
   {
      get(name).set(value);
      return this;
   }

   public ModelObject set(String name, int value)
   {
      get(name).set(value);
      return this;
   }

   @Override
   public ModelObject set(String name, long value)
   {
      get(name).set(value);
      return this;
   }

   @Override
   public ModelObject set(String name, double value)
   {
      get(name).set(value);
      return this;
   }

   @Override
   public ModelObject set(String name, BigInteger value)
   {
      get(name).set(value);
      return this;
   }

   @Override
   public ModelObject set(String name, BigDecimal value)
   {
      get(name).set(value);
      return this;
   }

   @Override
   public ModelObject set(String name, boolean value)
   {
      get(name).set(value);
      return this;
   }

   @Override
   public ModelObject set(String name, PathAddress value)
   {
      get(name).set(value);
      return this;
   }

   @Override
   public boolean has(String name)
   {
      return value.has(name);
   }

   @Override
   public boolean hasDefined(String name)
   {
      return has(name) && get(name).isDefined();
   }

   @Override
   public ModelValue remove(String name)
   {
      if (!value.hasDefined(name)) return null;

      return asValue(value.remove(name));
   }

   @Override
   public <T extends ModelValue> T remove(String name, Class<T> valueType)
   {
      return valueType.cast(remove(name));
   }

   @Override
   public Set<String> getNames()
   {
      if (!isDefined()) return Collections.emptySet();

      return value.keys();
   }
}
