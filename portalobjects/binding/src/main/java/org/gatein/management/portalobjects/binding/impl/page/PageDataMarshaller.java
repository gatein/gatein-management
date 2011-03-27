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

package org.gatein.management.portalobjects.binding.impl.page;

import org.exoplatform.portal.pom.data.ComponentData;
import org.exoplatform.portal.pom.data.PageData;
import org.gatein.management.binding.api.BindingException;
import org.gatein.management.binding.api.Bindings;
import org.gatein.management.portalobjects.binding.impl.AbstractPomDataMarshaller;
import org.gatein.management.portalobjects.binding.impl.Element;
import org.staxnav.StaxNavigator;
import org.staxnav.StaxWriter;
import org.staxnav.ValueType;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.gatein.common.xml.stax.navigator.Exceptions.*;
import static org.gatein.common.xml.stax.navigator.StaxNavUtils.*;
import static org.gatein.common.xml.stax.writer.StaxWriterUtils.*;


/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
@Bindings(classes = PageData.class)
public class PageDataMarshaller extends AbstractPomDataMarshaller<PageData>
{
   @Override
   public void marshal(PageData page, OutputStream outputStream) throws BindingException
   {
      marshalObjects(Collections.singleton(page), outputStream);
   }

   @Override
   public void marshalObjects(Collection<PageData> pages, OutputStream outputStream) throws BindingException
   {
      try
      {
         StaxWriter<Element> writer = createWriter(Element.class, outputStream);

         writer.writeStartElement(Element.PAGE_SET);
         writeGateinObjectsNamespace(writer);

         // Marshal pages
         for (PageData page : pages)
         {
            marshalPageData(writer, page);
         }

         writer.finish();
      }
      catch (XMLStreamException e)
      {
         throw new BindingException(e);
      }
   }

   @Override
   public PageData unmarshal(InputStream is) throws BindingException
   {
      Collection<PageData> pages =  unmarshalObjects(is);
      if (pages.size() != 1) throw new BindingException("Multiple pages found for input.");

      return pages.iterator().next();
   }

   @Override
   public Collection<PageData> unmarshalObjects(InputStream inputStream) throws BindingException
   {
      try
      {
         StaxNavigator<Element> navigator = createNavigator(Element.class, Element.UNKNOWN, inputStream);
         if (navigator.getName() == Element.PAGE_SET)
         {
            Collection<PageData> pages = new ArrayList<PageData>();
            for (StaxNavigator<Element> fork : navigator.fork(Element.PAGE))
            {
               pages.add(unmarshalPageData(fork));
            }
            return pages;
         }
         else
         {
            throw unknownElement(navigator);
         }
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

      writeOptionalElement(writer, Element.SHOW_MAX_WINDOW, ValueType.BOOLEAN, pageData.isShowMaxWindow());

      List<ComponentData> components = pageData.getChildren();
      for (ComponentData component : components)
      {
         marshalComponentData(writer, component);
      }

      writer.writeEndElement(); // End of page element
   }

   private PageData unmarshalPageData(StaxNavigator<Element> navigator) throws XMLStreamException
   {
      String name = null;
      String title = null;
      String description = null;
      List<String> accessPermissions = null;
      String editPermission = null;
      boolean showMaxWindow = false;
      List<ComponentData> components = null;

      //TODO: Need valid way to ensure a sequence of xml elements, with a mix of required and optional elements.
      ArrayDeque<Element> required = new ArrayDeque<Element>();
      required.push(Element.ACCESS_PERMISSIONS);
      required.push(Element.NAME);

      navigator.child();
      boolean pop = false;
      while (navigator.hasNext())
      {
         if (pop) required.pop();

         switch (navigator.getName())
         {
            case NAME:
               name = getRequiredContent(navigator, true);
               navigator.sibling();
               break;
            case TITLE:
               title = getContent(navigator, false);
               navigator.sibling();
               break;
            case DESCRIPTION:
               description = getContent(navigator, false);
               navigator.sibling();
               break;
            case ACCESS_PERMISSIONS:
               accessPermissions = unmarshalAccessPermissions(navigator);
               navigator.sibling();
               break;
            case EDIT_PERMISSION:
               editPermission = unmarshalEditPermission(navigator);
               navigator.sibling();
               break;
            case SHOW_MAX_WINDOW:
               showMaxWindow = parseRequiredContent(navigator, ValueType.BOOLEAN);
               navigator.sibling();
               break;
            case CONTAINER:
               if (components != null)
               {
                  throw unexpectedElement(navigator);
               }
               components = new ArrayList<ComponentData>(1);
               components.add(unmarshalContainerData(navigator.fork()));
               break;
            case PORTLET_APPLICATION:
               if (components != null)
               {
                  throw unexpectedElement(navigator);
               }
               components = new ArrayList<ComponentData>(1);
               components.add(unmarshalPortletApplication(navigator.fork()));
               break;
            case GADGET_APPLICATION:
               if (components != null)
               {
                  throw unexpectedElement(navigator);
               }
               components = new ArrayList<ComponentData>(1);
               components.add(unmarshalGadgetApplication(navigator.fork()));
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

      // Ensure children is not null
      if (components == null) components = Collections.emptyList();

      return new PageData(null, null, name, null, null, null, title, description, null, null, accessPermissions, components, "", "", editPermission, showMaxWindow);
   }
}
