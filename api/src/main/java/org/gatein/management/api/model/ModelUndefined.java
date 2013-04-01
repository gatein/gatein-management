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

package org.gatein.management.api.model;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * A <code>ModelValue</code> representing something that hasn't been defined yet.
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public interface ModelUndefined extends ModelValue
{
   ModelString set(String value);

   ModelNumber set(int value);

   ModelNumber set(long value);

   ModelNumber set(double value);

   ModelNumber set(BigInteger value);

   ModelNumber set(BigDecimal value);

   ModelBoolean set(boolean value);

   ModelObject setEmptyObject();

   ModelList setEmptyList();
}
