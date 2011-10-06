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

package org.gatein.management.cli.crash.plugins;

import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.PropertyPlugin;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class JaasDomainPropertyPlugin extends CRaSHPlugin<PropertyPlugin> implements PropertyPlugin<String>
{
   static final String JAAS_DOMAIN_PROPERTY = "jaas.domain";

   @Override
   public PropertyPlugin getImplementation()
   {
      return this;
   }

   @Override
   public String getPropertyName()
   {
      return "jaas.domain";
   }

   @Override
   public String getDefaultValue()
   {
      return null;
   }

   @Override
   public String parse(String value)
   {
      return value;
   }
}
