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

package org.gatein.management.core.api.binding.json;

import org.gatein.common.io.IOTools;
import org.gatein.management.api.binding.BindingException;
import org.gatein.management.api.binding.Marshaller;
import org.gatein.management.api.operation.model.NamedDescription;
import org.gatein.management.api.operation.model.ReadResourceModel;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class ReadResourceModelMarshaller implements Marshaller<ReadResourceModel>
{
   @Override
   public void marshal(ReadResourceModel model, OutputStream outputStream, boolean pretty) throws BindingException
   {
      PrintWriter printWriter = new PrintWriter(outputStream);
      try
      {
         JSONStringer json = new JSONStringer();
         json.object().key("description").value(model.getDescription());
         json.key("children").array();
         for (String child : model.getChildren())
         {
            json.object().key("name").value(child);
            NamedDescription nd = model.getChildDescription(child);
            if (nd != null)
            {
               json.key("description").value(nd.getDescription());
            }
            json.endObject();
         }
         json.endArray().key("operations").array();
         for (NamedDescription nd : model.getOperations())
         {
            json.object().key("operation-name").value(nd.getName()).key("operation-description").value(nd.getDescription()).endObject();
         }
         json.endArray().endObject();

         JSONObject output = new JSONObject(json.toString());
         if (pretty)
         {
            printWriter.write(output.toString(3));
         }
         else
         {
            printWriter.write(output.toString());
         }
         printWriter.flush();
      }
      catch (JSONException e)
      {
         throw new BindingException("Could not marshal to JSON format", e);
      }
      finally
      {
         IOTools.safeClose(printWriter);
      }
   }

   @Override
   public ReadResourceModel unmarshal(InputStream inputStream) throws BindingException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try
      {
         IOTools.copy(inputStream, baos);
      }
      catch (IOException e)
      {
         throw new BindingException("Could not read input stream for marshalling.", e);
      }

      String jsonString = new String(baos.toByteArray());
      try
      {
         JSONObject json = new JSONObject(jsonString);
         json.toString(3);
         throw new BindingException("Marshaller does not support unmarshalling of json data.");
      }
      catch (JSONException e)
      {
         throw new BindingException("Could not parse json string:\n" + jsonString, e);
      }
   }
}
