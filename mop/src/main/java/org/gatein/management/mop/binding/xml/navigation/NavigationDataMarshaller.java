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

package org.gatein.management.mop.binding.xml.navigation;

import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.pom.data.NavigationData;
import org.exoplatform.portal.pom.data.NavigationNodeData;
import org.gatein.common.xml.stax.writer.StaxWriter;
import org.gatein.common.xml.stax.writer.WritableValueTypes;
import org.gatein.management.api.binding.BindingException;
import org.gatein.management.mop.binding.xml.AbstractPomDataMarshaller;
import org.gatein.management.mop.binding.xml.Element;
import org.staxnav.StaxNavException;
import org.staxnav.StaxNavigator;
import org.staxnav.ValueType;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.gatein.common.xml.stax.navigator.Exceptions.*;
import static org.gatein.common.xml.stax.navigator.StaxNavUtils.*;
import static org.gatein.common.xml.stax.writer.StaxWriterUtils.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class NavigationDataMarshaller extends AbstractPomDataMarshaller<NavigationData>
{

   @Override
   public void marshal(NavigationData object, OutputStream outputStream) throws BindingException
   {
      try
      {
         StaxWriter<Element> writer = createWriter(Element.class, outputStream);
         marshalNavigationData(writer, object);
      }
      catch (StaxNavException e)
      {
         throw new BindingException(e);
      }
      catch (XMLStreamException e)
      {
         throw new BindingException(e);
      }
   }

   @Override
   public NavigationData unmarshal(InputStream is) throws BindingException
   {
      try
      {
         StaxNavigator<Element> navigator = createNavigator(Element.class, Element.UNKNOWN, is);
         return unmarshalNavigationData(navigator);
      }
      catch (StaxNavException e)
      {
         throw new BindingException(e);
      }
   }

   private void marshalNavigationData(StaxWriter<Element> writer, NavigationData navigation) throws XMLStreamException
   {
      writer.writeStartElement(Element.NODE_NAVIGATION);

      // Write gatein_objects xml namespace
      writeGateinObjectsNamespace(writer);

      // Priority
      writer.writeElement(Element.PRIORITY, WritableValueTypes.INTEGER, navigation.getPriority());

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

   public void marshallNavigationNodeData(StaxWriter<Element> writer, NavigationNodeData node) throws XMLStreamException
   {
      writer.writeStartElement(Element.NODE);
      writeOptionalElement(writer, Element.URI, node.getURI());
      writer.writeElement(Element.NAME, node.getName());
      writeOptionalElement(writer, Element.LABEL, node.getLabel());
      writeOptionalElement(writer, Element.ICON, node.getIcon());

      writeOptionalElement(writer, Element.START_PUBLICATION_DATE, WritableValueTypes.DATE_TIME, node.getStartPublicationDate());
      writeOptionalElement(writer, Element.END_PUBLICATION_DATE, WritableValueTypes.DATE_TIME, node.getEndPublicationDate());

      String visibility = (node.getVisibility() == null) ? null : node.getVisibility().name();
      writeOptionalElement(writer, Element.VISIBILITY, visibility);
      writeOptionalElement(writer, Element.PAGE_REFERENCE, node.getPageReference());

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

   private NavigationData unmarshalNavigationData(StaxNavigator<Element> navigator) throws StaxNavException
   {
      if (navigator.getName() == Element.NODE_NAVIGATION)
      {
         Element next = navigator.child();
         if (next != Element.PRIORITY)
         {
            throw expectedElement(navigator, Element.PRIORITY);
         }
         Integer priority = parseRequiredContent(navigator, ValueType.INTEGER);

         List<NavigationNodeData> nodes = new ArrayList<NavigationNodeData>();

         next = navigator.sibling();
         if (next == Element.PAGE_NODES)
         {
            next = navigator.child();
            if (next == Element.NODE)
            {
               for (StaxNavigator<Element> fork : navigator.fork(Element.NODE))
               {
                  nodes.add(unmarshalNavigationNodeData(fork));
               }
            }
            else if (next != null)
            {
               throw unknownElement(navigator);
            }
         }
         else if (next != null)
         {
            throw expectedElement(navigator, Element.PAGE_NODES);
         }

         return new NavigationData("", "", priority, nodes);
      }
      else
      {
         throw unknownElement(navigator);
      }
   }

   private NavigationNodeData unmarshalNavigationNodeData(StaxNavigator<Element> navigator) throws StaxNavException
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

      Element current = navigator.child();
      while (current != null)
      {
         switch (navigator.getName())
         {
            case URI:
               uri = navigator.getContent();
               current = navigator.sibling();
               break;
            case NAME:
               name = navigator.getContent();
               current = navigator.sibling();
               break;
            case LABEL:
               label = navigator.getContent();
               current = navigator.sibling();
               break;
            case ICON:
               icon = navigator.getContent();
               current = navigator.sibling();
               break;
            case START_PUBLICATION_DATE:
               start = navigator.parseContent(ValueType.DATE_TIME);
               current = navigator.sibling();
               break;
            case END_PUBLICATION_DATE:
               end = navigator.parseContent(ValueType.DATE_TIME);
               current = navigator.sibling();
               break;
            case VISIBILITY:
               visibility = navigator.parseContent(ValueType.get(Visibility.class));
               current = navigator.sibling();
               break;
            case PAGE_REFERENCE:
               pageRef = navigator.getContent();
               current = navigator.sibling();
               break;
            case NODE:
               NavigationNodeData node = unmarshalNavigationNodeData(navigator.fork());
               nodes.add(node);
               current = navigator.getName();
               break;
            case UNKNOWN:
               throw unknownElement(navigator);
            default:
               throw unexpectedElement(navigator);
         }
      }

      return new NavigationNodeData(uri, label, icon, name, start, end, visibility, pageRef, nodes);
   }
}
