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

package org.gatein.common.xml.stax.navigator.builder;

import org.staxnav.Naming;
import org.staxnav.StaxNavException;
import org.staxnav.StaxNavigator;
import org.staxnav.StaxNavigatorImpl;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import static org.gatein.common.util.ParameterValidation.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class StaxNavBuilderImpl implements StaxNavBuilder
{
   private Object input;
   private String inputEncoding;
   private XMLStreamReader reader;

   private Map<String, Object> properties = new HashMap<String, Object>();
   private Map<String, Object> supportedProperties = new HashMap<String, Object>();

   @Override
   public StaxNavBuilder withProperty(String name, Object value)
   {
      throwIllegalArgExceptionIfNull(name, "name");
      throwIllegalArgExceptionIfNull(value, "value");

      properties.put(name, value);
      return this;
   }

   @Override
   public StaxNavBuilder withPropertyIfSupported(String name, Object value)
   {
      throwIllegalArgExceptionIfNull(name, "name");
      throwIllegalArgExceptionIfNull(value, "value");

      supportedProperties.put(name, value);
      return this;
   }

   @Override
   public StaxNavBuilder withInputStream(InputStream inputStream)
   {
      throwIllegalArgExceptionIfNull(inputStream, "inputStream");

      input = inputStream;
      return this;
   }

   @Override
   public StaxNavBuilder withInputStream(InputStream inputStream, String encoding)
   {
      throwIllegalArgExceptionIfNull(inputStream, "inputStream");
      throwIllegalArgExceptionIfNull(encoding, "encoding");

      input = inputStream;
      inputEncoding = encoding;
      return this;
   }

   @Override
   public StaxNavBuilder withReader(Reader reader)
   {
      throwIllegalArgExceptionIfNull(reader, "reader");

      this.input = reader;
      return this;
   }

   @Override
   public StaxNavBuilder withSource(Source source)
   {
      throwIllegalArgExceptionIfNull(source, "source");

      input = source;
      return this;
   }

   @Override
   public StaxNavBuilder withXmlStreamReader(XMLStreamReader reader)
   {
      throwIllegalArgExceptionIfNull(reader, "XMLStreamReader");

      this.reader = reader;
      return this;
   }

   @Override
   public <N> StaxNavigator<N> build(Naming<N> naming) throws StaxNavException, IllegalStateException
   {
      throwIllegalArgExceptionIfNull(naming, "naming");

      if (reader == null && input == null)
         throw new IllegalStateException("Cannot build stax reader. Try calling withInputStream/withReader or pass in own XMLStreamReader.");

      if (reader == null)
      {
         //TODO: Create solution to properly cache XMLInputFactory
         XMLInputFactory factory = XMLInputFactory.newInstance();

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

         if (input instanceof InputStream)
         {
            if (inputEncoding == null)
            {
               try
               {
                  reader = factory.createXMLStreamReader((InputStream) input, inputEncoding);
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
                  reader = factory.createXMLStreamReader((InputStream) input);
               }
               catch (XMLStreamException e)
               {
                  throw new StaxNavException("Exception creating XMLStreamReader with inputStream: " + input, e);
               }
            }
         }
         else if (input instanceof Reader)
         {
            try
            {
               reader = factory.createXMLStreamReader((Reader) input);
            }
            catch (XMLStreamException e)
            {
               throw new StaxNavException("Exception creating XMLStreamReader with reader: " + input, e);
            }
         }
         else if (input instanceof Source)
         {
            try
            {
               reader = factory.createXMLStreamReader((Source) input);
            }
            catch (XMLStreamException e)
            {
               throw new StaxNavException("Exception creating XMLStreamReader with Source: " + input, e);
            }
         }
         else
         {
            throw new IllegalStateException("Unknown input: " + input); // should never happen...
         }
      }

      try
      {
         return new StaxNavigatorImpl<N>(naming, reader);
      }
      catch (XMLStreamException e)
      {
         throw new StaxNavException(e);
      }
   }
}
