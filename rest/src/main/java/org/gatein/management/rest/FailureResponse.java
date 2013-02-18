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

package org.gatein.management.rest;

import org.gatein.management.api.ContentType;
import org.gatein.management.api.controller.ManagedResponse;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class FailureResponse implements ManagedResponse
{
   private final String operationName;
   private final Outcome outcome;
   private final ContentType contentType;

   public FailureResponse(final String failure, final String operationName, final ContentType contentType)
   {
      this.operationName = operationName;
      this.outcome = new Outcome()
      {
         @Override
         public boolean isSuccess()
         {
            return false;
         }

         @Override
         public String getFailureDescription()
         {
            return failure;
         }
      };
      this.contentType = contentType;
   }

   @Override
   public Outcome getOutcome()
   {
      return outcome;
   }

   @Override
   public Object getResult()
   {
      return null;
   }

   @Override
   public void writeResult(OutputStream outputStream, boolean pretty) throws IOException
   {
      switch (contentType)
      {
         case JSON:
            writeJson(outputStream, pretty);
            break;
         case XML:
            writeXml(outputStream, pretty);
            break;
         default:
            throw new IOException(contentType + " is not a supported content type for a failure.");
      }
   }

   private void writeJson(OutputStream out, boolean pretty) throws IOException
   {
      try
      {
         JSONObject json = new JSONObject();
         json.put("operationName", operationName);
         json.put("failure", outcome.getFailureDescription());

         String text = (pretty) ? json.toString(3) : json.toString();
         out.write(text.getBytes());
         out.write('\n');
      }
      catch (JSONException e)
      {
         throw new IOException("Exception writing failure response to json. The failure response message was '" + outcome.getFailureDescription() + "'", e);
      }
   }

   private void writeXml(OutputStream out, boolean pretty) throws IOException
   {
      XMLStreamWriter writer;
      try
      {
         writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
      }
      catch (XMLStreamException e)
      {
         throw new IOException("Could not create XML streaming writer.", e);
      }

      try
      {
         writer.writeStartDocument("UTF-8", "1.0");
         // root element <failureResult>
         nl(writer, pretty);
         writer.writeStartElement("failureResult");
         nl(writer, pretty);
         indent(writer, 1, pretty);

         // <failure>
         writer.writeStartElement("failure");
         writer.writeCharacters(outcome.getFailureDescription());
         writer.writeEndElement();
         nl(writer, pretty);
         indent(writer, 1, pretty);

         // <operationName>
         writer.writeStartElement("operationName");
         writer.writeCharacters(operationName);
         writer.writeEndElement();
         nl(writer, pretty);

         // </failureResult>
         writer.writeEndElement();

         // End document
         writer.writeCharacters("\n");
         writer.writeEndDocument();
         writer.flush();
      }
      catch (XMLStreamException e)
      {
         throw new IOException("Exception writing failure response to XML. Failure response message was '" + outcome.getFailureDescription() + "'", e);
      }
//      finally
//      {
//         try
//         {
//            writer.close();
//         }
//         catch (XMLStreamException e)
//         {
//            // ignore
//         }
//      }
   }

   private static void nl(XMLStreamWriter writer, boolean pretty) throws XMLStreamException
   {
      if (pretty) writer.writeCharacters("\n");
   }

   private static void indent(XMLStreamWriter writer, int depth, boolean pretty) throws XMLStreamException
   {
      if (pretty)
      {
         for (int i = 0; i < depth; i++)
         {
            writer.writeCharacters("   ");
         }
      }
   }
}
