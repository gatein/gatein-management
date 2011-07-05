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

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.TransientApplicationState;
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
import org.gatein.common.xml.stax.writer.StaxWriter;
import org.gatein.common.xml.stax.writer.WritableValueTypes;
import org.gatein.management.api.binding.Marshaller;
import org.staxnav.StaxNavigator;
import org.staxnav.ValueType;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.gatein.common.xml.stax.navigator.Exceptions.*;
import static org.gatein.common.xml.stax.navigator.StaxNavUtils.*;
import static org.gatein.common.xml.stax.writer.StaxWriterUtils.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public abstract class AbstractPomDataMarshaller<T> implements Marshaller<T>
{
   protected void marshalComponentData(StaxWriter<Element> writer, ComponentData componentData) throws XMLStreamException
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
         writer.writeStartElement(Element.PAGE_BODY).writeEndElement();
      }
      else
      {
         throw new XMLStreamException("Unknown ComponentData type " + componentData);
      }
   }

   protected void marshalContainerData(StaxWriter<Element> writer, ContainerData componentData) throws XMLStreamException
   {
      writer.writeStartElement(Element.CONTAINER);

      writeOptionalAttribute(writer, Attribute.ID, componentData.getId());
      writeOptionalAttribute(writer, Attribute.TEMPLATE, componentData.getTemplate());
      writeOptionalAttribute(writer, Attribute.WIDTH, componentData.getWidth());
      writeOptionalAttribute(writer, Attribute.HEIGHT, componentData.getHeight());

      writeOptionalElement(writer, Element.NAME, componentData.getName());
      writeOptionalElement(writer, Element.TITLE, componentData.getTitle());
      writeOptionalElement(writer, Element.ICON, componentData.getIcon());
      writeOptionalElement(writer, Element.DESCRIPTION, componentData.getDescription());

      marshalAccessPermissions(writer, componentData.getAccessPermissions());

      writeOptionalElement(writer, Element.FACTORY_ID, componentData.getFactoryId());

      List<ComponentData> components = componentData.getChildren();
      for (ComponentData component : components)
      {
         marshalComponentData(writer, component);
      }

      writer.writeEndElement(); // End of container element
   }

   protected ContainerData unmarshalContainerData(StaxNavigator<Element> navigator) throws XMLStreamException
   {
      String id = navigator.getAttribute(Attribute.ID.getLocalName());
      String template = navigator.getAttribute(Attribute.TEMPLATE.getLocalName());
      String width = navigator.getAttribute(Attribute.WIDTH.getLocalName());
      String height = navigator.getAttribute(Attribute.HEIGHT.getLocalName());

      String name = null;
      String title = null;
      String icon = null;
      String description = null;
      List<String> accessPermissions = null;
      String factoryId = null;
      List<ComponentData> components = new ArrayList<ComponentData>();

      Element current = navigator.child();
      while (current != null)
      {
         switch (current)
         {
            case NAME:
               name = navigator.getContent();
               current = navigator.sibling();
               break;
            case TITLE:
               title = navigator.getContent();
               current = navigator.sibling();
               break;
            case ICON:
               icon = navigator.getContent();
               current = navigator.sibling();
               break;
            case DESCRIPTION:
               description = navigator.getContent();
               current = navigator.sibling();
               break;
            case ACCESS_PERMISSIONS:
               accessPermissions = unmarshalAccessPermissions(navigator, false);
               current = navigator.sibling();
               break;
            case FACTORY_ID:
               factoryId = navigator.getContent();
               current = navigator.sibling();
               break;
            case CONTAINER:
               components.add(unmarshalContainerData(navigator.fork()));
               current = navigator.getName();
               break;
            case PORTLET_APPLICATION:
               components.add(unmarshalPortletApplication(navigator.fork()));
               current = navigator.getName();
               break;
            case GADGET_APPLICATION:
               components.add(unmarshalGadgetApplication(navigator.fork()));
               current = navigator.getName();
               break;
            case UNKNOWN:
               throw unknownElement(navigator);
            default:
               throw unexpectedElement(navigator);
         }
      }

      return new ContainerData(null, id, name, icon, template, factoryId, title, description, width, height, accessPermissions, components);
   }

   protected void marshalPortletApplication(StaxWriter<Element> writer, ApplicationData<Portlet> portletApplication) throws XMLStreamException
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
         // The only way to retrieve the information if the state is not transient is if we're within the portal context
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
               throw new XMLStreamException("Could not obtain portlet state.");
            }

            try
            {
               contentId = dataStorage.getId(state);
            }
            catch (Exception e)
            {
               throw new XMLStreamException("Could not obtain contentId.", e);
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

            writer.writeStartElement(Element.PREFERENCE);
            writer.writeElement(Element.NAME, preference.getName());
            for (String value : preference.getValues())
            {
               writeOptionalContent(writer, Element.PREFERENCE_VALUE, value);
            }
            writer.writeElement(Element.PREFERENCE_READONLY, WritableValueTypes.BOOLEAN, preference.isReadOnly());
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

   protected ApplicationData<Portlet> unmarshalPortletApplication(StaxNavigator<Element> navigator) throws XMLStreamException
   {
      String theme = null;
      String title = null;
      String description = null;
      List<String> accessPermissions = null;
      Boolean showInfoBar = null;
      boolean showApplicationState = false;
      boolean showApplicationMode = false;
      String icon = null;
      String width = null;
      String height = null;

      requiresChild(navigator, Element.PORTLET);
      ApplicationState<Portlet> state = unmarshalPortletApplicationState(navigator.fork());

      Element current = navigator.getName();
      while (current != null)
      {
         switch (current)
         {
            case THEME:
               theme = navigator.getContent();
               current = navigator.sibling();
               break;
            case TITLE:
               title = navigator.getContent();
               current = navigator.sibling();
               break;
            case ACCESS_PERMISSIONS:
               accessPermissions = unmarshalAccessPermissions(navigator, true);
               current = navigator.sibling();
               break;
            case SHOW_INFO_BAR:
               showInfoBar = parseRequiredContent(navigator, ValueType.BOOLEAN);
               current = navigator.sibling();
               break;
            case SHOW_APPLICATION_STATE:
               showApplicationState = navigator.parseContent(ValueType.BOOLEAN);
               current = navigator.sibling();
               break;
            case SHOW_APPLICATION_MODE:
               showApplicationMode = navigator.parseContent(ValueType.BOOLEAN);
               current = navigator.sibling();
               break;
            case DESCRIPTION:
               description = navigator.getContent();
               current = navigator.sibling();
               break;
            case ICON:
               icon = navigator.getContent();
               current = navigator.sibling();
               break;
            case WIDTH:
               width = navigator.getContent();
               current = navigator.sibling();
               break;
            case HEIGHT:
               height = navigator.getContent();
               current = navigator.sibling();
               break;
            case UNKNOWN:
               throw unknownElement(navigator);
            default:
               throw unexpectedElement(navigator);
         }
      }

      //TODO: We should raise this exception as soon as we know so location is accurate
      if (accessPermissions == null) throw expectedElement(navigator, Element.ACCESS_PERMISSIONS);
      if (showInfoBar == null) throw expectedElement(navigator, Element.SHOW_INFO_BAR);

      return new ApplicationData<Portlet>(null, null, ApplicationType.PORTLET, state, null, title, icon, description,
         showInfoBar, showApplicationState, showApplicationMode, theme, width, height, Collections.<String, String>emptyMap(), accessPermissions);
   }

   private ApplicationState<Portlet> unmarshalPortletApplicationState(StaxNavigator<Element> navigator) throws XMLStreamException
   {
      // Application name
      requiresChild(navigator, Element.APPLICATION_REF);
      String applicationRef = getRequiredContent(navigator, true);

      // Portlet name
      requiresSibling(navigator, Element.PORTLET_REF);
      String portletRef = getRequiredContent(navigator, true);

      // Preferences
      PortletBuilder portletBuilder = null;
      if (navigator.sibling() == Element.PREFERENCES)
      {
         requiresChild(navigator, Element.PREFERENCE);
         portletBuilder = new PortletBuilder();
         for (StaxNavigator<Element> fork : navigator.fork(Element.PREFERENCE))
         {
            // Preference name
            requiresChild(fork, Element.NAME);
            String prefName = getRequiredContent(fork, false);

            // Preference values
            List<String> values = null;
            while (fork.sibling() == Element.PREFERENCE_VALUE)
            {
               if (values == null) values = new ArrayList<String>();
               values.add(getContent(fork, false));
            }
            if (values == null)
            {
               values = Collections.singletonList(null);
            }

            // Preference readonly
            Boolean readOnly = null;
            if (fork.getName() == Element.PREFERENCE_READONLY)
            {
               readOnly = parseRequiredContent(fork,  ValueType.BOOLEAN);
            }

            // Ensure nothing is left.
            if (fork.next() != null)
            {
               throw unexpectedElement(fork);
            }

            if (readOnly == null)
            {
               portletBuilder.add(prefName, values);
            }
            else
            {
               portletBuilder.add(prefName, values, readOnly);
            }
         }
      }

      TransientApplicationState<Portlet> state = new TransientApplicationState<Portlet>(applicationRef + "/" + portletRef);
      if (portletBuilder != null)
      {
         state.setContentState(portletBuilder.build());
      }

      return state;
   }

   protected void marshalGadgetApplication(StaxWriter<Element> writer, ApplicationData<Gadget> gadgetApplication) throws XMLStreamException
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

   protected ApplicationData<Gadget> unmarshalGadgetApplication(StaxNavigator<Element> navigator) throws XMLStreamException
   {
      String theme = null;
      String title = null;
      String description = null;
      List<String> accessPermissions = null;
      Boolean showInfoBar = null;
      boolean showApplicationState = false;
      boolean showApplicationMode = false;
      String icon = null;
      String width = null;
      String height = null;

      requiresChild(navigator, Element.GADGET);
      ApplicationState<Gadget> state = unmarshalGadgetApplicationState(navigator.fork());

      Element current = navigator.getName();
      while (current != null)
      {
         switch (current)
         {
            case THEME:
               theme = navigator.getContent();
               current = navigator.sibling();
               break;
            case TITLE:
               title = navigator.getContent();
               current = navigator.sibling();
               break;
            case ACCESS_PERMISSIONS:
               accessPermissions = unmarshalAccessPermissions(navigator, true);
               current = navigator.sibling();
               break;
            case SHOW_INFO_BAR:
               showInfoBar = parseRequiredContent(navigator, ValueType.BOOLEAN);
               current = navigator.sibling();
               break;
            case SHOW_APPLICATION_STATE:
               showApplicationState = navigator.parseContent(ValueType.BOOLEAN);
               current = navigator.sibling();
               break;
            case SHOW_APPLICATION_MODE:
               showApplicationMode = navigator.parseContent(ValueType.BOOLEAN);
               current = navigator.sibling();
               break;
            case DESCRIPTION:
               description = navigator.getContent();
               current = navigator.sibling();
               break;
            case ICON:
               icon = navigator.getContent();
               current = navigator.sibling();
               break;
            case WIDTH:
               width = navigator.getContent();
               current = navigator.sibling();
               break;
            case HEIGHT:
               height = navigator.getContent();
               current = navigator.sibling();
               break;
            case UNKNOWN:
               throw unknownElement(navigator);
            default:
               throw unexpectedElement(navigator);
         }
      }

      //TODO: We should raise this exception as soon as we know so location is accurate
      if (accessPermissions == null) throw expectedElement(navigator, Element.ACCESS_PERMISSIONS);
      if (showInfoBar == null) throw expectedElement(navigator, Element.SHOW_INFO_BAR);

      return new ApplicationData<Gadget>(null, null, ApplicationType.GADGET, state, null, title, icon, description,
         showInfoBar, showApplicationState, showApplicationMode, theme, width, height, Collections.<String, String>emptyMap(), accessPermissions);
   }

   private ApplicationState<Gadget> unmarshalGadgetApplicationState(StaxNavigator<Element> navigator) throws XMLStreamException
   {
      requiresChild(navigator, Element.GADGET_REF);
      String gadgetRef = getRequiredContent(navigator, true);

      //TODO: Implement userPref unmarshalling when gatein_objects support it
      Gadget gadget = null;

      if (navigator.next() != null)
      {
         throw unexpectedElement(navigator);
      }

      return new TransientApplicationState<Gadget>(gadgetRef, gadget);
   }

   protected void marshalApplication(StaxWriter<Element> writer, ApplicationData<?> application) throws XMLStreamException
   {
      // Theme, Title
      writeOptionalElement(writer, Element.THEME, application.getTheme());
      writeOptionalElement(writer, Element.TITLE, application.getTitle());

      // Access Permissions
      marshalAccessPermissions(writer, application.getAccessPermissions());

      // common application elements
      writeOptionalElement(writer, Element.SHOW_INFO_BAR, String.valueOf(application.isShowInfoBar()));
      writeOptionalElement(writer, Element.SHOW_APPLICATION_STATE, String.valueOf(application.isShowApplicationState()));
      writeOptionalElement(writer, Element.SHOW_APPLICATION_MODE, String.valueOf(application.isShowApplicationMode()));

      // Description, Icon
      writeOptionalElement(writer, Element.DESCRIPTION, application.getDescription());
      writeOptionalElement(writer, Element.ICON, application.getIcon());

      // Width & Height
      writeOptionalElement(writer, Element.WIDTH, application.getWidth());
      writeOptionalElement(writer, Element.HEIGHT, application.getHeight());
   }

   protected void marshalAccessPermissions(StaxWriter<Element> writer, List<String> accessPermissionsList) throws XMLStreamException
   {
      writeOptionalElement(writer, Element.ACCESS_PERMISSIONS, DelimitedValueType.SEMI_COLON, accessPermissionsList);
   }

   protected List<String> unmarshalAccessPermissions(StaxNavigator<Element> navigator, boolean required) throws XMLStreamException
   {
      if (required)
      {
         return parseRequiredContent(navigator, DelimitedValueType.SEMI_COLON);
      }
      else
      {
         return parseContent(navigator, DelimitedValueType.SEMI_COLON, null);
      }
   }

   protected void marshalEditPermission(StaxWriter<Element> writer, String editPermission) throws XMLStreamException
   {
      writeOptionalElement(writer, Element.EDIT_PERMISSION, editPermission);
   }

   protected String unmarshalEditPermission(StaxNavigator<Element> navigator) throws XMLStreamException
   {
      return getContent(navigator, true);
   }

   protected void writeGateinObjectsNamespace(StaxWriter<Element> writer) throws XMLStreamException
   {
      String gatein_object_ns = Namespace.CURRENT.getUri();
      String location = new StringBuilder().append(gatein_object_ns).append(" ").append(gatein_object_ns).toString();

      writer.writeDefaultNamespace(gatein_object_ns);
      writer.writeNamespace("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
      writer.writeAttribute(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation"), location);
   }

   @SuppressWarnings("unchecked")
   private <S> ApplicationData<S> safeCast(ApplicationData data, Class<S> stateClass)
   {
      return (ApplicationData<S>) data;
   }

   private static void writeOptionalAttribute(StaxWriter writer, Attribute attribute, String value) throws XMLStreamException
   {
      if (value == null) return;

      writer.writeAttribute(attribute.getLocalName(), value);
   }

   private static enum Attribute
   {
      ID("id"),
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
   }
}
