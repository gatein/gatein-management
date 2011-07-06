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

package org.gatein.management.mop.binding.xml;

import org.exoplatform.portal.pom.data.BodyData;
import org.exoplatform.portal.pom.data.BodyType;
import org.exoplatform.portal.pom.data.ComponentData;
import org.exoplatform.portal.pom.data.ContainerData;
import org.exoplatform.portal.pom.data.PortalData;
import org.gatein.common.xml.stax.navigator.StaxNavUtils;
import org.gatein.common.xml.stax.writer.StaxWriter;
import org.gatein.management.api.binding.BindingException;
import org.staxnav.StaxNavException;
import org.staxnav.StaxNavigator;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.gatein.common.xml.stax.navigator.Exceptions.*;
import static org.gatein.common.xml.stax.navigator.StaxNavUtils.*;
import static org.gatein.common.xml.stax.writer.StaxWriterUtils.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class SiteLayoutMarshaller extends AbstractMarshaller<PortalData>
{
   @Override
   public void marshal(PortalData object, OutputStream outputStream) throws BindingException
   {
      try
      {
         StaxWriter<Element> writer = createWriter(Element.class, outputStream);

         // root element
         writer.writeStartElement(Element.PORTAL_CONFIG);
         writeGateinObjectsNamespace(writer);

         marshalPortalData(writer, object);

         writer.finish();
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
   public PortalData unmarshal(InputStream is) throws BindingException
   {
      try
      {
         StaxNavigator<Element> navigator = StaxNavUtils.createNavigator(Element.class, Element.UNKNOWN, is);

         if (navigator.getName() == Element.PORTAL_CONFIG)
         {
            return unmarshalPortalData(navigator);
         }
         else
         {
            throw unknownElement(navigator);
         }
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

   private void marshalPortalData(StaxWriter<Element> writer, PortalData portalData) throws XMLStreamException
   {
      writer.writeElement(Element.PORTAL_NAME, portalData.getName());
      writeOptionalElement(writer, Element.LABEL, portalData.getLabel());
      writeOptionalElement(writer, Element.DESCRIPTION, portalData.getDescription());
      writeOptionalElement(writer, Element.LOCALE, portalData.getLocale());

      // Access permissions
      marshalAccessPermissions(writer, portalData.getAccessPermissions());

      // Edit permission
      marshalEditPermission(writer, portalData.getEditPermission());

      writeOptionalElement(writer, Element.SKIN, portalData.getSkin());

      boolean propertiesWritten = false;
      Map<String, String> properties = portalData.getProperties();
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
               writer.writeStartElement(Element.PROPERTIES_ENTRY);
               writer.writeAttribute(Attribute.PROPERTIES_KEY.getLocalName(), key);
               writer.writeContent(value).writeEndElement();
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

   private PortalData unmarshalPortalData(StaxNavigator<Element> navigator) throws XMLStreamException
   {
      String portalName = null;
      String locale = null;
      String label = null;
      String description = null;
      List<String> accessPermissions = Collections.emptyList();
      String editPermission = null;
      String skin = null;
      Map<String, String> properties = Collections.emptyMap();
      ContainerData portalLayout = null;
      List<ComponentData> components = null;

      Element current = navigator.child();
      while (current != null)
      {
         switch (current)
         {
            case PORTAL_NAME:
               portalName = navigator.getContent();
               current = navigator.sibling();
               break;
            case LOCALE:
               locale = navigator.getContent();
               current = navigator.sibling();
               break;
            case LABEL:
               label = navigator.getContent();
               current = navigator.sibling();
               break;
            case DESCRIPTION:
               description = navigator.getContent();
               current = navigator.sibling();
               break;
            case SKIN:
               skin = navigator.getContent();
               current = navigator.sibling();
               break;
            case PROPERTIES:
               properties = new HashMap<String, String>();
               for (StaxNavigator<Element> fork : navigator.fork(Element.PROPERTIES_ENTRY))
               {
                  String key = getRequiredAttribute(fork, Attribute.PROPERTIES_KEY.getLocalName());
                  String value = getRequiredContent(fork, false);
                  properties.put(key, value);
               }
               current = navigator.next();
               break;
            case ACCESS_PERMISSIONS:
               accessPermissions = unmarshalAccessPermissions(navigator, false);
               current = navigator.sibling();
               break;
            case EDIT_PERMISSION:
               editPermission = unmarshalEditPermission(navigator);
               current = navigator.sibling();
               break;
            case PORTAL_LAYOUT:
               components = new ArrayList<ComponentData>();
               current = navigator.child();
               break;
            case PAGE_BODY:
               if (components == null)
               {
                  throw expectedElement(navigator, Element.PORTAL_LAYOUT);
               }
               components.add(new BodyData(null, BodyType.PAGE));
               current = navigator.sibling();
               break;
            case PORTLET_APPLICATION:
               if (components == null)
               {
                  throw expectedElement(navigator, Element.PORTAL_LAYOUT);
               }
               components.add(unmarshalPortletApplication(navigator.fork()));
               current = navigator.getName();
               break;
            case GADGET_APPLICATION:
               if (components == null)
               {
                  throw expectedElement(navigator, Element.PORTAL_LAYOUT);
               }
               components.add(unmarshalGadgetApplication(navigator.fork()));
               current = navigator.getName();
               break;
            case CONTAINER:
               if (components == null)
               {
                  throw expectedElement(navigator, Element.PORTAL_LAYOUT);
               }
               components.add(unmarshalContainerData(navigator.fork()));
               current = navigator.getName();
               break;
            case UNKNOWN:
               throw unknownElement(navigator);
            default:
               throw unexpectedElement(navigator);
         }
      }

      //TODO: We should raise this exception as soon as we know so location is accurate
      if (accessPermissions == null) throw expectedElement(navigator, Element.ACCESS_PERMISSIONS);

      if (components == null) components = Collections.emptyList();
      portalLayout = new ContainerData(null, null, null, null, null, null, null, null, null, null, Collections.<String>emptyList(), components);

      return new PortalData(null, portalName, "", locale, label, description, accessPermissions, editPermission, properties, skin, portalLayout);
   }

   private static enum Attribute
   {
      PROPERTIES_KEY("key");

      private final String name;

      Attribute(final String name)
      {
         this.name = name;
      }

      /**
       * Get the local name of this element.
       *
       * @return the local name
       */
      public String getLocalName()
      {
         return name;
      }
   }
}
