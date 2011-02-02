/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
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

package org.gatein.management.portalobjects.binding.impl.page;

import org.exoplatform.portal.pom.data.ComponentData;
import org.exoplatform.portal.pom.data.PageData;
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
import java.util.List;

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
         StaxWriter writer = new StaxWriterBuilder().withOutputStream(outputStream).withEncoding("UTF-8").withDefaultFormatting().build();
         writer.writeStartDocument();

         writer.writeStartElement(Element.PAGE_SET);
         writeGateinObjectsRootElement(writer);

         // Marshal pages
         for (PageData page : pages)
         {
            marshalPageData(writer, page);
         }

         writer.writeEndElement().writeEndDocument(); // End page-set, end document
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
   public Collection<PageData> unmarshalObjects(InputStream is) throws BindingException
   {
      try
      {
         StaxReader reader = new StaxReaderBuilder().withInputStream(is).build();
         if (reader.readNextTag().getLocalName().equals(Element.PAGE_SET.getLocalName()))
         {
            Collection<PageData> pages = new ArrayList<PageData>();
            while (reader.hasNext())
            {
               switch (reader.read().match().onElement(Element.class, Element.UNKNOWN, Element.SKIP))
               {
                  case PAGE:
                     PageData page = unmarshalPageData(reader);
                     pages.add(page);
                     break;
                  case UNKNOWN:
                     throw new XMLStreamException("Uknown element '" + reader.currentReadEvent().getLocalName() + "' while unmarshalling pages.");
                  case SKIP:
                     break;
               }
            }
            return pages;
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

   private void marshalPageData(StaxWriter writer, PageData pageData) throws XMLStreamException
   {
      writer.writeStartElement(Element.PAGE);

      // name, title description
      writer.writeElement(Element.NAME, pageData.getName());
      writer.writeOptionalElement(Element.TITLE, pageData.getTitle());
      writer.writeOptionalElement(Element.DESCRIPTION, pageData.getDescription());

      // Access/Edit permissions
      marshalAccessPermissions(writer, pageData.getAccessPermissions());
      marshalEditPermission(writer, pageData.getEditPermission());

      writer.writeOptionalElement(Element.SHOW_MAX_WINDOW, String.valueOf(pageData.isShowMaxWindow()));

      List<ComponentData> components = pageData.getChildren();
      for (ComponentData component : components)
      {
         marshalComponentData(writer, component);
      }

      writer.writeEndElement(); // End of page element
   }

   private PageData unmarshalPageData(StaxReader reader) throws XMLStreamException
   {
      String name = null;
      String title = null;
      String description = null;
      List<String> accessPermissions = null;
      String editPermission = null;
      boolean showMaxWindow = false;
      List<ComponentData> components = null;

      reader.buildReadEvent().withNestedRead().untilElement(Element.PAGE).end();
      while (reader.hasNext())
      {
         switch(reader.read().match().onElement(Element.class, Element.UNKNOWN, Element.SKIP))
         {
            case NAME:
               name = reader.currentReadEvent().elementText();
               break;
            case TITLE:
               title = reader.currentReadEvent().elementText();
               break;
            case DESCRIPTION:
               description = reader.currentReadEvent().elementText();
               break;
            case SHOW_MAX_WINDOW:
               showMaxWindow = Boolean.valueOf(reader.currentReadEvent().elementText());
               break;
            case SKIP:
               break;
            case UNKNOWN:
               // Unmarshal access permissions
               if (isAccessPermissions(reader))
               {
                  accessPermissions = unmarshalAccessPermissions(reader);
                  break;
               }
               // Unmarshal edit permissions
               else if (isEditPermission(reader))
               {
                  editPermission = unmarshalEditPermission(reader);
                  break;
               }
               // Unmarshal container
               else if (isContainer(reader))
               {
                  components = new ArrayList<ComponentData>(1);
                  components.add(unmarshalContainerData(reader));
               }
               // Unmarshal portlet application
               else if (isPortletApplication(reader))
               {
                  if (components == null)
                  {
                     components = new ArrayList<ComponentData>();
                  }
                  components.add(unmarshalPortletApplication(reader));
               }
               else if (isGadgetApplication(reader))
               {
                  if (components == null)
                  {
                     components = new ArrayList<ComponentData>();
                  }
                  components.add(unmarshalGadgetApplication(reader));
               }
               else
               {
                  throw new XMLStreamException("Unknown element '" + reader.currentReadEvent().getLocalName() +
                     "' while unmarshalling page.", reader.currentReadEvent().getLocation());
               }
            default:
               break;
         }
      }
      if (components == null) components = Collections.emptyList();
      if (accessPermissions == null) accessPermissions = Collections.emptyList();
      return new PageData(null, null, name, null, null, null, title, description, null, null, accessPermissions, components, "", "", editPermission, showMaxWindow);
   }
}
