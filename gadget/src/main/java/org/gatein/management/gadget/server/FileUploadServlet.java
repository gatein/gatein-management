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
package org.gatein.management.gadget.server;

import gwtupload.server.UploadAction;
import gwtupload.server.exceptions.UploadActionException;
import org.apache.commons.fileupload.FileItem;
import org.exoplatform.container.ExoContainer;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.gadget.server.util.PortalService;
import org.gatein.management.gadget.server.util.ProcessException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.List;

import static org.gatein.management.gadget.server.ContainerRequestHandler.doInRequest;

/**
 * {@code FileUploadServlet}
 * <p>
 * The file upload servlet based on GWT upload, used for import sites.
 * </p>
 * Created on Jan 3, 2011, 3:43:36 PM
 *
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 * @version 1.0
 */
public class FileUploadServlet extends UploadAction
{

   private static final Logger log = LoggerFactory.getLogger(FileUploadServlet.class);
   private static final long serialVersionUID = 1L;
   private Hashtable<String, String> receivedContentTypes = new Hashtable<String, String>();
   /**
    * Maintain a list with received files and their content types.
    */
   private Hashtable<String, File> receivedFiles = new Hashtable<String, File>();

   /**
    * Override executeAction to save the received files in a custom place
    * and delete this items from session.
    */
   @Override
   public String executeAction(HttpServletRequest request, List<FileItem> sessionFiles) throws UploadActionException
   {
      StringBuilder response = new StringBuilder("<response>\n");
      int count = 0;
      for (FileItem item : sessionFiles)
      {
         //if (false == item.isFormField()) {
         if (!item.isFormField())
         {
            count++;
            try
            {
               // Create a new file based on the remote file name in the client
               String saveName = item.getName().replaceAll("[\\\\/><\\|\\s\"'{}()\\[\\]]+", "_");
               // Create a temporary file placed in the default system temp folder
               File file = File.createTempFile(saveName, ".zip");
               item.write(file);

               // Save a list with the received files
               receivedFiles.put(item.getFieldName(), file);
               receivedContentTypes.put(item.getFieldName(), item.getContentType());

               String overwriteVal = request.getParameter("overwrite");
               boolean overwrite = Boolean.parseBoolean(overwriteVal);

               // process the uploaded file
               processImport(request.getParameter("pc"), new FileInputStream(file), overwrite);
               // Compose a xml message with the full file information which can be parsed in client side
               response.append("<file-").append(count).append("-field>").append(item.getFieldName()).append("</file-").append(count).append("-field>\n");
               response.append("<file-").append(count).append("-name>").append(item.getName()).append("</file-").append(count).append("-name>\n");
               response.append("<file-").append(count).append("-size>").append(item.getSize()).append("</file-").append(count).append("-size>\n");
               response.append("<file-").append(count).append("-type>").append(item.getContentType()).append("</file-").append(count).append("type>\n");
            }
            catch (Exception e)
            {
               throw new UploadActionException(e);
            }
         }
      }

      // Remove files from session because we have a copy of them
      removeSessionFileItems(request);

      // Send information of the received files to the client.
      return response.append("</response>\n").toString();
   }

   /**
    * Get the content of an uploaded file.
    */
   @Override
   public void getUploadedFile(HttpServletRequest request, HttpServletResponse response) throws IOException
   {
      String fieldName = request.getParameter(PARAM_SHOW);
      File f = receivedFiles.get(fieldName);
      if (f != null)
      {
         response.setContentType(receivedContentTypes.get(fieldName));
         FileInputStream is = new FileInputStream(f);
         copyFromInputStreamToOutputStream(is, response.getOutputStream());
      }
      else
      {
         renderXmlResponse(request, response, ERROR_ITEM_NOT_FOUND);
      }
   }

   /**
    * Remove a file when the user sends a delete request.
    */
   @Override
   public void removeItem(HttpServletRequest request, String fieldName) throws UploadActionException
   {
      File file = receivedFiles.get(fieldName);
      receivedFiles.remove(fieldName);
      receivedContentTypes.remove(fieldName);
      if (file != null)
      {
         file.delete();
      }
   }

   /**
    * Try to import the site from the zip file opened by the given input stream
    *
    * @param containerName portal container name for the request.
    * @param in            the input stream pointing to the zip file
    * @param overwrite
    * @throws Exception
    */
   private void processImport(final String containerName, final InputStream in, final boolean overwrite) throws Exception
   {

      doInRequest(containerName, new ContainerCallback<Void>()
      {

         @Override
         public Void doInContainer(ExoContainer container) throws Exception
         {
            try
            {
               PortalService service = PortalService.create(container);
               service.importSite(in, overwrite);
               return null;
            }
            catch (Exception ex)
            {
               throw new ProcessException("Import failed for portal container " + containerName, ex);
            }
         }
      });

   }
}
