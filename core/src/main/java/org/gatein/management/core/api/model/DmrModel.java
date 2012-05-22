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
import org.gatein.management.api.model.ModelBoolean;
import org.gatein.management.api.model.ModelList;
import org.gatein.management.api.model.ModelNumber;
import org.gatein.management.api.model.ModelObject;
import org.gatein.management.api.model.ModelString;
import org.jboss.dmr.ModelNode;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class DmrModel extends DmrModelValue implements Model
{
   DmrModel(ModelNode value)
   {
      super(value);
   }

   @Override
   public ModelString set(String value)
   {
      if (value == null) return asValue(ModelString.class);

      return (ModelString) asValue(this.value.set(value));
   }

   @Override
   public ModelNumber set(int value)
   {
      return (ModelNumber) asValue(this.value.set(value));
   }

   @Override
   public ModelNumber set(long value)
   {
      return (ModelNumber) asValue(this.value.set(value));
   }

   @Override
   public ModelNumber set(double value)
   {
      return (ModelNumber) asValue(this.value.set(value));
   }

   @Override
   public ModelNumber set(BigInteger value)
   {
      if (value == null) return asValue(ModelNumber.class);

      return (ModelNumber) asValue(this.value.set(value));
   }

   @Override
   public ModelNumber set(BigDecimal value)
   {
      if (value == null) return asValue(ModelNumber.class);

      return (ModelNumber) asValue(this.value.set(value));
   }

   @Override
   public ModelBoolean set(boolean value)
   {
      return (ModelBoolean) asValue(this.value.set(value));
   }

   @Override
   public ModelObject setEmptyObject()
   {
      return (ModelObject) asValue(this.value.setEmptyObject());
   }

   @Override
   public ModelList setEmptyList()
   {
      return (ModelList) asValue(this.value.setEmptyList());
   }
}
