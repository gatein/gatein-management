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

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.api.ContentType;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.controller.ManagedResponse;
import org.gatein.management.api.controller.ManagementController;
import org.gatein.management.api.exceptions.NotAuthorizedException;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.operation.OperationNames;
import org.gatein.management.api.operation.model.NoResultModel;
import org.gatein.management.api.operation.model.ReadResourceModel;
import org.gatein.management.rest.content.Resource;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;
import java.io.InputStream;

import static javax.ws.rs.core.Response.*;
import static org.gatein.management.rest.HttpManagedRequestBuilder.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
@Path(RestApplication.API_ENTRY_POINT)
public class RestController
{
   private static final Logger log = LoggerFactory.getLogger(RestController.class);

   private ManagementController controller;

   public RestController(ManagementController controller)
   {
      this.controller = controller;
   }

   //------------------------------------- Html (browser) Handlers -------------------------------------//
   // Note we add text/html here so we can handle browsers, even though we don't produce text/html
   @GET
   @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
   public Response htmlGetRequest(@Context UriInfo uriInfo, @Context HttpServletRequest servletRequest, @Context HttpHeaders httpHeaders)
   {
      return htmlGetRequest(uriInfo, servletRequest, httpHeaders, "");
   }

   @GET
   @Path("/{path:.*}")
   @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
   public Response htmlGetRequest(@Context UriInfo uriInfo, @Context HttpServletRequest servletRequest, @Context HttpHeaders httpHeaders, @PathParam("path") String path)
   {
      HttpManagedRequest request = get()
         .path(path)
         .servlet(servletRequest)
         .locale(httpHeaders)
         .parameters(uriInfo.getQueryParameters())
         .build();

      return executeRequest(uriInfo, request);
   }

   @POST
   @Consumes(MediaType.TEXT_HTML)
   @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
   public Response htmlPostRequest(@Context UriInfo uriInfo, @Context HttpServletRequest servletRequest, @Context HttpHeaders httpHeaders, InputStream data)
   {
      return htmlPostRequest(uriInfo, servletRequest, httpHeaders, "", data);
   }

   @POST
   @Path("/{path:.*}")
   @Consumes(MediaType.TEXT_HTML)
   @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
   public Response htmlPostRequest(@Context UriInfo uriInfo, @Context HttpServletRequest servletRequest, @Context HttpHeaders httpHeaders, @PathParam("path") String path, InputStream data)
   {
      HttpManagedRequest request = post(data)
         .path(path)
         .servlet(servletRequest)
         .locale(httpHeaders)
         .parameters(uriInfo.getQueryParameters())
         .build();

      return executeRequest(uriInfo, request);
   }

   @PUT
   @Consumes(MediaType.TEXT_HTML)
   @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
   public Response htmlPutRequest(@Context UriInfo uriInfo, @Context HttpServletRequest servletRequest, @Context HttpHeaders httpHeaders, InputStream data)
   {
      return htmlPutRequest(uriInfo, servletRequest, httpHeaders, "", data);
   }

   @PUT
   @Path("/{path:.*}")
   @Consumes(MediaType.TEXT_HTML)
   @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
   public Response htmlPutRequest(@Context UriInfo uriInfo, @Context HttpServletRequest servletRequest, @Context HttpHeaders httpHeaders, @PathParam("path") String path, InputStream data)
   {
      HttpManagedRequest request = put(data)
         .path(path)
         .servlet(servletRequest)
         .locale(httpHeaders)
         .parameters(uriInfo.getQueryParameters())
         .build();

      return executeRequest(uriInfo, request);
   }

   @DELETE
   @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
   public Response htmlDeleteRequest(@Context UriInfo uriInfo, @Context HttpServletRequest servletRequest, @Context HttpHeaders httpHeaders)
   {
      return htmlDeleteRequest(uriInfo, servletRequest, httpHeaders, "");
   }

   @DELETE
   @Path("/{path:.*}")
   @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
   public Response htmlDeleteRequest(@Context UriInfo uriInfo, @Context HttpServletRequest servletRequest, @Context HttpHeaders httpHeaders, @PathParam("path") String path)
   {
      HttpManagedRequest request = delete()
         .path(path)
         .servlet(servletRequest)
         .locale(httpHeaders)
         .parameters(uriInfo.getQueryParameters())
         .build();

      return executeRequest(uriInfo, request);
   }

   //----------------------------------------- JSON Handlers -----------------------------------------//
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public Response jsonGetRequest(@Context UriInfo uriInfo, @Context HttpServletRequest servletRequest, @Context HttpHeaders httpHeaders)
   {
      return jsonGetRequest(uriInfo, servletRequest, httpHeaders, "");
   }

   @GET
   @Path("/{path:.*}")
   @Produces(MediaType.APPLICATION_JSON)
   public Response jsonGetRequest(@Context UriInfo uriInfo, @Context HttpServletRequest servletRequest, @Context HttpHeaders httpHeaders, @PathParam("path") String path)
   {
      HttpManagedRequest request = get()
         .path(path)
         .servlet(servletRequest)
         .locale(httpHeaders)
         .parameters(uriInfo.getQueryParameters())
         .contentType(ContentType.JSON)
         .build();

      return executeRequest(uriInfo, request);
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public Response jsonPostRequest(@Context UriInfo uriInfo, @Context HttpServletRequest servletRequest, @Context HttpHeaders httpHeaders, InputStream data)
   {
      return jsonPostRequest(uriInfo, servletRequest, httpHeaders, "", data);
   }

   @POST
   @Path("/{path:.*}")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public Response jsonPostRequest(@Context UriInfo uriInfo, @Context HttpServletRequest servletRequest, @Context HttpHeaders httpHeaders, @PathParam("path") String path, InputStream data)
   {
      HttpManagedRequest request = post(data)
         .path(path)
         .servlet(servletRequest)
         .locale(httpHeaders)
         .parameters(uriInfo.getQueryParameters())
         .contentType(ContentType.JSON)
         .build();

      return executeRequest(uriInfo, request);
   }

   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public Response jsonPutRequest(@Context UriInfo uriInfo, @Context HttpServletRequest servletRequest, @Context HttpHeaders httpHeaders, InputStream data)
   {
      return jsonPutRequest(uriInfo, servletRequest, httpHeaders, "", data);
   }

   @PUT
   @Path("/{path:.*}")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public Response jsonPutRequest(@Context UriInfo uriInfo, @Context HttpServletRequest servletRequest, @Context HttpHeaders httpHeaders, @PathParam("path") String path, InputStream data)
   {
      HttpManagedRequest request = put(data)
         .path(path)
         .servlet(servletRequest)
         .locale(httpHeaders)
         .parameters(uriInfo.getQueryParameters())
         .contentType(ContentType.JSON)
         .build();

      return executeRequest(uriInfo, request);
   }

   @DELETE
   @Produces(MediaType.APPLICATION_JSON)
   public Response jsonDeleteRequest(@Context UriInfo uriInfo, @Context HttpServletRequest servletRequest, @Context HttpHeaders httpHeaders)
   {
      return jsonDeleteRequest(uriInfo, servletRequest, httpHeaders, "");
   }

   @DELETE
   @Path("/{path:.*}")
   @Produces(MediaType.APPLICATION_JSON)
   public Response jsonDeleteRequest(@Context UriInfo uriInfo, @Context HttpServletRequest servletRequest, @Context HttpHeaders httpHeaders, @PathParam("path") String path)
   {
      HttpManagedRequest request = delete()
         .path(path)
         .servlet(servletRequest)
         .locale(httpHeaders)
         .parameters(uriInfo.getQueryParameters())
         .contentType(ContentType.JSON)
         .build();

      return executeRequest(uriInfo, request);
   }

   //----------------------------------------- XML Handlers -----------------------------------------//
   @GET
   @Produces(MediaType.APPLICATION_XML)
   public Response xmlGetRequest(@Context UriInfo uriInfo, @Context HttpServletRequest servletRequest, @Context HttpHeaders httpHeaders)
   {
      return xmlGetRequest(uriInfo, servletRequest, httpHeaders, "");
   }

   @GET
   @Path("/{path:.*}")
   @Produces(MediaType.APPLICATION_XML)
   public Response xmlGetRequest(@Context UriInfo uriInfo, @Context HttpServletRequest servletRequest, @Context HttpHeaders httpHeaders, @PathParam("path") String path)
   {
      HttpManagedRequest request = get()
         .path(path)
         .servlet(servletRequest)
         .locale(httpHeaders)
         .parameters(uriInfo.getQueryParameters())
         .contentType(ContentType.XML)
         .build();

      return executeRequest(uriInfo, request);
   }

   @POST
   @Consumes(MediaType.APPLICATION_XML)
   @Produces(MediaType.APPLICATION_XML)
   public Response xmlPostRequest(@Context UriInfo uriInfo, @Context HttpServletRequest servletRequest, @Context HttpHeaders httpHeaders, InputStream data)
   {
      return xmlPostRequest(uriInfo, servletRequest, httpHeaders, "", data);
   }

   @POST
   @Path("/{path:.*}")
   @Consumes(MediaType.APPLICATION_XML)
   @Produces(MediaType.APPLICATION_XML)
   public Response xmlPostRequest(@Context UriInfo uriInfo, @Context HttpServletRequest servletRequest, @Context HttpHeaders httpHeaders, @PathParam("path") String path, InputStream data)
   {
      HttpManagedRequest request = post(data)
         .path(path)
         .servlet(servletRequest)
         .locale(httpHeaders)
         .parameters(uriInfo.getQueryParameters())
         .contentType(ContentType.XML)
         .build();

      return executeRequest(uriInfo, request);
   }

   @PUT
   @Consumes(MediaType.APPLICATION_XML)
   @Produces(MediaType.APPLICATION_XML)
   public Response xmlPutRequest(@Context UriInfo uriInfo, @Context HttpServletRequest servletRequest, @Context HttpHeaders httpHeaders, InputStream data)
   {
      return xmlPutRequest(uriInfo, servletRequest, httpHeaders, "", data);
   }

   @PUT
   @Path("/{path:.*}")
   @Consumes(MediaType.APPLICATION_XML)
   @Produces(MediaType.APPLICATION_XML)
   public Response xmlPutRequest(@Context UriInfo uriInfo, @Context HttpServletRequest servletRequest, @Context HttpHeaders httpHeaders, @PathParam("path") String path, InputStream data)
   {
      HttpManagedRequest request = put(data)
         .path(path)
         .servlet(servletRequest)
         .locale(httpHeaders)
         .parameters(uriInfo.getQueryParameters())
         .contentType(ContentType.XML)
         .build();

      return executeRequest(uriInfo, request);
   }

   @DELETE
   @Produces(MediaType.APPLICATION_XML)
   public Response xmlDeleteRequest(@Context UriInfo uriInfo, @Context HttpServletRequest servletRequest, @Context HttpHeaders httpHeaders)
   {
      return xmlDeleteRequest(uriInfo, servletRequest, httpHeaders, "");
   }

   @DELETE
   @Path("/{path:.*}")
   @Produces(MediaType.APPLICATION_XML)
   public Response xmlDeleteRequest(@Context UriInfo uriInfo, @Context HttpServletRequest servletRequest, @Context HttpHeaders httpHeaders, @PathParam("path") String path)
   {
      HttpManagedRequest request = delete()
         .path(path)
         .servlet(servletRequest)
         .locale(httpHeaders)
         .parameters(uriInfo.getQueryParameters())
         .contentType(ContentType.XML)
         .build();

      return executeRequest(uriInfo, request);
   }

   //----------------------------------------- ZIP Handlers -----------------------------------------//
   @GET
   @Path("/{path:.*}")
   @Produces("application/zip")
   public Response zipGetRequest(@Context UriInfo uriInfo, @Context HttpServletRequest servletRequest, @Context HttpHeaders httpHeaders, @PathParam("path") String path)
   {
      HttpManagedRequest request = get()
         .path(path)
         .servlet(servletRequest)
         .locale(httpHeaders)
         .parameters(uriInfo.getQueryParameters())
         .operationName(OperationNames.EXPORT_RESOURCE)
         .contentType(ContentType.ZIP)
         .build();

      return executeRequest(uriInfo, request);
   }

   @PUT
   @Path("/{path:.*}")
   @Consumes("application/zip")
   @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
   public Response zipPutRequest(@Context UriInfo uriInfo, @Context HttpServletRequest servletRequest, @Context HttpHeaders httpHeaders, @PathParam("path") String path, InputStream data)
   {
      HttpManagedRequest request = put(data)
         .path(path)
         .servlet(servletRequest)
         .locale(httpHeaders)
         .parameters(uriInfo.getQueryParameters())
         .operationName(OperationNames.IMPORT_RESOURCE)
         .contentType(ContentType.ZIP)
         .build();

      return executeRequest(uriInfo, request);
   }

   //----------------------------------------- Private Handler -----------------------------------------//
   private Response executeRequest(UriInfo uriInfo, HttpManagedRequest request)
   {
      ContentType contentType = request.getContentType();
      if (contentType == null)
      {
         return Response.notAcceptable(Variant.mediaTypes(ContentTypeUtils.mediaTypes()).build()).build();
      }

      // Validate request
      Response response = validateRequest(request);
      if (response != null) return response;

      String operationName = request.getOperationName();
      PathAddress address = request.getAddress();
      try
      {
         ManagedResponse resp = controller.execute(request);
         if (resp == null)
         {
            return failure("No response returned.", operationName, Status.INTERNAL_SERVER_ERROR, contentType);
         }

         return success(uriInfo, resp, contentType);
      }
      catch (ResourceNotFoundException nfe)
      {
         if (log.isDebugEnabled()) // Don't want to log exceptions for wrong url's all the time.
         {
            log.error("Resource not found for address " + address, nfe);
         }
         return failure(nfe.getMessage(), operationName, Status.NOT_FOUND, contentType);
      }
      catch (OperationException e)
      {
         log.error("Operation exception for operation: " + operationName + ", address: " + address + ", content-type: " + contentType, e);
         if (e instanceof NotAuthorizedException)
         {
            return failure(e.getMessage(), operationName, Status.UNAUTHORIZED, contentType);
         }
         else
         {
            return failure(e.getMessage(), operationName, Status.INTERNAL_SERVER_ERROR, contentType);
         }
      }
      catch (Exception e)
      {
         String message = "Error processing operation: " + operationName + ", address: " + address + ", content-type: " + contentType;
         log.error(message, e);

         return failure(message, operationName, Status.INTERNAL_SERVER_ERROR, contentType);
      }
   }

   private Response failure(String failureDescription, String operationName, Status status, ContentType contentType)
   {
      if (contentType == null)
      {
         contentType = ContentType.JSON;
      }
      else if (contentType == ContentType.ZIP)
      {
         contentType = ContentType.JSON;
      }

      MediaType mediaType = ContentTypeUtils.getMediaType(contentType);
      return Response.status(status).entity(new FailureResult(failureDescription, operationName)).type(mediaType).build();
   }

   private Response success(UriInfo uriInfo, ManagedResponse response, ContentType contentType)
   {
      MediaType mediaType = ContentTypeUtils.getMediaType(contentType);

      Object result = response.getResult();
      if (result instanceof ReadResourceModel)
      {
         result = new Resource(uriInfo, (ReadResourceModel) result);
      }
      else if (result instanceof NoResultModel)
      {
         return Response.ok().type(mediaType).build();
      }
      else
      {
         return Response.ok(response).type(mediaType).build();
      }

      return Response.ok(result).type(mediaType).build();
   }

   private Response validateRequest(HttpManagedRequest request)
   {
      String operationName = request.getOperationName();
      String httpMethod = request.getHttpMethod();
      MediaType mediaType = ContentTypeUtils.getMediaType(request.getContentType());

      if (operationName.equals(OperationNames.READ_RESOURCE))
      {
         if (!httpMethod.equals(HttpMethod.GET))
         {
            return badRequest("Request must be a GET.", operationName, mediaType);
         }
      }
      else if (operationName.equals(OperationNames.ADD_RESOURCE))
      {
         if (!httpMethod.equals(HttpMethod.POST))
         {
            return badRequest("Request must be a POST.", operationName, mediaType);
         }
      }
      else if (operationName.equals(OperationNames.UPDATE_RESOURCE))
      {
         if (!httpMethod.equals(HttpMethod.PUT))
         {
            return badRequest("Request must be a POST.", operationName, mediaType);
         }
      }
      else if (operationName.equals(OperationNames.REMOVE_RESOURCE))
      {
         if (!httpMethod.equals(HttpMethod.DELETE))
         {
            return badRequest("Request must be a DELETE.", operationName, mediaType);
         }
      }

      return null;
   }

   private Response badRequest(String reason, String operationName, MediaType mediaType)
   {
      return Response.status(Status.BAD_REQUEST).entity(new FailureResult(reason, operationName)).type(mediaType).build();
   }
}
