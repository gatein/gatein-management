/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.gatein.management.portalobjects.binding.impl.navigation;

import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.pom.data.NavigationData;
import org.exoplatform.portal.pom.data.NavigationNodeData;
import org.gatein.management.binding.api.BindingException;
import org.gatein.management.binding.api.Bindings;
import org.gatein.management.portalobjects.binding.impl.AbstractPomDataMarshaller;
import org.gatein.staxbuilder.reader.NavigationReadEvent;
import org.gatein.staxbuilder.reader.StaxReader;
import org.gatein.staxbuilder.reader.StaxReaderBuilder;
import org.gatein.staxbuilder.writer.StaxWriter;
import org.gatein.staxbuilder.writer.StaxWriterBuilder;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
@Bindings(classes = NavigationData.class)
public class NavigationDataMarshaller extends AbstractPomDataMarshaller<NavigationData>
{

   @Override
   public void marshal(NavigationData object, OutputStream outputStream) throws BindingException
   {
      try
      {
         StaxWriter writer = new StaxWriterBuilder().withOutputStream(outputStream).withEncoding("UTF-8").withDefaultFormatting().build();
         writer.writeStartDocument();

         marshalNavigationData(writer, object);
         writer.writeEndDocument();
      }
      catch (XMLStreamException e)
      {
         throw new BindingException(e);
      }
   }

   @Override
   public void marshalObjects(Collection<NavigationData> objects, OutputStream outputStream) throws BindingException
   {
      throw new BindingException("Marshaller does not support collections.");
   }

   @Override
   public NavigationData unmarshal(InputStream is) throws BindingException
   {
      try
      {
         StaxReader reader = new StaxReaderBuilder().withInputStream(is).build();

         return unmarshalNavigationData(reader);
      }
      catch (XMLStreamException e)
      {
         throw new BindingException(e);
      }
   }

   @Override
   public Collection<NavigationData> unmarshalObjects(InputStream is) throws BindingException
   {
      throw new BindingException("Marshaller does not support collections.");
   }


   private void marshalNavigationData(StaxWriter writer, NavigationData navigation) throws XMLStreamException
   {
      writer.writeStartElement(Element.NODE_NAVIGATION);

      // Write gatein_objects xml namespace
      writeGateinObjectsRootElement(writer);

      // Priority
      writer.writeElement(Element.PRIORITY, String.valueOf(navigation.getPriority()));

      // Page nodes
      writer.writeStartElement(Element.PAGE_NODES);
      List<NavigationNodeData> nodes = navigation.getNodes();
      if (nodes != null && !nodes.isEmpty())
      {
         for (NavigationNodeData node : nodes)
         {
            marshallNavigationNodeData(writer, node);
         }
      }
      writer.writeEndElement().writeEndElement(); // End page-nodes and node-navigation
   }

   public void marshallNavigationNodeData(StaxWriter writer, NavigationNodeData node) throws XMLStreamException
   {
      writer.writeStartElement(Element.NODE);
      writer.writeOptionalElement(Element.URI, node.getURI());
      writer.writeElement(Element.NAME, node.getName());
      writer.writeOptionalElement(Element.LABEL, node.getLabel());
      writer.writeOptionalElement(Element.ICON, node.getIcon());

      writeOptionalDateTime(writer, Element.START_PUBLICATION_DATE, node.getStartPublicationDate());
      writeOptionalDateTime(writer, Element.END_PUBLICATION_DATE, node.getEndPublicationDate());

      String visiblity = (node.getVisibility() == null) ? null : node.getVisibility().name();
      writer.writeOptionalElement(Element.VISIBILITY, visiblity);
      writer.writeOptionalElement(Element.PAGE_REFERENCE, node.getPageReference());

      // Marshall children
      List<NavigationNodeData> children = node.getNodes();
      if (children != null && !children.isEmpty())
      {
         for (NavigationNodeData child : children)
         {
            marshallNavigationNodeData(writer, child);
         }
      }

      writer.writeEndElement(); // End of node
   }

   private NavigationData unmarshalNavigationData(StaxReader reader) throws XMLStreamException
   {
      if (reader.readNextTag().getLocalName().equals(Element.NODE_NAVIGATION.getLocalName()))
      {
         List<NavigationNodeData> nodes = new ArrayList<NavigationNodeData>();
         Integer priority = null;

         // Unmarshal priority
         NavigationReadEvent navigation = reader.buildReadEvent().withNavigation();
         if (navigation.child(Element.PRIORITY.getLocalName()).success())
         {
            priority = Integer.valueOf(navigation.getText());
         }

         if (navigation.sibling(Element.PAGE_NODES.getLocalName()).success())
         {
            while (reader.hasNext())
            {
               switch (reader.read().match().onElement(Element.class, Element.UNKNOWN, Element.SKIP))
               {
                  case NODE:
                     // Unmarshal navigation nodes
                     NavigationNodeData node = unmarshalNavigationNodeData(reader);
                     nodes.add(node);
                     break;
                  case SKIP:
                     break;
                  case UNKNOWN:
                     throw new XMLStreamException("Unknown element " + reader.currentReadEvent().getLocalName(), reader.currentReadEvent().getLocation());
                  default:
                     break;
               }
            }
         }
         else
         {
            throw new XMLStreamException("Unknown child element " + reader.currentReadEvent().getLocalName(), reader.currentReadEvent().getLocation());
         }
         return new NavigationData("", "", priority, nodes);
      }
      else
      {
         throw new XMLStreamException("Unknown root element " + reader.currentReadEvent().getLocalName(), reader.currentReadEvent().getLocation());
      }
   }

   private NavigationNodeData unmarshalNavigationNodeData(StaxReader reader) throws XMLStreamException
   {
      String uri = null;
      String name = null;
      String label = null;
      String icon = null;
      Date start = null;
      Date end = null;
      Visibility visibility = null;
      String pageRef = null;
      List<NavigationNodeData> nodes = new ArrayList<NavigationNodeData>();

      // This will continue reading until the end of the node element.
      reader.buildReadEvent().withNestedRead().untilElement(Element.NODE).end();
      while (reader.hasNext())
      {
         switch (reader.read().match().onElement(Element.class, Element.UNKNOWN, Element.SKIP))
         {
            case URI:
               uri = reader.currentReadEvent().elementText();
               break;
            case NAME:
               name = reader.currentReadEvent().elementText();
               break;
            case LABEL:
               label = reader.currentReadEvent().elementText();
               break;
            case ICON:
               icon = reader.currentReadEvent().elementText();
               break;
            case START_PUBLICATION_DATE:
               start = reader.currentReadEvent().convertElementText(DatatypeConstants.DATETIME, Calendar.class).getTime();
               break;
            case END_PUBLICATION_DATE:
               end = reader.currentReadEvent().convertElementText(DatatypeConstants.DATETIME, Calendar.class).getTime();
               break;
            case VISIBILITY:
               String vis = reader.currentReadEvent().elementText();
               if (vis != null)
               {
                  visibility = Visibility.valueOf(vis.toUpperCase());
               }
               break;
            case PAGE_REFERENCE:
               pageRef = reader.currentReadEvent().elementText();
               break;
            case NODE:
               NavigationNodeData node = unmarshalNavigationNodeData(reader);
               nodes.add(node);
               break;
            case SKIP:
               break;
            case UNKNOWN:
               throw new XMLStreamException("Unknown element.", reader.currentReadEvent().getLocation());
            default:
               break;
         }
      }

      return new NavigationNodeData(uri, label, icon, name, start, end, visibility, pageRef, nodes);
   }
}
