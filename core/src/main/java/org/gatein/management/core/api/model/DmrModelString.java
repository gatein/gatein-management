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

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class DmrModelString extends DmrModelValue implements ModelString
{
   DmrModelString(final ModelNode value)
   {
      super(value);
   }

   @Override
   public String getValue()
   {
      if (!isDefined()) return null;

      return value.asString();
   }

   @Override
   public ModelString set(String value)
   {
      this.value.set(value);
      return this;
   }

   @Override
   public <T extends ModelValue> T asValue(Class<T> valueType)
   {
      if (ModelNumber.class.isAssignableFrom(valueType))
      {
         return valueType.cast(new DmrModelNumber(value));
      }
      else
      {
         return super.asValue(valueType);
      }
   }
}
