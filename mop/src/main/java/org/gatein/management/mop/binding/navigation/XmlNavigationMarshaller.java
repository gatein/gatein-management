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

package org.gatein.management.mop.binding.navigation;

import org.gatein.management.api.binding.BindingException;
import org.gatein.management.api.binding.Marshaller;
import org.gatein.mop.api.workspace.Navigation;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class XmlNavigationMarshaller implements Marshaller<Navigation>
{
   //TODO: Implment navigation marshalling

   @Override
   public void marshal(Navigation navigation, OutputStream outputStream) throws BindingException
   {
      try
      {
         XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream);
         writer.writeStartDocument();
         writer.writeStartElement("node-navigation");
         writer.writeStartElement("page-nodes");

         marshal(writer, navigation.getChildren());

         writer.writeEndElement();
         writer.writeEndElement();
         writer.writeEndDocument();
      }
      catch (XMLStreamException e)
      {
         throw new BindingException(e);
      }
   }

   @Override
   public Navigation unmarshal(InputStream inputStream) throws BindingException
   {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
   }

   private void marshal(XMLStreamWriter writer, List<Navigation> children) throws XMLStreamException
   {
      for (Navigation nav : children)
      {
         writer.writeStartElement("node");
         writer.writeStartElement("name");
         writer.writeCharacters(nav.getName());
         writer.writeEndElement();
//         marshal(writer, nav.getChildren());
         writer.writeEndElement();
      }
   }
}
