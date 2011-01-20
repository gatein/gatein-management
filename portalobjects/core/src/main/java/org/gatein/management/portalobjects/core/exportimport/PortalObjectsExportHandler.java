/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.gatein.management.portalobjects.core.exportimport;

import org.gatein.management.binding.api.BindingProvider;

import org.gatein.management.portalobjects.api.exportimport.ExportContext;
import org.gatein.management.portalobjects.api.exportimport.ExportHandler;
import org.gatein.management.portalobjects.common.exportimport.ExportImportUtils;
import org.gatein.management.portalobjects.common.exportimport.PortalObjectsContext;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PortalObjectsExportHandler implements ExportHandler
{
   private BindingProvider bindingProvider;

   public PortalObjectsExportHandler(BindingProvider bindingProvider)
   {
      this.bindingProvider = bindingProvider;
   }

   @Override
   public ExportContext createExportContext()
   {
      return new PortalObjectsContext();
   }

   @Override
   public void exportContext(ExportContext context, OutputStream out) throws IOException
   {
      ExportImportUtils.exportAsZip(bindingProvider, context, out);
   }
}
