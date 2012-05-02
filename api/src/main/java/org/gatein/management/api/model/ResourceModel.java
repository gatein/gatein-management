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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Set;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
//TODO: Javadoc, Unit Tests
public interface ResourceModel
{
   ResourceModel get(String name);
   ResourceModel get(String...names);
   ResourceModel get(int index);

   ResourceModel add();

   ResourceModel add(int value);

   ResourceModel add(boolean value);

   ResourceModel add(double value);

   ResourceModel add(long value);

   ResourceModel add(String value);

   ResourceModel add(ResourceModel value);

   ResourceModel set(int value);
   ResourceModel set(String name, int value);

   ResourceModel set(boolean value);
   ResourceModel set(String name, boolean value);

   ResourceModel set(double value);
   ResourceModel set(String name, double value);

   ResourceModel set(long value);
   ResourceModel set(String name, long value);

   ResourceModel set(String value);
   ResourceModel set(String name, String value);

   ResourceModel set(ResourceModel value);
   ResourceModel set(String name, ResourceModel value);

   ResourceModel set(Collection<ResourceModel> values);
   ResourceModel set(String name, Collection<ResourceModel> values);

   ResourceModelType getModelType();

   Set<String> keys();

   String asString(boolean pretty);
   void write(PrintWriter writer, boolean pretty) throws IOException;
}
