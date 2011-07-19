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

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.pom.data.ModelDataStorage;
import org.exoplatform.portal.pom.data.PageData;
import org.gatein.management.mop.model.PageDataContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PageImportTask extends AbstractImportTask<PageDataContainer>
{
   private final ModelDataStorage dataStorage;
   private PageDataContainer rollbackSaves;
   private PageDataContainer rollbackDeletes;

   public PageImportTask(PageDataContainer data, SiteKey siteKey, ModelDataStorage dataStorage)
   {
      super(data, siteKey);
      this.dataStorage = dataStorage;
   }

   @Override
   public void importData(ImportStrategy strategy) throws Exception
   {
      if (data == null || data.getPages() == null || data.getPages().isEmpty()) return;

      Query<PageData> query = new Query<PageData>(siteKey.getTypeName(), siteKey.getName(), PageData.class);
      LazyPageList<PageData> list = dataStorage.find(query);
      int size = list.getAvailable();

      PageDataContainer dst = null;
      switch (strategy)
      {
         case CONSERVE:
            if (size == 0)
            {
               dst = data; // No pages exist yet.
               rollbackDeletes = data;
            }
            else
            {
               dst = new PageDataContainer(new ArrayList<PageData>());
               List<PageData> existingPages = list.getAll();
               rollbackDeletes = new PageDataContainer(new ArrayList<PageData>());
               for (PageData src : data.getPages())
               {
                  PageData found = findPage(existingPages, src);
                  if (found == null)
                  {
                     dst.getPages().add(src);
                     rollbackDeletes.getPages().add(src);
                  }
               }
            }
            break;
         case MERGE:
            if (size == 0) // No pages exist yet.
            {
               dst = data;
               rollbackDeletes = data;
            }
            else
            {
               dst = new PageDataContainer(new ArrayList<PageData>(data.getPages().size()));
               List<PageData> existingPages = list.getAll();
               rollbackSaves = new PageDataContainer(new ArrayList<PageData>(size));
               rollbackDeletes = new PageDataContainer(new ArrayList<PageData>());
               for (PageData src : data.getPages())
               {
                  dst.getPages().add(src);

                  PageData found = findPage(existingPages, src);
                  if (found == null)
                  {
                     rollbackDeletes.getPages().add(src);
                  }
                  else
                  {
                     rollbackSaves.getPages().add(found);
                  }
               }
            }
            break;
         case OVERWRITE:
            if (size == 0)
            {
               dst = data;
               rollbackDeletes = data;
            }
            else
            {
               List<PageData> existingPages = list.getAll();
               rollbackSaves = new PageDataContainer(new ArrayList<PageData>(size));
               rollbackDeletes = new PageDataContainer(new ArrayList<PageData>());
               for (PageData page : existingPages)
               {
                  dataStorage.remove(page);
                  dataStorage.save();
                  rollbackSaves.getPages().add(page);
               }
               for (PageData src : data.getPages())
               {
                  PageData found = findPage(rollbackSaves.getPages(), src);
                  if (found == null)
                  {
                     rollbackDeletes.getPages().add(src);
                  }
               }

               dst = data;
            }
            break;
      }

      if (dst != null)
      {
         for (PageData page : dst.getPages())
         {
            dataStorage.save(page);
            dataStorage.save();
         }
      }
   }

   @Override
   public void rollback() throws Exception
   {
      if (rollbackDeletes != null && !rollbackDeletes.getPages().isEmpty())
      {
         for (PageData page : rollbackDeletes.getPages())
         {
            dataStorage.remove(page);
            dataStorage.save();
         }
      }
      if (rollbackSaves != null && !rollbackSaves.getPages().isEmpty())
      {
         for (PageData page : rollbackSaves.getPages())
         {
            dataStorage.save(page);
            dataStorage.save();
         }
      }
   }

   PageDataContainer getRollbackSaves()
   {
      return rollbackSaves;
   }

   PageDataContainer getRollbackDeletes()
   {
      return rollbackDeletes;
   }

   private PageData findPage(List<PageData> pages, PageData src)
   {
      PageData found = null;
      for (PageData page : pages)
      {
         if (src.getName().equals(page.getName()))
         {
            found = page;
         }
      }
      return found;
   }
}
