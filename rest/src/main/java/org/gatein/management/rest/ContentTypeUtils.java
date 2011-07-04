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

package org.gatein.management.rest;

import com.sun.org.apache.regexp.internal.RE;
import org.gatein.management.api.binding.ContentType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class ContentTypeUtils
{
   private ContentTypeUtils(){}

   private static final ContentType[] CONTENT_TYPES = ContentType.values();
   private static final MediaType[] MEDIA_TYPES;

   static
   {
      MediaType[] mediaTypes = new MediaType[CONTENT_TYPES.length];
      for (int i=0; i<mediaTypes.length; i++)
      {
         mediaTypes[i] = new MediaType("application", CONTENT_TYPES[i].name().toLowerCase());
      }

      MEDIA_TYPES = mediaTypes;
   }

   public static MediaType[] mediaTypes()
   {
      return MEDIA_TYPES;
   }

   public static ContentType getContentType(MediaType mediaType)
   {
      for (ContentType ct : CONTENT_TYPES)
      {
         if (mediaType.getSubtype().equalsIgnoreCase(ct.name()))
         {
            return ct;
         }
      }

      return null;
   }

   public static MediaType getMediaType(ContentType contentType)
   {
      ContentType[] contentTypes = CONTENT_TYPES;
      for (int i=0; i<contentTypes.length; i++)
      {
         if (contentTypes[i] == contentType)
         {
            return MEDIA_TYPES[i];
         }
      }

      return null;
   }

   public static ContentType getContentType(UriInfo uriInfo)
   {
      return getContentType(uriInfo.getPath(), uriInfo.getQueryParameters().getFirst("format"));
   }

   public static ContentType getContentType(String path, String format)
   {
      ContentType[] contentTypes = CONTENT_TYPES;

      int index = path.lastIndexOf(".");
      if (index != -1)
      {
         String extension = path.substring(index+1, path.length());
         for (ContentType ct : contentTypes)
         {
            if (ct.name().equalsIgnoreCase(extension))
            {
               return ct;
            }
         }
      }

      if (format != null)
      {
         for (ContentType ct : contentTypes)
         {
            if (ct.name().equalsIgnoreCase(format))
            {
               return ct;
            }
         }
      }

      return ContentType.JSON;
   }

   public static String getExtension(String path)
   {
      ContentType[] contentTypes = CONTENT_TYPES;

      int index = path.lastIndexOf(".");
      if (index != -1)
      {
         String extension = path.substring(index+1, path.length());
         for (ContentType ct : contentTypes)
         {
            if (ct.name().equalsIgnoreCase(extension))
            {
               return extension;
            }
         }
      }

      return null;
   }
}
