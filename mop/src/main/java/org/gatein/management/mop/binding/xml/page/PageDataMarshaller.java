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

package org.gatein.management.mop.binding.xml.page;

import org.exoplatform.portal.pom.data.ComponentData;
import org.exoplatform.portal.pom.data.PageData;
import org.gatein.common.xml.stax.writer.StaxWriter;
import org.gatein.common.xml.stax.writer.WritableValueTypes;
import org.gatein.management.api.binding.BindingException;
import org.gatein.management.mop.binding.xml.AbstractPomDataMarshaller;
import org.gatein.management.mop.binding.xml.Element;
import org.gatein.management.mop.model.PageDataContainer;
import org.staxnav.StaxNavException;
import org.staxnav.StaxNavigator;
import org.staxnav.ValueType;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.gatein.common.xml.stax.navigator.Exceptions.*;
import static org.gatein.common.xml.stax.navigator.StaxNavUtils.*;
import static org.gatein.common.xml.stax.writer.StaxWriterUtils.*;


/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PageDataMarshaller extends AbstractPomDataMarshaller<PageDataContainer>
{
   @Override
   public void marshal(PageDataContainer pageContainer, OutputStream outputStream) throws BindingException
   {
      try
      {
         StaxWriter<Element> writer = createWriter(Element.class, outputStream);

         writer.writeStartElement(Element.PAGE_SET);
         writeGateinObjectsNamespace(writer);

         // Marshal pages
         for (PageData page : pageContainer.getPages())
         {
            marshalPageData(writer, page);
         }

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
   public PageDataContainer unmarshal(InputStream inputStream) throws BindingException
   {
      try
      {
         StaxNavigator<Element> navigator = createNavigator(Element.class, Element.UNKNOWN, inputStream);
         if (navigator.getName() == Element.PAGE_SET)
         {
            List<PageData> pages = new ArrayList<PageData>();
            Element next = navigator.child();
            if (next == Element.PAGE)
            {
               for (StaxNavigator<Element> fork : navigator.fork(Element.PAGE))
               {
                  pages.add(unmarshalPageData(fork));
               }
            }
            else if (next != null)
            {
               throw unexpectedElement(navigator);
            }

            //TODO: Seems like next should be null here...
            if (navigator.sibling() != null)
            {
               throw unexpectedElement(navigator);
            }

            return new PageDataContainer(pages);
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

   private void marshalPageData(StaxWriter<Element> writer, PageData pageData) throws XMLStreamException
   {
      writer.writeStartElement(Element.PAGE);

      // name, title description
      writer.writeElement(Element.NAME, pageData.getName());
      writeOptionalElement(writer, Element.TITLE, pageData.getTitle());
      writeOptionalElement(writer, Element.DESCRIPTION, pageData.getDescription());

      // Access/Edit permissions
      marshalAccessPermissions(writer, pageData.getAccessPermissions());
      marshalEditPermission(writer, pageData.getEditPermission());

      writeOptionalElement(writer, Element.SHOW_MAX_WINDOW, WritableValueTypes.BOOLEAN, pageData.isShowMaxWindow());

      List<ComponentData> components = pageData.getChildren();
      for (ComponentData component : components)
      {
         marshalComponentData(writer, component);
      }

      writer.writeEndElement(); // End of page element
   }

   private PageData unmarshalPageData(StaxNavigator<Element> navigator) throws XMLStreamException
   {
      requiresChild(navigator, Element.NAME);
      String name = getRequiredContent(navigator, true);

      String title = null;
      String description = null;
      List<String> accessPermissions = null;
      String editPermission = null;
      boolean showMaxWindow = false;
      List<ComponentData> components = new ArrayList<ComponentData>();

      //TODO: Need valid way to ensure a sequence of xml elements, with a mix of required and optional elements.
      Element current = navigator.sibling();
      while (current != null)
      {
         switch (current)
         {
            case TITLE:
               title = getContent(navigator, false);
               current = navigator.sibling();
               break;
            case DESCRIPTION:
               description = getContent(navigator, false);
               current = navigator.sibling();
               break;
            case ACCESS_PERMISSIONS:
               accessPermissions = unmarshalAccessPermissions(navigator, true);
               current = navigator.sibling();
               break;
            case EDIT_PERMISSION:
               editPermission = unmarshalEditPermission(navigator);
               current = navigator.sibling();
               break;
            case SHOW_MAX_WINDOW:
               showMaxWindow = parseRequiredContent(navigator, ValueType.BOOLEAN);
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
      //TODO: We should raise this exception as soon as we know so location is accurate
      if (name == null) throw expectedElement(navigator, Element.NAME);
      if (accessPermissions == null) throw expectedElement(navigator, Element.ACCESS_PERMISSIONS);

      return new PageData(null, null, name, null, null, null, title, description, null, null, accessPermissions, components, "", "", editPermission, showMaxWindow);
   }
}
