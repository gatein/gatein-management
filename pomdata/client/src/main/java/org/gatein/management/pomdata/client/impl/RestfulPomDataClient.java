/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual
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

package org.gatein.management.pomdata.client.impl;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.exoplatform.portal.pom.data.NavigationData;
import org.exoplatform.portal.pom.data.PageData;
import org.exoplatform.portal.pom.data.PortalData;
import org.gatein.management.binding.api.BindingProvider;
import org.gatein.management.binding.api.Marshaller;
import org.gatein.management.binding.rest.BindingRestProvider;
import org.gatein.management.pomdata.client.api.ClientException;
import org.gatein.management.pomdata.client.api.PomDataClient;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClientExecutor;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class RestfulPomDataClient implements PomDataClient
{
   private static final String BASE_URI = "/management/rest/pomdata";

   private final BindingProvider bindingProvider;

   // Restful client stubs
   private final SiteClientStub siteClientStub;
   private final PageClientStub pageClientStub;
   private final NavigationClientStub navigationClientStub;

   public RestfulPomDataClient(InetAddress address, int port, String containerName, BindingProvider bindingProvider)
   {
      this.bindingProvider = bindingProvider;

      //TODO: Configure authentication
      Credentials credentials = new UsernamePasswordCredentials("root", "gtn");
      HttpClient httpClient = new HttpClient();
      httpClient.getState().setCredentials(AuthScope.ANY, credentials);
      httpClient.getParams().setAuthenticationPreemptive(true);

      try
      {
         //TODO: What about https
         StringBuilder uri = new StringBuilder().append("http://").append(address.getHostName());
         if (port != 80)
         {
            uri.append(':').append(port);
         }
         uri.append(BASE_URI).append("/").append(containerName);
         ClientRequestFactory clientRequestFactory = new ClientRequestFactory(
            new ApacheHttpClientExecutor(httpClient), new URI(uri.toString()));

         siteClientStub = clientRequestFactory.createProxy(SiteClientStub.class);
         pageClientStub = clientRequestFactory.createProxy(PageClientStub.class);
         navigationClientStub = clientRequestFactory.createProxy(NavigationClientStub.class);
      }
      catch (URISyntaxException e)
      {
         throw new RuntimeException("Could not create restful mop client.", e);
      }

      ResteasyProviderFactory instance = ResteasyProviderFactory.getInstance();
      instance.registerProviderInstance(new BindingRestProvider(bindingProvider));
      RegisterBuiltin.register(instance);
   }

   @Override
   public List<PortalData> getPortalConfig(String ownerType) throws ClientException
   {
      return handleResponse(siteClientStub.getPortalData(ownerType));
   }

   @Override
   public PortalData getPortalConfig(String ownerType, String ownerId) throws ClientException
   {
      if (ownerId.charAt(0) == '/') ownerId = ownerId.substring(1);
      return handleResponse(siteClientStub.getPortalData(ownerType, ownerId));
   }

   @Override
   public List<PageData> getPages(String ownerType, String ownerId) throws ClientException
   {
      return handleResponse(pageClientStub.getPages(ownerType, ownerId));
   }

   @Override
   public PageData getPage(String ownerType, String ownerId, String pageName) throws ClientException
   {
      return handleResponse(pageClientStub.getPage(ownerType, ownerId, pageName));
   }

   @Override
   public NavigationData getNavigation(String ownerType, String ownerId) throws ClientException
   {
      return handleResponse(navigationClientStub.getNavigation(ownerType, ownerId));
   }

   @Override
   public NavigationData getNavigation(String ownerType, String ownerId, String navigationPath) throws ClientException
   {
      return handleResponse(navigationClientStub.getNavigation(ownerType, ownerId, navigationPath));
   }

   @Override
   public void exportToFile(PortalData data, File file) throws IOException
   {
      FileOutputStream fos = new FileOutputStream(file);

      try
      {
         Marshaller<PortalData> marshaller = bindingProvider.createContext(PortalData.class).createMarshaller();
         marshaller.marshal(data, fos);
         fos.flush();
      }
      finally
      {
         close(fos);
      }
   }

   @Override
   public void exportToFile(PageData data, File file) throws IOException
   {
      exportToFile(Collections.singletonList(data), file);
   }

   @Override
   public void exportToFile(List<PageData> data, File file) throws IOException
   {
      FileOutputStream fos = new FileOutputStream(file);

      try
      {
         Marshaller<PageData> marshaller = bindingProvider.createContext(PageData.class).createMarshaller();
         marshaller.marshalObjects(data, fos);
         fos.flush();
      }
      finally
      {
         close(fos);
      }
   }

   @Override
   public void exportToFile(NavigationData data, File file) throws IOException
   {
      FileOutputStream fos = new FileOutputStream(file);

      try
      {
         Marshaller<NavigationData> marshaller = bindingProvider.createContext(NavigationData.class).createMarshaller();
         marshaller.marshal(data, fos);
         fos.flush();
      }
      finally
      {
         close(fos);
      }
   }

   private <T> T handleResponse(ClientResponse<T> response)
   {
      if (response.getResponseStatus() == Response.Status.OK)
      {
         return response.getEntity();
      }
      else if (response.getResponseStatus() == Response.Status.NOT_FOUND)
      {
         return null;
      }
      else if (response.getStatus() >= 400)
      {
         String message = response.getEntity(String.class);
         Exception e = new Exception(message);
         throw new ClientException("HTTP Error status " + response.getStatus() + " (" + response.getResponseStatus().toString() + ") received.", e);
      }
      else
      {
         throw new ClientException("Unknown http status code returned from server: " + response.getStatus());
      }
   }

   private void close(Closeable closeable)
   {
      if (closeable != null)
      {
         try
         {
            closeable.close();
         }
         catch (IOException ioex)
         {
         }
      }
   }

   @Path("/sites")
   private static interface SiteClientStub
   {
      @GET
      @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
      ClientResponse<List<PortalData>> getPortalData(@QueryParam("ownerType") String ownerType);

      @GET
      @Path("/{ownerId:.*}")
      @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
      ClientResponse<PortalData> getPortalData(@QueryParam("ownerType") String ownerType,
                                               @Encoded @PathParam("ownerId") String ownerId);
   }

   @Path("/pages")
   private static interface PageClientStub
   {
      @GET
      @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
      ClientResponse<List<PageData>> getPages(@QueryParam("ownerType") String ownerType,
                                              @QueryParam("ownerId") String ownerId);

      @GET
      @Path("/{page-name}")
      @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
      ClientResponse<PageData> getPage(@QueryParam("ownerType") String ownerType,
                                       @QueryParam("ownerId") String ownerId,
                                       @PathParam("page-name") String pageName);
   }

   @Path("/navigation")
   private static interface NavigationClientStub
   {
      @GET
      @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
      ClientResponse<NavigationData> getNavigation(@QueryParam("ownerType") String ownerType,
                                                   @QueryParam("ownerId") String ownerId);

      @GET
      @Path("/{nav-path:.*}")
      @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
      ClientResponse<NavigationData> getNavigation(@QueryParam("ownerType") String ownerType,
                                                   @QueryParam("ownerId") String ownerId,
                                                   @Encoded @PathParam("nav-path") String navigationPath);
   }
}
