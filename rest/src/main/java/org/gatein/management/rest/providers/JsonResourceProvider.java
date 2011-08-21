package org.gatein.management.rest.providers;

import com.google.gson.stream.JsonWriter;
import org.gatein.management.rest.content.Child;
import org.gatein.management.rest.content.Link;
import org.gatein.management.rest.content.Operation;
import org.gatein.management.rest.content.Resource;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * JSON Provider to control marshalling of a managed resource.
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JsonResourceProvider implements MessageBodyWriter<Resource>
{
   @Override
   public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return (Resource.class.isAssignableFrom(type));
   }

   @Override
   public long getSize(Resource resource, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return -1;
   }

   @Override
   public void writeTo(Resource resource, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException
   {
      JsonWriter writer  =  new JsonWriter(new PrintWriter(entityStream));
      //writer.setIndent("   ");
      writer.beginObject();
      writer.name("description").value(resource.getDescription());
      writer.name("children").beginArray();
      for (Child child : resource.getChildren())
      {
         writeChild(child, writer);
      }
      writer.endArray();
      if (resource.getOperations() != null)
      {
         writer.name("operations").beginArray();
         for (Operation operation : resource.getOperations())
         {
            writeOperation(operation, writer);
         }
         writer.endArray();
      }
      writer.endObject().flush();
   }

   private void writeOperation(Operation operation, JsonWriter writer) throws IOException
   {
      writer.beginObject().name("operation-name").value(operation.getOperationName());
      writer.name("operation-description").value(operation.getOperationDescription());
      writeLink("link", operation.getOperationLink(), writer);
      writer.endObject();
   }

   private void writeChild(Child child, JsonWriter writer) throws IOException
   {
      writer.beginObject().name("name").value(child.getName());
      writeLink("link", child.getLink(), writer);
      writer.endObject();
   }

   private void writeLink(String name, Link link, JsonWriter writer) throws IOException
   {
      writer.name(name).beginObject();
      if (link.getRel() != null)
      {
         writer.name("rel").value(link.getRel());
      }
      writer.name("href").value(link.getHref());
      
      if (link.getType() != null)
      {
         writer.name("type").value(link.getType());
      }
      if (link.getMethod() != null)
      {
         writer.name("method").value(link.getMethod());
      }
      writer.endObject();
   }
}
