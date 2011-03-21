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

package org.gatein.management.portalobjects.binding.impl;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.pom.config.Utils;
import org.exoplatform.portal.pom.data.ApplicationData;
import org.exoplatform.portal.pom.data.BodyData;
import org.exoplatform.portal.pom.data.ComponentData;
import org.exoplatform.portal.pom.data.ContainerData;
import org.exoplatform.portal.pom.data.ModelDataStorage;
import org.exoplatform.portal.pom.data.PageData;
import org.exoplatform.portal.pom.spi.gadget.Gadget;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.portlet.PortletBuilder;
import org.exoplatform.portal.pom.spi.portlet.Preference;
import org.gatein.management.binding.api.Marshaller;
import org.gatein.staxbuilder.EnumAttribute;
import org.gatein.staxbuilder.EnumElement;
import org.gatein.staxbuilder.reader.StaxReader;
import org.gatein.staxbuilder.writer.StaxWriter;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public abstract class AbstractPomDataMarshaller<T> implements Marshaller<T>
{
   private static final String PERMISSIONS_SEPARATOR = ";";

   @Override
   public void setCustomContext(Object customContext)
   {
   }

   protected void marshalComponentData(StaxWriter writer, ComponentData componentData) throws XMLStreamException
   {
      if (componentData instanceof ApplicationData)
      {
         ApplicationData applicationData = (ApplicationData) componentData;
         ApplicationType type = applicationData.getType();
         if (ApplicationType.PORTLET == type)
         {
            marshalPortletApplication(writer, safeCast(applicationData, Portlet.class));
         }
         else if (ApplicationType.GADGET == type)
         {
            marshalGadgetApplication(writer, safeCast(applicationData, Gadget.class));
         }
         else if (ApplicationType.WSRP_PORTLET == type)
         {
            throw new XMLStreamException("WSRP portlet marshalling not supported.");
         }
      }
      else if (componentData instanceof PageData)
      {
         //marshalPageData(writer, (PageData) componentData);
         throw new XMLStreamException("Unexpected PageData object. Storage id: " + componentData.getStorageId());
      }
      else if (componentData instanceof ContainerData)
      {
         marshalContainerData(writer, (ContainerData) componentData);
      }
      else if (componentData instanceof BodyData)
      {
         writer.writeStartElement("page-body").writeEndElement();
      }
      else
      {
         throw new XMLStreamException("Unknown ComponentData type " + componentData);
      }
   }

   protected void marshalContainerData(StaxWriter writer, ContainerData componentData) throws XMLStreamException
   {
      writer.writeStartElement(Element.CONTAINER);

      writeOptionalAttribute(writer, Attribute.TEMPLATE, componentData.getTemplate());
      writeOptionalAttribute(writer, Attribute.WIDTH, componentData.getWidth());
      writeOptionalAttribute(writer, Attribute.HEIGHT, componentData.getHeight());

      writer.writeOptionalElement(Element.TITLE, componentData.getTitle());
      writer.writeOptionalElement(Element.DESCRIPTION, componentData.getDescription());

      marshalAccessPermissions(writer, componentData.getAccessPermissions());

      writer.writeOptionalElement(Element.FACTORY_ID, componentData.getFactoryId());

      List<ComponentData> components = componentData.getChildren();
      for (ComponentData component : components)
      {
         marshalComponentData(writer, component);
      }

      writer.writeEndElement(); // End of container element
   }

   protected ContainerData unmarshalContainerData(StaxReader reader) throws XMLStreamException
   {
      String template = null;
      String width = null;
      String height = null;
      String title = null;
      String description = null;
      List<String> accessPermissions = null;
      String factoryId = null;
      List<ComponentData> components = new ArrayList<ComponentData>();

      int count = reader.currentReadEvent().getAttributeCount();
      for (int i=0; i<count; i++)
      {
         String name = reader.currentReadEvent().getAttributeLocalName(i);
         if (Attribute.TEMPLATE.getLocalName().equals(name))
         {
            template = reader.currentReadEvent().getAttributeValue(i);
         }
         else if (Attribute.WIDTH.getLocalName().endsWith(name))
         {
            width = reader.currentReadEvent().getAttributeValue(i);
         }
         else if (Attribute.HEIGHT.getLocalName().equals(name))
         {
            height = reader.currentReadEvent().getAttributeValue(i);
         }
      }
      reader.buildReadEvent().withNestedRead().untilElement(Element.CONTAINER).end();
      while (reader.hasNext())
      {
         switch (reader.read().match().onElement(Element.class, Element.UNKNOWN, Element.SKIP))
         {
            case TITLE:
               title = reader.currentReadEvent().elementText();
               break;
            case DESCRIPTION:
               description = reader.currentReadEvent().elementText();
               break;
            case ACCESS_PERMISSIONS:
               accessPermissions = unmarshalAccessPermissions(reader);
               break;
            case FACTORY_ID:
               factoryId = reader.currentReadEvent().elementText();
               break;
            case CONTAINER:
               components.add(unmarshalContainerData(reader));
               break;
            case PORTLET_APPLICATION:
               components.add(unmarshalPortletApplication(reader));
               break;
            case UNKNOWN:
               throw new XMLStreamException("Unknown element '" + reader.currentReadEvent().getLocalName() +
                  "' while unmarshalling container.", reader.currentReadEvent().getLocation());
            case SKIP:
               break;
            default:
               break;
         }
      }

      return new ContainerData(null, null, null, null, template, factoryId, title, description, width, height, accessPermissions, components);
   }

   protected boolean isContainer(StaxReader reader) throws XMLStreamException
   {
      return isCurrentElement(reader, Element.CONTAINER);
   }

   protected void marshalPortletApplication(StaxWriter writer, ApplicationData<Portlet> portletApplication) throws XMLStreamException
   {
      writer.writeStartElement(Element.PORTLET_APPLICATION).writeStartElement(Element.PORTLET);

      // Marshal ApplicationState
      ApplicationState<Portlet> state = portletApplication.getState();

      // Marshal application state
      String contentId;
      Portlet portlet;
      // If transient we have all the information we need
      if (state instanceof TransientApplicationState)
      {
         TransientApplicationState<Portlet> transientApplicationState = (TransientApplicationState<Portlet>) state;
         contentId = transientApplicationState.getContentId();
         portlet = transientApplicationState.getContentState();
      }
      else
      {
         // The only way to retrieve the information if the state is not transient is if we're within a portal context
         ExoContainer container = ExoContainerContext.getCurrentContainer();
         if (container instanceof PortalContainer)
         {
            ModelDataStorage dataStorage = (ModelDataStorage) container.getComponentInstanceOfType(ModelDataStorage.class);
            try
            {
               portlet = dataStorage.load(state, ApplicationType.PORTLET);
            }
            catch (Exception e)
            {
               throw new XMLStreamException("Could not obtain portlet state from custom context.");
            }

            try
            {
               contentId = dataStorage.getId(state);
            }
            catch (Exception e)
            {
               throw new XMLStreamException("Could not obtain contentId from custom context.", e);
            }
         }
         else
         {
            throw new XMLStreamException("Cannot marshal application state " + state + " outside the context of the portal.");
         }
      }

      // Marshal portlet application id
      if (contentId == null) throw new XMLStreamException("Portlet application ID was null.");
      writer.writeElement(Element.APPLICATION_REF, contentId.substring(0, contentId.indexOf("/")));
      writer.writeElement(Element.PORTLET_REF, contentId.substring(contentId.indexOf("/") + 1, contentId.length()));

      // Marshal preferences
      if (portlet != null)
      {

         boolean prefsWritten = false;
         for (Preference preference : portlet)
         {
            if (!prefsWritten)
            {
               writer.writeStartElement(Element.PREFERENCES);
               prefsWritten = true;
            }
            writer.writeStartElement(Element.PREFERENCE).writeElement(Element.PREFERENCE_NAME, preference.getName());
            //TODO: what to do for multivalue preference values ? i think JiBX accepts multiple values here, xsd does not
            for (String value : preference.getValues())
            {
               writer.writeOptionalElement(Element.PREFERENCE_VALUE, value);
            }
            writer.writeOptionalElement(Element.PREFERENCE_READONLY, String.valueOf(preference.isReadOnly()));
            writer.writeEndElement(); // End of preference
         }
         if (prefsWritten)
         {
            writer.writeEndElement(); // End  of preferences
         }
      }
      writer.writeEndElement(); // End of portlet

      marshalApplication(writer, portletApplication);

      writer.writeEndElement(); // End of portlet-application
   }

   protected ApplicationData<Portlet> unmarshalPortletApplication(StaxReader reader) throws XMLStreamException
   {
      ApplicationState<Portlet> state = null;
      String theme = null;
      String title = null;
      String description = null;
      List<String> accessPermissions = null;
      boolean showInfoBar = false;
      boolean showApplicationState = false;
      boolean showApplicationMode = false;
      String icon = null;
      String width = null;
      String height = null;

      reader.buildReadEvent().withNestedRead().untilElement(Element.PORTLET_APPLICATION).end();
      while(reader.hasNext())
      {
         switch (reader.read().match().onElement(Element.class, Element.UNKNOWN, Element.SKIP))
         {
            case PORTLET:
               state = unmarshalPortletApplicationState(reader);
               break;
            case THEME:
               theme = reader.currentReadEvent().elementText();
               break;
            case TITLE:
               title = reader.currentReadEvent().elementText();
               break;
            case DESCRIPTION:
               description = reader.currentReadEvent().elementText();
               break;
            case ACCESS_PERMISSIONS:
               accessPermissions = unmarshalAccessPermissions(reader);
               break;
            case SHOW_INFO_BAR:
               showInfoBar = Boolean.valueOf(reader.currentReadEvent().elementText());
               break;
            case SHOW_APPLICATION_STATE:
               showApplicationState = Boolean.valueOf(reader.currentReadEvent().elementText());
               break;
            case SHOW_APPLICATION_MODE:
               showApplicationMode = Boolean.valueOf(reader.currentReadEvent().elementText());
               break;
            case ICON:
               icon = reader.currentReadEvent().elementText();
               break;
            case WIDTH:
               width = reader.currentReadEvent().elementText();
               break;
            case HEIGHT:
               height = reader.currentReadEvent().elementText();
               break;
            case UNKNOWN:
               throw new XMLStreamException("Uknown element '" + reader.currentReadEvent().getLocalName() +
                  "' while unmarshalling portlet application.", reader.currentReadEvent().getLocation());
            case SKIP:
               break;
            default:
               break;
         }
      }

      return new ApplicationData<Portlet>(null, null, ApplicationType.PORTLET, state, null, title, icon, description,
         showInfoBar, showApplicationState, showApplicationMode, theme, width, height, Collections.<String, String>emptyMap(), accessPermissions);
   }

   private ApplicationState<Portlet> unmarshalPortletApplicationState(StaxReader reader)
      throws XMLStreamException
   {
      String applicationRef = null;
      String portletRef = null;
      PortletBuilder portletBuilder = null;
      reader.buildReadEvent().withNestedRead().untilElement(Element.PORTLET).end();
      while (reader.hasNext())
      {
         switch (reader.read().match().onElement(Element.class, Element.UNKNOWN, Element.SKIP))
         {
            case APPLICATION_REF:
               applicationRef = reader.currentReadEvent().elementText();
               break;
            case PORTLET_REF:
               portletRef = reader.currentReadEvent().elementText();
               break;
            case PREFERENCES:
               portletBuilder = new PortletBuilder();
               break;
            case PREFERENCE:
               if (portletBuilder == null)
               {
                  throw new XMLStreamException("Cannot have " + Element.PREFERENCE.getLocalName() +
                     " element without parent " + Element.PREFERENCES + " element.");
               }
               String prefName = null;
               List<String> prefValue = null;
               boolean readOnly = true;
               reader.buildReadEvent().withNestedRead().untilElement(Element.PREFERENCE).end();
               while (reader.hasNext())
               {
                  switch (reader.read().match().onElement(Element.class, Element.UNKNOWN, Element.SKIP))
                  {
                     case PREFERENCE_NAME:
                        prefName = reader.currentReadEvent().elementText();
                        break;
                     case PREFERENCE_VALUE:
                        if (prefValue == null) prefValue = new ArrayList<String>();
                        prefValue.add(reader.currentReadEvent().elementText());
                        break;
                     case PREFERENCE_READONLY:
                        readOnly = Boolean.valueOf(reader.currentReadEvent().elementText());
                        break;
                     case UNKNOWN:
                        throw new XMLStreamException("Unknown element '" + reader.currentReadEvent().getLocalName() + "' while unmarshalling preference.",
                           reader.currentReadEvent().getLocation());
                     case SKIP:
                        break;
                     default:
                        break;
                  }
               }
               portletBuilder.add(prefName, prefValue, readOnly);
               break;
            case UNKNOWN:
               throw new XMLStreamException("Unknown element '" + reader.currentReadEvent().getLocalName() +
                  "' while unmarshalling portlet", reader.currentReadEvent().getLocation());
            case SKIP:
               break;
            default:
               break;
         }
      }
      TransientApplicationState<Portlet> state = new TransientApplicationState<Portlet>(applicationRef + "/" + portletRef);
      if (portletBuilder != null)
      {
         state.setContentState(portletBuilder.build());
      }

      return state;
   }

   protected boolean isPortletApplication(StaxReader reader) throws XMLStreamException
   {
      return isCurrentElement(reader, Element.PORTLET_APPLICATION);
   }

   protected void marshalGadgetApplication(StaxWriter writer, ApplicationData<Gadget> gadgetApplication) throws XMLStreamException
   {
      writer.writeStartElement(Element.GADGET_APPLICATION).writeStartElement(Element.GADGET);

      // Marshal ApplicationState
      ApplicationState<Gadget> state = gadgetApplication.getState();

      // Marshal application state
      String contentId;
      Gadget gadget;
      // If transient we have all the information we need
      if (state instanceof TransientApplicationState)
      {
         TransientApplicationState<Gadget> transientApplicationState = (TransientApplicationState<Gadget>) state;
         contentId = transientApplicationState.getContentId();
         gadget = transientApplicationState.getContentState();
      }
      else
      {
         // The only way to retrieve the information if the state is not transient is if we're within a portal context
         ExoContainer container = ExoContainerContext.getCurrentContainer();
         if (container instanceof PortalContainer)
         {
            ModelDataStorage dataStorage = (ModelDataStorage) container.getComponentInstanceOfType(ModelDataStorage.class);
            try
            {
               gadget = dataStorage.load(state, ApplicationType.GADGET);
            }
            catch (Exception e)
            {
               throw new XMLStreamException("Could not obtain gadget state from custom context.");
            }

            try
            {
               contentId = dataStorage.getId(state);
            }
            catch (Exception e)
            {
               throw new XMLStreamException("Could not obtain contentId from custom context.", e);
            }
         }
         else
         {
            throw new XMLStreamException("Cannot marshal application state " + state + " outside the context of the portal.");
         }
      }

      // Marshal portlet application id
      if (contentId == null) throw new XMLStreamException("Gadget content ID was null.");
      writer.writeElement(Element.GADGET_REF, contentId);

      // Marshal preferences
      if (gadget != null)
      {
         //TODO: When user-prefs are supported, uncomment
         //writer.writeOptionalElement(Element.PREFERENCES, gadget.getUserPref());
      }
      writer.writeEndElement(); // End of portlet

      marshalApplication(writer, gadgetApplication);


      writer.writeEndElement(); // End of gadget-application
   }

   protected ApplicationData<Gadget> unmarshalGadgetApplication(StaxReader reader) throws XMLStreamException
   {
      ApplicationState<Gadget> state = null;
      String theme = null;
      String title = null;
      String description = null;
      List<String> accessPermissions = null;
      boolean showInfoBar = false;
      boolean showApplicationState = false;
      boolean showApplicationMode = false;
      String icon = null;
      String width = null;
      String height = null;

      reader.buildReadEvent().withNestedRead().untilElement(Element.GADGET_APPLICATION).end();
      while(reader.hasNext())
      {
         switch (reader.read().match().onElement(Element.class, Element.UNKNOWN, Element.SKIP))
         {
            case GADGET:
               state = unmarshalGadgetApplicationState(reader);
               break;
            case THEME:
               theme = reader.currentReadEvent().elementText();
               break;
            case TITLE:
               title = reader.currentReadEvent().elementText();
               break;
            case DESCRIPTION:
               description = reader.currentReadEvent().elementText();
               break;
            case ACCESS_PERMISSIONS:
               accessPermissions = unmarshalAccessPermissions(reader);
               break;
            case SHOW_INFO_BAR:
               showInfoBar = Boolean.valueOf(reader.currentReadEvent().elementText());
               break;
            case SHOW_APPLICATION_STATE:
               showApplicationState = Boolean.valueOf(reader.currentReadEvent().elementText());
               break;
            case SHOW_APPLICATION_MODE:
               showApplicationMode = Boolean.valueOf(reader.currentReadEvent().elementText());
               break;
            case ICON:
               icon = reader.currentReadEvent().elementText();
               break;
            case WIDTH:
               width = reader.currentReadEvent().elementText();
               break;
            case HEIGHT:
               height = reader.currentReadEvent().elementText();
               break;
            case UNKNOWN:
               throw new XMLStreamException("Uknown element '" + reader.currentReadEvent().getLocalName() +
                  "' while unmarshalling gadget application.", reader.currentReadEvent().getLocation());
            case SKIP:
               break;
            default:
               break;
         }
      }

      return new ApplicationData<Gadget>(null, null, ApplicationType.GADGET, state, null, title, icon, description,
         showInfoBar, showApplicationState, showApplicationMode, theme, width, height, Collections.<String, String>emptyMap(), accessPermissions);
   }

   private ApplicationState<Gadget> unmarshalGadgetApplicationState(StaxReader reader) throws XMLStreamException
   {
      String gadgetRef = null;
      String userPref = null;
      reader.buildReadEvent().withNestedRead().untilElement(Element.GADGET).end();
      while (reader.hasNext())
      {
         switch (reader.read().match().onElement(Element.class, Element.UNKNOWN, Element.SKIP))
         {
            case GADGET_REF:
               gadgetRef = reader.currentReadEvent().elementText();
               break;
            //TODO: When user-prefs are supported uncomment
            /*case PREFERENCES:
               userPref = reader.currentReadEvent().elementText();
               break;*/
            case UNKNOWN:
               throw new XMLStreamException("Unknown element '" + reader.currentReadEvent().getLocalName() +
                  "' while unmarshalling gadget", reader.currentReadEvent().getLocation());
            case SKIP:
               break;
            default:
               break;
         }
      }
      Gadget gadget = new Gadget();
      gadget.setUserPref(userPref);
      return new TransientApplicationState<Gadget>(gadgetRef, gadget);
   }

   protected boolean isGadgetApplication(StaxReader reader) throws XMLStreamException
   {
      return isCurrentElement(reader, Element.GADGET_APPLICATION);
   }

   protected void marshalApplication(StaxWriter writer, ApplicationData<?> application) throws XMLStreamException
   {
      // Theme, Title
      writer.writeOptionalElement(Element.THEME, application.getTheme());
      writer.writeOptionalElement(Element.TITLE, application.getTitle());

      // Access Permissions
      marshalAccessPermissions(writer, application.getAccessPermissions());

      // common application elements
      writer.writeOptionalElement(Element.SHOW_INFO_BAR, String.valueOf(application.isShowInfoBar()));
      writer.writeOptionalElement(Element.SHOW_APPLICATION_STATE, String.valueOf(application.isShowApplicationState()));
      writer.writeOptionalElement(Element.SHOW_APPLICATION_MODE, String.valueOf(application.isShowApplicationMode()));

      // Description, Icon
      writer.writeOptionalElement(Element.DESCRIPTION, application.getDescription());
      writer.writeOptionalElement(Element.ICON, application.getIcon());

      // Width & Height
      writer.writeOptionalElement(Element.WIDTH, application.getWidth());
      writer.writeOptionalElement(Element.HEIGHT, application.getHeight());
   }

   protected void marshalAccessPermissions(StaxWriter writer, List<String> accessPermissionsList) throws XMLStreamException
   {
      String accessPermissions = Utils.join(PERMISSIONS_SEPARATOR, accessPermissionsList);
      if (accessPermissions != null && accessPermissions.trim().length() == 0) accessPermissions = null;
      writer.writeOptionalElement(Element.ACCESS_PERMISSIONS, accessPermissions);
   }

   protected List<String> unmarshalAccessPermissions(StaxReader reader) throws XMLStreamException
   {
      return Arrays.asList(Utils.split(PERMISSIONS_SEPARATOR, reader.currentReadEvent().elementText()));
   }

   protected boolean isAccessPermissions(StaxReader reader) throws XMLStreamException
   {
      return isCurrentElement(reader, Element.ACCESS_PERMISSIONS);
   }

   protected void marshalEditPermission(StaxWriter writer, String editPermission) throws XMLStreamException
   {
      writer.writeOptionalElement(Element.EDIT_PERMISSION, editPermission);
   }

   protected String unmarshalEditPermission(StaxReader reader) throws XMLStreamException
   {
      return reader.currentReadEvent().elementText();
   }

   protected boolean isEditPermission(StaxReader reader) throws XMLStreamException
   {
      return isCurrentElement(reader, Element.EDIT_PERMISSION);
   }

   protected void writeGateinObjectsRootElement(StaxWriter writer) throws XMLStreamException
   {
      String gatein_object_ns = Namespace.CURRENT.getUri();
      String location = new StringBuilder().append(gatein_object_ns).append(" ").append(gatein_object_ns).toString();
      writer.writeDefaultNamespace(gatein_object_ns)
         .writeNamespace("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI)
         .writeAttribute(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation", location);
   }

   protected void writeOptionalDateTime(StaxWriter writer, EnumElement element, Date date) throws XMLStreamException
   {
      if (date == null) return;

      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      writer.writeStartElement(element).writeDateTime(cal).writeEndElement();
   }

   @SuppressWarnings("unchecked")
   private <S> ApplicationData<S> safeCast(ApplicationData data, Class<S> stateClass)
   {
      return (ApplicationData<S>) data;
   }

   private boolean isCurrentElement(StaxReader reader, Element element) throws XMLStreamException
   {
      return reader.currentReadEvent().getLocalName().equals(element.getLocalName());
   }

   private void writeOptionalAttribute(StaxWriter writer, Attribute attribute, String value) throws XMLStreamException
   {
      if (value == null) return;

      writer.writeAttribute(attribute, value);
   }

   private static enum Element implements EnumElement<Element>
   {
      UNKNOWN(null),
      SKIP("skip"),
      TITLE("title"),
      DESCRIPTION("description"),
      FACTORY_ID("factory-id"),
      ACCESS_PERMISSIONS("access-permissions"),
      EDIT_PERMISSION("edit-permission"),
      PORTLET_APPLICATION("portlet-application"),
      GADGET_APPLICATION("gadget-application"),
      CONTAINER("container"),
//      TEMPLATE("template"),
      APPLICATION_REF("application-ref"),
      PORTLET_REF("portlet-ref"),
      PORTLET("portlet"),
      GADGET_REF("gadget-ref"),
      GADGET("gadget"),
      THEME("theme"),
      SHOW_INFO_BAR("show-info-bar"),
      SHOW_APPLICATION_STATE("show-application-state"),
      SHOW_APPLICATION_MODE("show-application-mode"),
      ICON("icon"),
      WIDTH("width"),
      HEIGHT("height"),
      PREFERENCES("preferences"),
      PREFERENCE("preference"),
      PREFERENCE_NAME("name"),
      PREFERENCE_VALUE("value"),
      PREFERENCE_READONLY("read-only"),
      ;
      private final String name;

      Element(final String name)
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

      private static final Map<String, Element> MAP;

      static
      {
         final Map<String, Element> map = new HashMap<String, Element>();
         for (Element element : values())
         {
            final String name = element.getLocalName();
            if (name != null) map.put(name, element);
         }
         MAP = map;
      }

      public static Element forName(String localName)
      {
         final Element element = MAP.get(localName);
         return element == null ? UNKNOWN : element;
      }
   }

   private static enum Attribute implements EnumAttribute<Attribute>
   {
      UNKNOWN(null),
      TEMPLATE("template"),
      WIDTH("width"),
      HEIGHT("height");

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

      private static final Map<String, Attribute> MAP;

      static
      {
         final Map<String, Attribute> map = new HashMap<String, Attribute>();
         for (Attribute attribute : values())
         {
            final String name = attribute.getLocalName();
            if (name != null) map.put(name, attribute);
         }
         MAP = map;
      }

      public static Attribute forName(String localName)
      {
         final Attribute attribute = MAP.get(localName);
         return attribute == null ? UNKNOWN : attribute;
      }
   }
}
