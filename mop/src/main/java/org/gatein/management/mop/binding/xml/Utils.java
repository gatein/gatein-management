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

import org.exoplatform.portal.config.model.LocalizedValue;
import org.gatein.common.xml.stax.navigator.StaxNavUtils;
import org.gatein.common.xml.stax.writer.StaxWriter;
import org.staxnav.StaxNavException;
import org.staxnav.StaxNavigator;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
class Utils
{
   private static final Pattern RFC1766_PATTERN = Pattern.compile("^([a-zA-Z]{2})(?:-([a-zA-Z]{2}))?$");
   private static final Pattern JAVA_LOCALE_PATTERN = Pattern.compile("^([a-zA-Z]{2})(?:_([a-zA-Z]{2}))?$");

   public static <N> void writeGateinObjectsNamespace(StaxWriter<N> writer) throws XMLStreamException
   {
      String gatein_object_ns = Namespace.CURRENT.getUri();
      String location = new StringBuilder().append(gatein_object_ns).append(" ").append(gatein_object_ns).toString();

      writer.writeDefaultNamespace(gatein_object_ns);
      writer.writeNamespace("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
      writer.writeAttribute(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation"), location);
   }

   public static <N> LocalizedValue parseLocalizedString(StaxNavigator<N> navigator) throws StaxNavException
   {
      String attribute = navigator.getAttribute(new QName(XMLConstants.XML_NS_URI, "lang", XMLConstants.XML_NS_PREFIX));
      if (attribute == null)
      {
         attribute = navigator.getAttribute("lang");
      }

      Locale lang = null;
      if (attribute != null)
      {
         Matcher matcher = JAVA_LOCALE_PATTERN.matcher(attribute);
         if (!matcher.matches())
         {
            matcher = RFC1766_PATTERN.matcher(attribute);
         }
         if (matcher.matches())
         {
            String langISO = matcher.group(1);
            String countryISO = matcher.group(2);
            if (countryISO == null)
            {
               lang = new Locale(langISO.toLowerCase());
            }
            else
            {
               lang = new Locale(langISO.toLowerCase(), countryISO.toLowerCase());
            }
         }
         else
         {
            throw new StaxNavException(navigator.getLocation(), "The attribute xml:lang='" + attribute + "' does not represent a valid language pattern (ie: en, en-us, en_us).");
         }
      }

      String value = StaxNavUtils.getRequiredContent(navigator, false);

      return new LocalizedValue(value, lang);
   }
}
