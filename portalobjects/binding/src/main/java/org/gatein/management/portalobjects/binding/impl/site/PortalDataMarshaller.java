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

package org.gatein.management.portalobjects.binding.impl.site;

import org.exoplatform.portal.pom.data.BodyData;
import org.exoplatform.portal.pom.data.BodyType;
import org.exoplatform.portal.pom.data.ComponentData;
import org.exoplatform.portal.pom.data.ContainerData;
import org.exoplatform.portal.pom.data.PortalData;
import org.gatein.management.binding.api.BindingException;
import org.gatein.management.binding.api.Bindings;
import org.gatein.management.portalobjects.binding.impl.AbstractPomDataMarshaller;
import org.gatein.staxbuilder.reader.StaxReader;
import org.gatein.staxbuilder.reader.StaxReaderBuilder;
import org.gatein.staxbuilder.writer.StaxWriter;
import org.gatein.staxbuilder.writer.StaxWriterBuilder;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
@Bindings(classes = PortalData.class)
public class PortalDataMarshaller extends AbstractPomDataMarshaller<PortalData>
{
   @Override
   public void marshal(PortalData object, OutputStream outputStream) throws BindingException
   {
      try
      {
         StaxWriter writer = new StaxWriterBuilder().withOutputStream(outputStream).withEncoding("UTF-8").withDefaultFormatting().build();
         writer.writeStartDocument();

         // root element
         writer.writeStartElement(Element.PORTAL_CONFIG);
         writeGateinObjectsRootElement(writer);

         marshalPortalData(writer, object);

         writer.writeEndElement().writeEndDocument(); // End of portal-config, end document
      }
      catch (XMLStreamException e)
      {
         throw new BindingException(e);
      }
   }

   @Override
   public void marshalObjects(Collection<PortalData> objects, OutputStream outputStream) throws BindingException
   {
      try
      {
         StaxWriter writer = new StaxWriterBuilder().withOutputStream(outputStream).
            withEncoding("UTF-8").withDefaultFormatting().build();

         // gatein_objects format does not allow multiple portal config elements, but we can still support the marshalling and unmarshalling of such
         writer.writeStartDocument().writeStartElement("portal-configs");

         for (PortalData data : objects)
         {
            writer.writeStartElement(Element.PORTAL_CONFIG);
            marshalPortalData(writer, data);
            writer.writeEndElement(); // End of portal-config
         }

         writer.writeEndElement().writeEndDocument();
      }
      catch (XMLStreamException e)
      {
         throw new BindingException(e);
      }
   }

   @Override
   public PortalData unmarshal(InputStream is) throws BindingException
   {
      try
      {
         StaxReader reader = new StaxReaderBuilder().withInputStream(is).build();
         if (reader.readNextTag().getLocalName().equals(Element.PORTAL_CONFIG.getLocalName()))
         {
            return unmarshalPortalData(reader);
         }
         else
         {
            throw new XMLStreamException("Unknown root element.", reader.currentReadEvent().getLocation());
         }
      }
      catch (XMLStreamException e)
      {
         throw new BindingException(e);
      }
   }

   @Override
   public Collection<PortalData> unmarshalObjects(InputStream is) throws BindingException
   {
      try
      {
         StaxReader reader = new StaxReaderBuilder().withInputStream(is).build();
         if (reader.readNextTag().getLocalName().equals(Element.PORTAL_CONFIGS.getLocalName()))
         {
            Collection<PortalData> data = new ArrayList<PortalData>();
            while (reader.hasNext())
            {
               switch (reader.read().match().onElement(Element.class, Element.UNKNOWN, Element.SKIP))
               {
                  case PORTAL_CONFIG:
                     data.add(unmarshalPortalData(reader));
                     break;
                  case UNKNOWN:
                     throw new XMLStreamException("Uknown element '" + reader.currentReadEvent().getLocalName() +
                        "' while unmarshalling multiple portal data's.", reader.currentReadEvent().getLocation());
                  case SKIP:
                     break;
                  default:
                     break;
               }
            }

            return data;
         }
         else
         {
            throw new XMLStreamException("Unknown root element.", reader.currentReadEvent().getLocation());
         }
      }
      catch (XMLStreamException e)
      {
         throw new BindingException(e);
      }
   }

   private void marshalPortalData(StaxWriter writer, PortalData portalData) throws XMLStreamException
   {
      writer.writeElement(Element.PORTAL_NAME, portalData.getName());
      writer.writeOptionalElement(Element.LOCALE, portalData.getLocale());

      // Access permissions
      marshalAccessPermissions(writer, portalData.getAccessPermissions());

      // Edit permission
      marshalEditPermission(writer, portalData.getEditPermission());

      writer.writeOptionalElement(Element.SKIN, portalData.getSkin());

      boolean propertiesWritten = false;
      Map<String,String> properties = portalData.getProperties();
      if (properties != null)
      {
         for (String key : properties.keySet())
         {
            if (!propertiesWritten)
            {
               writer.writeStartElement(Element.PROPERTIES);
               propertiesWritten = true;
            }
            String value = properties.get(key);
            if (value != null)
            {
               writer.writeStartElement(Element.PROPERTIES_ENTRY).
                  writeAttribute(Attribute.PROPERTIES_KEY, key).writeCharacters(value).writeEndElement();
            }
         }
         if (propertiesWritten)
         {
            writer.writeEndElement();
         }
      }

      ContainerData container = portalData.getPortalLayout();
      if (container != null)
      {
         writer.writeStartElement(Element.PORTAL_LAYOUT);
         List<ComponentData> children = container.getChildren();
         if (children != null && !children.isEmpty())
         {
            for (ComponentData child : children)
            {
               marshalComponentData(writer, child);
            }
         }
         writer.writeEndElement();
      }
   }

   private PortalData unmarshalPortalData(StaxReader reader) throws XMLStreamException
   {
      String portalName = null;
      String locale = null;
      List<String> accessPermissions = Collections.emptyList();
      String editPermission = null;
      String skin = null;
      Map<String,String> properties = Collections.emptyMap();
      ContainerData portalLayout = null;
      List<ComponentData> components = null;

      reader.buildReadEvent().withNestedRead().untilElement(Element.PORTAL_CONFIG).end();
      while (reader.hasNext())
      {
         switch (reader.read().match().onElement(Element.class, Element.UNKNOWN, Element.SKIP))
         {
            case PORTAL_NAME:
               portalName = reader.currentReadEvent().elementText();
               break;
            case LOCALE:
               locale = reader.currentReadEvent().elementText();
               break;
            case SKIN:
               skin = reader.currentReadEvent().elementText();
               break;
            case PROPERTIES:
               properties = new HashMap<String,String>();
               break;
            case PORTAL_LAYOUT:
               components = new ArrayList<ComponentData>();
               break;
            case PAGE_BODY:
               if (components == null)
               {
                  throw new XMLStreamException("Unexpected " + Element.PAGE_BODY.getLocalName() +" without parent " + Element.PORTAL_LAYOUT.getLocalName());
               }
               components.add(new BodyData(null, BodyType.PAGE));
               break;
            case PROPERTIES_ENTRY:
               int count = reader.currentReadEvent().getAttributeCount();
               if (count == 0)
               {
                  throw new XMLStreamException("No attribute for properties entry element.", reader.currentReadEvent().getLocation());
               }
               for (int i=0; i<count; i++)
               {
                  String name = reader.currentReadEvent().getAttributeLocalName(i);
                  if (Attribute.PROPERTIES_KEY.getLocalName().equals(name))
                  {
                     String key = reader.currentReadEvent().getAttributeValue(i);
                     String value = reader.currentReadEvent().elementText();
                     properties.put(key, value);
                     break;
                  }
               }
               break;
            case SKIP:
               break;
            case UNKNOWN:
               if (isAccessPermissions(reader))
               {
                  accessPermissions = unmarshalAccessPermissions(reader);
               }
               else if (isEditPermission(reader))
               {
                  editPermission = unmarshalEditPermission(reader);
               }
               else if (isPortletApplication(reader))
               {
                  if (components == null)
                  {
                     throw new XMLStreamException("Unexpected portlet application without parent " + Element.PORTAL_LAYOUT.getLocalName());
                  }
                  components.add(unmarshalPortletApplication(reader));
               }
               else if (isContainer(reader))
               {
                  if (components == null)
                  {
                     throw new XMLStreamException("Unexpected container without parent " + Element.PORTAL_LAYOUT.getLocalName());
                  }
                  components.add(unmarshalContainerData(reader));
               }
               else
               {
                  throw new XMLStreamException("Unknown element '" + reader.currentReadEvent().getLocalName() + "' while unmarshalling portal data.");
               }
               break;
            default:
               break;
         }
      }
      portalLayout = new ContainerData(null, null, null, null, null, null, null, null, null, null, Collections.<String>emptyList(), components);
      return new PortalData(null, portalName, "", locale, accessPermissions, editPermission, properties, skin, portalLayout);
   }
}
