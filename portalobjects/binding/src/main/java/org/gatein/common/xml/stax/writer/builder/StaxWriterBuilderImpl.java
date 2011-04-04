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

package org.gatein.common.xml.stax.writer.builder;

import org.gatein.common.xml.stax.writer.StaxWriter;
import org.gatein.common.xml.stax.writer.StaxWriterImpl;
import org.gatein.common.xml.stax.writer.formatting.XmlStreamingFormatter;
import org.staxnav.Naming;
import org.staxnav.StaxNavException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import static org.gatein.common.util.ParameterValidation.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class StaxWriterBuilderImpl implements StaxWriterBuilder
{
   private XMLStreamWriter writer;
   private Object output;
   private String outputEncoding;
   private String version;
   private String encoding;

   private XmlStreamingFormatter formatter;

   private Map<String, Object> properties = new HashMap<String, Object>();
   private Map<String, Object> supportedProperties = new HashMap<String, Object>();

   public StaxWriterBuilder withProperty(String name, Object value)
   {
      throwIllegalArgExceptionIfNull(name, "name");
      throwIllegalArgExceptionIfNull(value, "value");

      properties.put(name, value);
      return this;
   }

   public StaxWriterBuilder withPropertyIfSupported(String name, Object value)
   {
      throwIllegalArgExceptionIfNull(name, "name");
      throwIllegalArgExceptionIfNull(value, "value");

      supportedProperties.put(name, value);
      return this;
   }

   public StaxWriterBuilder withOutputStream(OutputStream outputStream)
   {
      throwIllegalArgExceptionIfNull(outputStream, "outputStream");

      output = outputStream;
      return this;
   }

   public StaxWriterBuilder withOutputStream(OutputStream outputStream, String encoding)
   {
      throwIllegalArgExceptionIfNull(outputStream, "outputStream");
      throwIllegalArgExceptionIfNull(encoding, "encoding");

      output = outputStream;
      outputEncoding = encoding;
      return this;
   }

   public StaxWriterBuilder withWriter(Writer writer)
   {
      throwIllegalArgExceptionIfNull(writer, "writer");

      this.output = writer;
      return this;
   }

   public StaxWriterBuilder withResult(Result result)
   {
      throwIllegalArgExceptionIfNull(result, "result");

      output = result;
      return this;
   }

   public StaxWriterBuilder withXmlStreamWriter(XMLStreamWriter writer)
   {
      throwIllegalArgExceptionIfNull(writer, "XMLStreamWriter");

      this.writer = writer;
      return this;
   }

   public StaxWriterBuilder withEncoding(String encoding)
   {
      throwIllegalArgExceptionIfNull(encoding, "encoding");

      this.encoding = encoding;
      return this;
   }

   public StaxWriterBuilder withVersion(String version)
   {
      throwIllegalArgExceptionIfNull(version, version);

      this.version = version;
      return this;
   }

   public StaxWriterBuilder withFormatting(XmlStreamingFormatter formatter)
   {
      throwIllegalArgExceptionIfNull(formatter, "formatter");

      this.formatter = formatter;
      return this;
   }

   public <N> StaxWriter<N> build(Naming<N> naming) throws StaxNavException, IllegalStateException
   {
      throwIllegalArgExceptionIfNull(naming, "naming");

      if (writer == null && output == null)
         throw new IllegalStateException("Cannot build stax writer. Try calling withOutputStream/withWriter or pass in own XMLStreamWriter.");

      if (writer == null)
      {
         //TODO: Create solution to properly cache XMLOutputFactory
         XMLOutputFactory factory = XMLOutputFactory.newInstance();

         // Set properties
         for (Map.Entry<String, Object> entry : properties.entrySet())
         {
            factory.setProperty(entry.getKey(), entry.getValue());
         }

         // Set properties if supported
         for (Map.Entry<String, Object> entry : supportedProperties.entrySet())
         {
            String name = entry.getKey();
            if (factory.isPropertySupported(name))
            {
               factory.setProperty(name, entry.getValue());
            }
         }

         if (output instanceof OutputStream)
         {
            if (outputEncoding == null)
            {
               try
               {
                  writer = factory.createXMLStreamWriter((OutputStream) output, outputEncoding);
               }
               catch (XMLStreamException e)
               {
                  throw new StaxNavException(e);
               }
            }
            else
            {
               try
               {
                  writer = factory.createXMLStreamWriter((OutputStream) output);
               }
               catch (XMLStreamException e)
               {
                  throw new StaxNavException(null, "Exception creating XMLStreamWriter with OutputStream: " + output, e);
               }
            }
         }
         else if (output instanceof Writer)
         {
            try
            {
               writer = factory.createXMLStreamWriter((Writer) output);
            }
            catch (XMLStreamException e)
            {
               throw new StaxNavException(null, "Exception creating XMLStreamWriter with Writer: " + output, e);
            }
         }
         else if (output instanceof Result)
         {
            try
            {
               writer = factory.createXMLStreamWriter((Result) output);
            }
            catch (XMLStreamException e)
            {
               throw new StaxNavException(null, "Exception creating XMLStreamWriter with Result: " + output, e);
            }
         }
         else
         {
            throw new IllegalStateException("Unknown output: " + output); // should never happen...
         }
      }

      return new StaxWriterImpl<N>(naming, writer, formatter, encoding, version);
   }
}
