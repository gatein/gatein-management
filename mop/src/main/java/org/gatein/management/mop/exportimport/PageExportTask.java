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
import org.exoplatform.portal.pom.config.tasks.PageTask;
import org.exoplatform.portal.pom.data.PageData;
import org.exoplatform.portal.pom.data.PageKey;
import org.gatein.management.api.binding.Marshaller;
import org.gatein.management.api.operation.model.ExportTask;
import org.gatein.management.mop.model.PageDataContainer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PageExportTask extends POMSessionExportTask implements ExportTask
{
   public static final String FILE = "pages.xml";

   private List<String> pageNames;
   private Marshaller<PageDataContainer> marshaller;

   public PageExportTask(SiteKey siteKey, POMSession session, Marshaller<PageDataContainer> marshaller)
   {
      super(siteKey, session);
      this.marshaller = marshaller;
      pageNames = new ArrayList<String>();
   }

   @Override
   public void export(OutputStream outputStream) throws IOException
   {
      List<PageData> pages = new ArrayList<PageData>(pageNames.size());
      for (String pageName : pageNames)
      {
         pages.add(new PageTask.Load(new PageKey(siteKey.getTypeName(), siteKey.getName(), pageName)).run(session));
      }

      marshaller.marshal(new PageDataContainer(pages), outputStream);
   }

   @Override
   protected String getXmlFileName()
   {
      return FILE;
   }

   public void addPageName(String pageName)
   {
      pageNames.add(pageName);
   }

   public List<String> getPageNames()
   {
      return Collections.unmodifiableList(pageNames);
   }
}
