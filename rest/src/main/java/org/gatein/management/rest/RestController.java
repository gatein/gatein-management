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
import org.gatein.management.api.controller.ManagedRequest;
import org.gatein.management.api.controller.ManagedResponse;
import org.gatein.management.api.controller.ManagementController;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.operation.OperationNames;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.Response.*;

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

   //------------------------------------- Custom Content Handlers -------------------------------------//
   @GET
   public Response customGetRequest(@Context UriInfo uriInfo)
   {
      return customGetRequest(uriInfo, "");
   }

   @GET
   @Path("/{path:.*}")
   public Response customGetRequest(@Context UriInfo uriInfo, @PathParam("path") String path)
   {
      String extension = ContentTypeUtils.getExtension(path);
      if (extension != null)
      {
         path = path.substring(0, path.lastIndexOf(extension)-1);
      }

      return getRequest(ContentTypeUtils.getContentType(uriInfo), path);
   }


   //----------------------------------------- XML Handlers -----------------------------------------//
   @GET
   @Produces(MediaType.APPLICATION_XML)
   public Response xmlGetRequest()
   {
      return xmlGetRequest("");
   }

   @GET
   @Path("/{path:.*}")
   @Produces(MediaType.APPLICATION_XML)
   public Response xmlGetRequest(@PathParam("path") String path)
   {
      return getRequest(ContentType.XML, path);
   }

   //----------------------------------------- JSON Handlers -----------------------------------------//
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public Response jsonGetRequest()
   {
      return jsonGetRequest("");
   }

   @GET
   @Path("/{path:.*}")
   @Produces(MediaType.APPLICATION_JSON)
   public Response jsonGetRequest(@PathParam("path") String path)
   {
      return getRequest(ContentType.JSON, path);
   }

   //----------------------------------------- JSON Handlers -----------------------------------------//
   @GET
   @Produces("application/zip")
   public Response zipGetRequest()
   {
      return jsonGetRequest("");
   }

   @GET
   @Path("/{path:.*}")
   @Produces("application/zip")
   public Response zipGetRequest(@PathParam("path") String path)
   {
      return getRequest(ContentType.ZIP, path);
   }

   //----------------------------------------- Private Impl -----------------------------------------//

   private Response getRequest(ContentType contentType, String path)
   {
      if (contentType == null)
      {
         return Response.notAcceptable(Variant.mediaTypes(ContentTypeUtils.mediaTypes()).build()).build();
      }

      String operationName = OperationNames.READ_RESOURCE;
      if (contentType == ContentType.ZIP)
      {
         operationName = OperationNames.EXPORT_RESOURCE;
      }

      final PathAddress address = PathAddress.pathAddress(trim(path.split("/")));
      try
      {
         ManagedResponse resp = controller.execute(ManagedRequest.Factory.create(operationName, address, contentType));
         if (resp == null)
         {
            return failure("No response returned for operation " + operationName, Status.INTERNAL_SERVER_ERROR, contentType);
         }

         return success(resp, contentType);
      }
      catch (ResourceNotFoundException nfe)
      {
         if (log.isDebugEnabled()) // Don't want to log exceptions for wrong url's all the time.
         {
            log.error("Resource not found for address " + address, nfe);
         }
         return failure(nfe.getMessage(), Status.NOT_FOUND, contentType);
      }
      catch (OperationException e)
      {
         String message = e.getMessage() + " for operation " + e.getOperationName();
         log.error("Operation exception for operation '" + operationName + "' and content-type: " + contentType, e);
         return failure(message, Status.INTERNAL_SERVER_ERROR, contentType);
      }
      catch (Exception e)
      {
         String message = "Error processing operation: " + operationName + " for address " + address;
         log.error(message, e);

         return failure(message, Status.INTERNAL_SERVER_ERROR, contentType);
      }
   }

   @POST
   @Path("/{path:.*}")
   public Response postRequest(@Context HttpHeaders headers, @PathParam("path") String path, @QueryParam("format") String format, InputStream data)
   {
      if (path == null) path = "";
      String operationName = "add-resource";

      //TODO: Clean this up
      MediaType tmpMediaType = null;
      if (path.endsWith(".xml"))
      {
         path = path.substring(0, path.lastIndexOf("."));
         tmpMediaType = MediaType.APPLICATION_XML_TYPE;
         operationName = "read-config-as-xml";
      }
      else if (path.endsWith(".json"))
      {
         path = path.substring(0, path.lastIndexOf("."));
         tmpMediaType = MediaType.APPLICATION_JSON_TYPE;
      }
      else if ("xml".equals(format))
      {
         tmpMediaType = MediaType.APPLICATION_XML_TYPE;
      }
      else if ("json".equals(format))
      {
         tmpMediaType = MediaType.APPLICATION_JSON_TYPE;
      }

      final MediaType mediaType;
      if (tmpMediaType != null)
      {
         if (headerSpecifiesMediaType(headers))
         {
            log.warn("Detected 'Accept' header in request (which dictates content negotiation) along with custom content negotiation (either as url extension or parameter).");
         }
         mediaType = tmpMediaType;
      }
      else
      {
         mediaType = null;
      }

      PathAddress address = PathAddress.pathAddress(trim(path.split("/")));

      System.out.println("Post address " + address);

      return Response.ok().build();
   }

   private boolean headerSpecifiesMediaType(HttpHeaders headers)
   {
      return headers.getRequestHeaders().containsKey(HttpHeaders.ACCEPT);
   }

   private static String[] trim(String[] array)
   {
      List<String> trimmed = new ArrayList<String>(array.length);
      for (String s : array)
      {
         if (s != null && !"".equals(s))
         {
            trimmed.add(s);
         }
      }

      return trimmed.toArray(new String[trimmed.size()]);
   }

   private Response failure(String failureDescription, Status status, ContentType contentType)
   {
      if (contentType == ContentType.ZIP)
      {
         contentType = ContentType.JSON;
      }

      MediaType mediaType = ContentTypeUtils.getMediaType(contentType);
      return Response.status(status).entity(new FailureResult(failureDescription)).type(mediaType).build();
   }

   private Response success(ManagedResponse response, ContentType contentType)
   {
      MediaType mediaType = ContentTypeUtils.getMediaType(contentType);
      return Response.ok(response.getResult()).type(mediaType).build();
   }
}
