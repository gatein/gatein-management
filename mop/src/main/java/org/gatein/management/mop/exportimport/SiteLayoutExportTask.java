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

package org.gatein.management.mop.exportimport;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.tasks.PortalConfigTask;
import org.exoplatform.portal.pom.data.PortalData;
import org.exoplatform.portal.pom.data.PortalKey;
import org.gatein.management.api.binding.Marshaller;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class SiteLayoutExportTask extends POMSessionExportTask
{
   public static final String FILE = "portal.xml";

   private Marshaller<PortalData> marshaller;

   public SiteLayoutExportTask(SiteKey siteKey, POMSession session, Marshaller<PortalData> marshaller)
   {
      super(siteKey, session);
      this.marshaller = marshaller;
   }

   @Override
   protected String getXmlFileName()
   {
      return FILE;
   }

   @Override
   public void export(OutputStream outputStream) throws IOException
   {
      PortalData data = new PortalConfigTask.Load(new PortalKey(siteKey.getTypeName(), siteKey.getName())).run(session);

      marshaller.marshal(data, outputStream);
   }
}
