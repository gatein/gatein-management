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

package org.gatein.management.mop.binding.page;

import org.exoplatform.portal.pom.data.PageData;
import org.gatein.management.api.binding.BindingException;
import org.gatein.management.api.binding.Marshaller;
import org.gatein.management.mop.model.PageDataContainer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class XmlPageMarshaller implements Marshaller<PageDataContainer>
{
   //TODO: Implement page marshaller/unmarshaller
   @Override
   public void marshal(PageDataContainer object, OutputStream outputStream) throws BindingException
   {
      try
      {
         XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream);
         writer.writeStartDocument();

         writer.writeStartElement("page-set");
         for (PageData page : object.getPages())
         {
            writer.writeStartElement("page");
            writer.writeStartElement("name");
            writer.writeCharacters(page.getName());
            writer.writeEndElement();
         }
         writer.writeEndElement();
         writer.writeEndDocument();
      }
      catch (XMLStreamException e)
      {
         throw new BindingException(e);
      }
   }

   @Override
   public PageDataContainer unmarshal(InputStream inputStream) throws BindingException
   {
      try
      {
         XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);

         return new PageDataContainer(Collections.<PageData>emptyList());
      }
      catch (XMLStreamException e)
      {
         throw new BindingException(e);
      }
   }
}
