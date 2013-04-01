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

import org.gatein.management.api.ContentType;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.controller.ManagedRequest;
import org.gatein.management.api.operation.OperationNames;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import java.io.InputStream;
import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
class HttpManagedRequestBuilder
{
   private HttpServletRequest servletRequest;
   private String path;
   private String operationName;
   private ContentType contentType;
   private InputStream inputStream;
   private MultivaluedMap<String, String> parameters;
   private Locale locale;
   private String httpMethod;

   public static HttpManagedRequestBuilder get()
   {
      return new HttpManagedRequestBuilder(OperationNames.READ_RESOURCE, HttpMethod.GET);
   }

   public static HttpManagedRequestBuilder post(InputStream inputStream)
   {
      return new HttpManagedRequestBuilder(OperationNames.ADD_RESOURCE, inputStream, HttpMethod.POST);
   }

   public static HttpManagedRequestBuilder put(InputStream inputStream)
   {
      return new HttpManagedRequestBuilder(OperationNames.UPDATE_RESOURCE, inputStream, HttpMethod.PUT);
   }

   public static HttpManagedRequestBuilder delete()
   {
      return new HttpManagedRequestBuilder(OperationNames.REMOVE_RESOURCE, HttpMethod.DELETE);
   }

   private HttpManagedRequestBuilder(String operationName, String httpMethod)
   {
      this(operationName, null, httpMethod);
   }

   private HttpManagedRequestBuilder(String operationName, InputStream inputStream, String httpMethod)
   {
      this.operationName = operationName;
      this.inputStream = inputStream;
      this.httpMethod = httpMethod;
   }

   public String getHttpMethod()
   {
      return httpMethod;
   }

   public HttpManagedRequestBuilder path(String path)
   {
      if (path == null) path = "";
      this.path = path;

      return this;
   }

   public HttpManagedRequestBuilder parameters(MultivaluedMap<String, String> parameters)
   {
      this.parameters = parameters;
      return this;
   }

   public HttpManagedRequestBuilder contentType(ContentType contentType)
   {
      this.contentType = contentType;
      return this;
   }

   public HttpManagedRequestBuilder operationName(String operationName)
   {
      this.operationName = operationName;
      return this;
   }

   public HttpManagedRequestBuilder servlet(HttpServletRequest request)
   {
      this.servletRequest = request;
      return this;
   }

   public HttpManagedRequestBuilder locale(HttpHeaders headers)
   {
      if (headers != null)
      {
         List<Locale> locales = headers.getAcceptableLanguages();
         if (locales != null && !locales.isEmpty())
         {
            this.locale = locales.get(0); // just get the first one
         }
      }

      return this;
   }

   @SuppressWarnings("deprecation")
   public HttpManagedRequest build()
   {
      String op = parameters.getFirst("op");
      if (op != null)
      {
         operationName = op;
         if (operationName.equals(OperationNames.READ_CONFIG) || operationName.equals(OperationNames.READ_CONFIG_AS_XML))
         {
            contentType = ContentType.XML;
         }
         else if (operationName.equals(OperationNames.EXPORT_RESOURCE))
         {
            contentType = ContentType.ZIP;
         }
      }

      if (contentType == null)
      {
         String format = parameters.getFirst("format");
         contentType = ContentType.forName(format);
      }

      if (path.endsWith(".xml"))
      {
         path = path.substring(0, path.lastIndexOf(".xml"));
         operationName = OperationNames.READ_CONFIG;
         contentType = ContentType.XML;
      }
      else if (path.endsWith(".zip"))
      {
         path = path.substring(0, path.lastIndexOf(".zip"));
         operationName = OperationNames.EXPORT_RESOURCE;
         contentType = ContentType.ZIP;
      }

      PathAddress address = PathAddress.pathAddress(path);
      if (contentType == null)
      {
         contentType = ContentType.JSON; // default to JSON
      }

      ManagedRequest request = ManagedRequest.Factory.create(operationName, address, parameters, inputStream, contentType);
      request.setLocale(locale);
      return new HttpManagedRequestDelegate(request, servletRequest, httpMethod);
   }

   private static class HttpManagedRequestDelegate implements HttpManagedRequest
   {
      private final ManagedRequest delegate;
      private final String httpMethod;
      private final HttpServletRequest servletRequest;

      private HttpManagedRequestDelegate(ManagedRequest delegate, HttpServletRequest servletRequest, String httpMethod)
      {
         this.delegate = delegate;
         this.httpMethod = httpMethod;
         this.servletRequest = servletRequest;
      }

      @Override
      public Object getRequest()
      {
         return servletRequest;
      }

      @Override
      public RoleResolver getRoleResolver()
      {
         return new RoleResolver()
         {
            @Override
            public boolean isUserInRole(String role)
            {
               return servletRequest.isUserInRole(role);
            }
         };
      }

      @Override
      public String  getRemoteUser()
      {
         return servletRequest.getRemoteUser();
      }

      @Override
      public String getOperationName()
      {
         return delegate.getOperationName();
      }

      @Override
      public PathAddress getAddress()
      {
         return delegate.getAddress();
      }

      @Override
      public Map<String, List<String>> getAttributes()
      {
         return delegate.getAttributes();
      }

      @Override
      public Locale getLocale()
      {
         return delegate.getLocale();
      }

      @Override
      public void setLocale(Locale locale)
      {
         delegate.setLocale(locale);
      }

      @Override
      public InputStream getDataStream()
      {
         return delegate.getDataStream();
      }

      @Override
      public ContentType getContentType()
      {
         return delegate.getContentType();
      }

      @Override
      public String getHttpMethod()
      {
         return httpMethod;
      }
   }
}
