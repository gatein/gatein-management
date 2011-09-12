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

package org.gatein.management.core.api.binding.zip;

import org.gatein.common.io.IOTools;
import org.gatein.management.api.binding.BindingException;
import org.gatein.management.api.binding.Marshaller;
import org.gatein.management.api.operation.model.ExportResourceModel;
import org.gatein.management.api.operation.model.ExportTask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class ExportResourceModelMarshaller implements Marshaller<ExportResourceModel>
{
   @Override
   public void marshal(ExportResourceModel model, OutputStream outputStream) throws BindingException
   {
      File file;
      ZipOutputStream zos = null;
      try
      {
         file = File.createTempFile("gatein-export", ".zip");
         zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
      }
      catch (IOException e)
      {
         throw new BindingException("Could not create temp file for export.", e);
      }

      try
      {
         if (model.getTasks().isEmpty())
         {
            zos.putNextEntry(new ZipEntry(""));
         }
         else
         {
            for (ExportTask task : model.getTasks())
            {
               String entry = task.getEntry();
               zos.putNextEntry(new ZipEntry(entry));

               // Call export task responsible for writing the data.
               task.export(zos);

               zos.closeEntry();
            }
         }
         zos.flush();
         zos.finish();
      }
      catch (Throwable t)
      {
         throw new BindingException("Exception writing data to zip.", t);
      }
      finally
      {
         IOTools.safeClose(zos);
      }

      try
      {
         InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
         IOTools.copy(inputStream, outputStream);
      }
      catch (FileNotFoundException e)
      {
         throw new BindingException("Could not read from temporary zip file " + file, e);
      }
      catch (IOException e)
      {
         throw new BindingException("IOException writing data to final output stream.", e);
      }
   }

   @Override
   public ExportResourceModel unmarshal(InputStream inputStream) throws BindingException
   {
      throw new UnsupportedOperationException();
   }
}
