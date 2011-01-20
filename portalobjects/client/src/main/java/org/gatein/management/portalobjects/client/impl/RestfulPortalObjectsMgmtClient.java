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

package org.gatein.management.portalobjects.client.impl;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.pom.data.NavigationData;
import org.exoplatform.portal.pom.data.NavigationNodeData;
import org.exoplatform.portal.pom.data.PageData;
import org.exoplatform.portal.pom.data.PortalData;
import org.gatein.management.binding.api.BindingProvider;
import org.gatein.management.binding.rest.BindingRestProvider;
import org.gatein.management.portalobjects.api.exportimport.ExportContext;
import org.gatein.management.portalobjects.api.exportimport.ExportHandler;
import org.gatein.management.portalobjects.api.exportimport.ImportContext;
import org.gatein.management.portalobjects.api.exportimport.ImportHandler;
import org.gatein.management.portalobjects.client.api.ClientException;
import org.gatein.management.portalobjects.client.api.PortalObjectsMgmtClient;
import org.gatein.management.portalobjects.common.exportimport.PortalObjectsContext;
import org.gatein.management.portalobjects.common.exportimport.ExportImportUtils;
import org.gatein.management.portalobjects.common.utils.PortalObjectsUtils;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClientExecutor;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class RestfulPortalObjectsMgmtClient implements PortalObjectsMgmtClient
{
   private static final String BASE_URI = "/management/rest/portalobjects";

   private final BindingProvider bindingProvider;

   // Restful client stubs
   private final SiteClientStub siteClientStub;
   private final PageClientStub pageClientStub;
   private final NavigationClientStub navigationClientStub;

   // Export/Import handlers
   private ExportHandler exportHandler;
   private ImportHandler importHandler;

   public RestfulPortalObjectsMgmtClient(InetAddress address, int port, String containerName, BindingProvider bindingProvider)
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
   public List<PortalConfig> getPortalConfig(String ownerType) throws ClientException
   {
      List<PortalData> data = handleResponse(ownerType, null, siteClientStub.getPortalData(ownerType));

      return PortalObjectsUtils.toPortalConfigList(data);
   }

   @Override
   public PortalConfig getPortalConfig(String ownerType, String ownerId) throws ClientException
   {
      if (ownerId.charAt(0) == '/') ownerId = ownerId.substring(1);
      PortalData data = handleResponse(ownerType, ownerId, siteClientStub.getPortalData(ownerType, ownerId));

      return PortalObjectsUtils.toPortalConfig(data);
   }

   @Override
   public void updatePortalConfig(PortalConfig portalConfig) throws ClientException
   {
      String ownerId = portalConfig.getName();
      if (ownerId.charAt(0) == '/') ownerId = ownerId.substring(1);

      PortalData data = PortalObjectsUtils.toPortalData(portalConfig);
      handleNoEntityResponse(siteClientStub.updatePortalData(portalConfig.getType(), ownerId, data));
   }

   @Override
   public List<Page> getPages(String ownerType, String ownerId) throws ClientException
   {
      List<PageData> data = handleResponse(ownerType, ownerId, pageClientStub.getPages(ownerType, ownerId));
      return PortalObjectsUtils.toPageList(data);
   }

   @Override
   public Page getPage(String ownerType, String ownerId, String pageName) throws ClientException
   {
      PageData data = handleResponse(ownerType, ownerId, pageClientStub.getPage(ownerType, ownerId, pageName));
      return PortalObjectsUtils.toPage(data);
   }

   @Override
   public Page createPage(String ownerType, String ownerId, String pageName, String title) throws ClientException
   {
      PageData data = handleResponse(ownerType, ownerId, pageClientStub.createPage(ownerType, ownerId, pageName, title));
      return PortalObjectsUtils.toPage(data);
   }

   @Override
   public void updatePage(Page page) throws ClientException
   {
      PageData data = PortalObjectsUtils.toPageData(page);
      handleNoEntityResponse(pageClientStub.updatePage(data.getOwnerType(), data.getOwnerId(), data.getName(), data));
   }

   @Override
   public void deletePage(String ownerType, String ownerId, String pageName) throws ClientException
   {
      handleNoEntityResponse(pageClientStub.deletePage(ownerType, ownerId, pageName));
   }

   @Override
   public PageNavigation getNavigation(String ownerType, String ownerId) throws ClientException
   {
      NavigationData data = handleResponse(ownerType, ownerId, navigationClientStub.getNavigation(ownerType, ownerId));
      return PortalObjectsUtils.toPageNavigation(data);
   }

   @Override
   public PageNode getNavigationNode(String ownerType, String ownerId, String navigationUri) throws ClientException
   {
      NavigationData data = handleResponse(ownerType, ownerId,
         navigationClientStub.getNavigation(ownerType, ownerId, navigationUri));

      return toPageNode(data);
   }

   @Override
   public PageNavigation createNavigation(String ownerType, String ownerId, int priority) throws ClientException
   {
      NavigationData data = handleResponse(ownerType, ownerId,
         navigationClientStub.createNavigation(ownerType, ownerId, priority, null, null));

      return PortalObjectsUtils.toPageNavigation(data);
   }

   @Override
   public PageNode createNavigationNode(String ownerType, String ownerId, String name, String label) throws ClientException
   {
      NavigationData data = handleResponse(ownerType, ownerId,
         navigationClientStub.createNavigation(ownerType, ownerId, (String) null, name, label));

      return toPageNode(data);
   }

   @Override
   public PageNode createNavigationNode(String ownerType, String ownerId, String parentNavigationUri, String name, String label) throws ClientException
   {
      NavigationData data = handleResponse(ownerType, ownerId,
         navigationClientStub.createNavigation(ownerType, ownerId, parentNavigationUri, name, label));

      return toPageNode(data);
   }

   @Override
   public void updateNavigation(PageNavigation navigation) throws ClientException
   {
      NavigationData data = PortalObjectsUtils.toNavigationData(navigation);
      handleNoEntityResponse(navigationClientStub.updateNavigation(navigation.getOwnerType(), navigation.getOwnerId(), data));
   }

   @Override
   public void updateNavigationNode(String ownerType, String ownerId, PageNode node) throws ClientException
   {
      NavigationData data = toNavigationData(ownerType, ownerId, node);
      handleNoEntityResponse(navigationClientStub.updateNavigation(ownerType, ownerId, node.getUri(), data));
   }

   @Override
   public void deleteNavigationNode(String ownerType, String ownerId, String navigationUri) throws ClientException
   {
      handleNoEntityResponse(navigationClientStub.deleteNavigation(ownerType, ownerId, navigationUri));
   }

   @Override
   public ExportHandler getExportHandler()
   {
      return exportHandler;
   }

   public void setExportHandler(ExportHandler exportHandler)
   {
      this.exportHandler = exportHandler;
   }

   @Override
   public ImportHandler getImportHandler()
   {
      return importHandler;
   }

   public void setImportHandler(ImportHandler importHandler)
   {
      this.importHandler = importHandler;
   }

   private void handleNoEntityResponse(ClientResponse<?> response)
   {
      if (response.getResponseStatus() == Response.Status.OK)
      {
      }
      else if (response.getResponseStatus().getFamily() == Response.Status.Family.SERVER_ERROR)
      {
         String message = response.getEntity(String.class);
         Exception e = new Exception(message);
         throw new ClientException("Internal server error " + response.getStatus() + " (" + response.getResponseStatus().toString() + ") received.", e);
      }
      else
      {
         throw new ClientException("Unknown http status code returned from server: " + response.getStatus());
      }
   }

   private <T> T handleResponse(String ownerType, String ownerId, ClientResponse<T> response)
   {
      if (response.getResponseStatus() == Response.Status.OK)
      {
         T t = response.getEntity();
         return PortalObjectsUtils.fixOwner(ownerType, ownerId, t);
      }
      else if (response.getResponseStatus() == Response.Status.NOT_FOUND)
      {
         return null;
      }
      else if (response.getResponseStatus().getFamily() == Response.Status.Family.SERVER_ERROR)
      {
         String message = response.getEntity(String.class);
         Exception e = new Exception(message);
         throw new ClientException("Internal server error " + response.getStatus() + " (" + response.getResponseStatus().toString() + ") received.", e);
      }
      else
      {
         throw new ClientException("Unhandled http status code returned from server: " + response.getStatus());
      }
   }

   private PageNode toPageNode(NavigationData data)
   {
      if (data == null) return null;

      PageNavigation navigation = PortalObjectsUtils.toPageNavigation(data);
      int size = navigation.getNodes().size();
      if (size == 0) throw new ClientException("No data returned from response.");
      if (size > 1) throw new ClientException("Multiple navigation nodes returned from response. This is likely an error on the server.");

      return navigation.getNodes().get(0);
   }

   private NavigationData toNavigationData(String ownerType, String ownerId, PageNode node)
   {
      if (node == null) return null;

      NavigationNodeData nodeData = PortalObjectsUtils.toNavigationNodeData(node);
      List<NavigationNodeData> nodeDataList = new ArrayList<NavigationNodeData>(1);
      nodeDataList.add(nodeData);

      return new NavigationData(ownerType, ownerId, null, nodeDataList);
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

      @POST
      @Path("/{owner-id:.*}")
      @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
      ClientResponse updatePortalData(@QueryParam("ownerType") String ownerType,
                                      @PathParam("ownerId") @Encoded String ownerId,
                                      PortalData data);
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

      @POST
      @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
      ClientResponse<PageData> createPage(@QueryParam("ownerType") String ownerType,
                                          @QueryParam("ownerId") String ownerId,
                                          @QueryParam("name") String pageName,
                                          @QueryParam("title") String title);

      @PUT
      @Path("/{page-name}")
      @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
      ClientResponse updatePage(@QueryParam("ownerType") String ownerType, @QueryParam("ownerId") String ownerId,
                                @PathParam("page-name") String pageName, PageData page);

      @DELETE
      @Path("/{page-name}")
      @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
      ClientResponse deletePage(@QueryParam("ownerType") String ownerType,
                                @QueryParam("ownerId") String ownerId,
                                @PathParam("page-name") String pageName);
   }

   @Path("/navigations")
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

      @POST
      @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
      ClientResponse<NavigationData> createNavigation(@QueryParam("ownerType") String ownerType,
                                                      @QueryParam("ownerId") String ownerId,
                                                      @QueryParam("priority") Integer priority,
                                                      @QueryParam("name") String name,
                                                      @QueryParam("label") String label);

      @PUT
      @Path("/{parent-nav-uri:.*}")
      @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
      ClientResponse<NavigationData> createNavigation(@QueryParam("ownerType") String ownerType,
                                                      @QueryParam("ownerId") String ownerId,
                                                      @PathParam("parent-nav-uri") String parentNavigationUri,
                                                      @QueryParam("name") String name,
                                                      @QueryParam("label") String label);

      @POST
      @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
      public ClientResponse updateNavigation(@QueryParam("ownerType") String ownerType,
                                             @QueryParam("ownerId") String ownerId,
                                             NavigationData data);

      @POST
      @Path("/{nav-uri:.*}")
      @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
      public ClientResponse updateNavigation(@QueryParam("ownerType") String ownerType,
                                             @QueryParam("ownerId") String ownerId,
                                             @PathParam("nav-uri") String navigationPath,
                                             NavigationData data);

      @DELETE
      @Path("/{nav-uri:.*}")
      @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XHTML_XML})
      public ClientResponse deleteNavigation(@QueryParam("ownerType") String ownerType,
                                             @QueryParam("ownerId") String ownerId,
                                             @PathParam("nav-uri") String navigationUri);
   }
}
