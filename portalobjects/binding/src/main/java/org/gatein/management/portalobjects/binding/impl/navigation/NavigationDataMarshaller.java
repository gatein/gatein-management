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

package org.gatein.management.portalobjects.binding.impl.navigation;

import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.pom.data.NavigationData;
import org.exoplatform.portal.pom.data.NavigationNodeData;
import org.gatein.common.xml.stax.navigator.StaxNavUtils;
import org.gatein.common.xml.stax.writer.StaxWriter;
import org.gatein.common.xml.stax.writer.WritableValueTypes;
import org.gatein.management.binding.api.BindingException;
import org.gatein.management.binding.api.Bindings;
import org.gatein.management.portalobjects.binding.impl.AbstractPomDataMarshaller;
import org.gatein.management.portalobjects.binding.impl.Element;
import org.staxnav.StaxNavigator;
import org.staxnav.ValueType;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.gatein.common.xml.stax.navigator.Exceptions.*;
import static org.gatein.common.xml.stax.writer.StaxWriterUtils.*;

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
         StaxWriter<Element> writer = createWriter(Element.class, outputStream);
         marshalNavigationData(writer, object);
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
         StaxNavigator<Element> navigator = StaxNavUtils.createNavigator(Element.class, Element.UNKNOWN, is);
         return unmarshalNavigationData(navigator);
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


   private void marshalNavigationData(StaxWriter<Element> writer, NavigationData navigation) throws XMLStreamException
   {
      writer.writeStartElement(Element.NODE_NAVIGATION);

      // Write gatein_objects xml namespace
      //writeGateinObjectsNamespace(writer);

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

   private NavigationData unmarshalNavigationData(StaxNavigator<Element> navigator) throws XMLStreamException
   {
      if (navigator.getName() == Element.NODE_NAVIGATION)
      {
         List<NavigationNodeData> nodes = new ArrayList<NavigationNodeData>();
         Integer priority = null;

         // Unmarshal priority
         if (navigator.child(Element.PRIORITY))
         {
            priority = Integer.valueOf(navigator.parseContent(ValueType.INTEGER));
         }

         if (navigator.sibling(Element.PAGE_NODES))
         {
            if (navigator.child(Element.NODE))
            {
               for (StaxNavigator<Element> fork : navigator.fork(Element.NODE))
               {
                  nodes.add(unmarshalNavigationNodeData(fork));
               }
            }
            else
            {
               throw unknownElement(navigator);
            }
         }
         else
         {
            throw unknownElement(navigator);
         }
         return new NavigationData("", "", priority, nodes);
      }
      else
      {
         throw unknownElement(navigator);
      }
   }

   private NavigationNodeData unmarshalNavigationNodeData(StaxNavigator<Element> navigator) throws XMLStreamException
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

      Set<Element> children = new HashSet<Element>();
      children.add(Element.URI);
      children.add(Element.NAME);
      children.add(Element.LABEL);
      children.add(Element.ICON);
      children.add(Element.START_PUBLICATION_DATE);
      children.add(Element.END_PUBLICATION_DATE);
      children.add(Element.VISIBILITY);
      children.add(Element.PAGE_REFERENCE);
      children.add(Element.NODE);

      while (navigator.next(children) != null)
      {
         switch (navigator.getName())
         {
            case URI:
               uri = navigator.getContent();
               break;
            case NAME:
               name = navigator.getContent();
               break;
            case LABEL:
               label = navigator.getContent();
               break;
            case ICON:
               icon = navigator.getContent();
               break;
            case START_PUBLICATION_DATE:
               start = navigator.parseContent(ValueType.DATE_TIME);
               break;
            case END_PUBLICATION_DATE:
               end = navigator.parseContent(ValueType.DATE_TIME);
               break;
            case VISIBILITY:
               visibility = navigator.parseContent(ValueType.get(Visibility.class));
               break;
            case PAGE_REFERENCE:
               pageRef = navigator.getContent();
               break;
            case NODE:
               NavigationNodeData node = unmarshalNavigationNodeData(navigator.fork());
               nodes.add(node);
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
