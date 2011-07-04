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

package org.gatein.management.mop.binding;

import org.exoplatform.portal.pom.data.PageData;
import org.exoplatform.portal.pom.data.PortalData;
import org.gatein.management.api.binding.BindingException;
import org.gatein.management.api.binding.BindingProvider;
import org.gatein.management.api.binding.ContentType;
import org.gatein.management.api.binding.Marshaller;
import org.gatein.management.mop.binding.navigation.XmlNavigationMarshaller;
import org.gatein.management.mop.binding.xml.page.PageDataMarshaller;
import org.gatein.management.mop.binding.xml.site.PortalDataMarshaller;
import org.gatein.management.mop.model.PageDataContainer;
import org.gatein.mop.api.workspace.Navigation;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class MopBindingProvider implements BindingProvider
{
   @Override
   public <T> Marshaller<T> getMarshaller(Class<T> type, ContentType contentType) throws BindingException
   {
      switch (contentType)
      {
         case XML:
            return getXmlMarshaller(type);
         case JSON:
         case ZIP:
         default:
            return null;
      }
   }

   private <T> Marshaller<T> getXmlMarshaller(Class<T> type)
   {
      if (PageData.class.isAssignableFrom(type))
      {
         return (Marshaller<T>) XmlMarshallers.page_marshaller;
      }
      else if (PageDataContainer.class.isAssignableFrom(type))
      {
         return (Marshaller<T>) XmlMarshallers.pages_marshaller;
      }
      else if (Navigation.class.isAssignableFrom(type))
      {
         return (Marshaller<T>) XmlMarshallers.navigation_marshaller;
      }
      else if (PortalData.class.isAssignableFrom(type))
      {
         return (Marshaller<T>) XmlMarshallers.site_marshaller;
      }

      return null;
   }

   private static class XmlMarshallers
   {

      //------------------------------------ Page Marshallers ------------------------------------//
      private static Marshaller<PageDataContainer> pages_marshaller = new PageDataMarshaller();

      private static Marshaller<PageData> page_marshaller = new Marshaller<PageData>()
      {
         @Override
         public void marshal(PageData page, OutputStream outputStream) throws BindingException
         {
            PageDataContainer pages = new PageDataContainer(Collections.singletonList(page));

            XmlMarshallers.pages_marshaller.marshal(pages, outputStream);
         }

         @Override
         public PageData unmarshal(InputStream inputStream) throws BindingException
         {
            PageDataContainer pages = pages_marshaller.unmarshal(inputStream);

            if (pages.getPages().isEmpty()) throw new BindingException("No page was unmarshalled.");

            if (pages.getPages().size() != 1) throw new BindingException("Multiple pages found.");

            return pages.getPages().get(0);
         }
      };

      private static Marshaller<Navigation> navigation_marshaller = new XmlNavigationMarshaller();

      private static Marshaller<PortalData> site_marshaller = new PortalDataMarshaller();
   }
}
