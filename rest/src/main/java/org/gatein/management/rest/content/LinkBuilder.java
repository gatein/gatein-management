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

package org.gatein.management.rest.content;

import javax.ws.rs.core.UriBuilder;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class LinkBuilder
{
   private UriBuilder uriBuilder;
   private String rel;
   private String type;
   private String method;

   public static LinkBuilder fromLink(Link link)
   {
      return new LinkBuilder(UriBuilder.fromUri(link.getHref()))
         .rel(link.getRel())
         .type(link.getType())
         .method(link.getMethod());
   }

   public LinkBuilder(UriBuilder uriBuilder)
   {
      String uri = uriBuilder.build().toString();
      if (uri.endsWith("/"))
      {
         uri = uri.substring(0, uri.length()-1);
      }
      this.uriBuilder = UriBuilder.fromUri(uri);
   }

   public LinkBuilder rel(String rel)
   {
      this.rel = rel;
      return this;
   }

   public LinkBuilder type(String type)
   {
      this.type = type;
      return this;
   }

   public LinkBuilder method(String method)
   {
      this.method = method;
      return this;
   }

   public LinkBuilder extension(String extension) throws IllegalArgumentException
   {
      String uri = uriBuilder.build().toString();
      int queryStart = uri.indexOf("?");
      if (queryStart == -1)
      {
         uri = uri + "." + extension;
      }
      else
      {
         String beforeQuery = uri.substring(0, queryStart);
         String afterQuery = uri.substring(queryStart, uri.length());
         uri = beforeQuery + "." + extension + afterQuery;
      }

      uriBuilder = UriBuilder.fromUri(uri);

      return this;
   }

   public LinkBuilder path(String path) throws IllegalArgumentException
   {
      uriBuilder.path(path);
      return this;
   }

   public LinkBuilder replaceQuery(String query) throws IllegalArgumentException
   {
      uriBuilder.replaceQuery(query);
      return this;
   }

   public LinkBuilder queryParam(String name, Object... values) throws IllegalArgumentException
   {
      uriBuilder.queryParam(name, values);
      return this;
   }

   public LinkBuilder replaceQueryParam(String name, Object... values) throws IllegalArgumentException
   {
      uriBuilder.replaceQueryParam(name, values);
      return this;
   }

   public LinkBuilder copy()
   {
      return fromLink(this.build());
   }

   public Link build()
   {
      return new Link(rel, uriBuilder.build().toString(), type, method);
   }
}
