/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.gatein.management.core.api.operation;

import org.gatein.management.api.operation.OperationAttributes;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class OperationAttributesImpl implements OperationAttributes
{
   private Map<String, List<String>> attributes = Collections.emptyMap();

   public OperationAttributesImpl(Map<String, List<String>> attributes)
   {
      if (attributes != null && !attributes.isEmpty())
      {
         this.attributes = new HashMap<String, List<String>>(attributes);
      }
   }

   @Override
   public String getValue(String name)
   {
      List<String> list = getValues(name);
      return (list.isEmpty()) ? null : list.get(0);
   }

   @Override
   public List<String> getValues(String name)
   {
      List<String> list = attributes.get(name);
      if (list == null) return Collections.emptyList();
      
      return Collections.unmodifiableList(list);
   }
}
