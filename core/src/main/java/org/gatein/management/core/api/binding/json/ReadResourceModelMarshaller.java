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
   public void marshal(ReadResourceModel model, OutputStream outputStream) throws BindingException
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

         printWriter.write(new JSONObject(json.toString()).toString(3));
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
