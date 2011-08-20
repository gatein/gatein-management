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

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
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
   @Consumes({MediaType.TEXT_HTML, MediaType.APPLICATION_XHTML_XML})
   @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
   @RolesAllowed("administrators")
   public Response customGetRequest(@Context UriInfo uriInfo)
   {
      return customGetRequest(uriInfo, "");
   }

   @GET
   @Path("/{path:.*}")
   @Consumes({MediaType.TEXT_HTML, MediaType.APPLICATION_XHTML_XML})
   @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
   @RolesAllowed("administrators")
   public Response customGetRequest(@Context UriInfo uriInfo, @PathParam("path") String path)
   {
      String format = uriInfo.getQueryParameters().getFirst("format");
      ContentType contentType = ContentType.forName(format);

      if (contentType != null)
      {
         switch (contentType)
         {
            case XML:
               return xmlGetRequest(uriInfo, path);
            case JSON:
               return jsonGetRequest(uriInfo, path);
            default:
               return failure("Invalid query parameter: 'format=" + format +"'", OperationNames.READ_RESOURCE, Status.BAD_REQUEST, contentType);
         }
      }

      if (path.endsWith(".xml"))
      {
         return xmlGetRequest(uriInfo, path);
      }
      else if (path.endsWith(".zip"))
      {
         return zipGetRequest(uriInfo, path);
      }
      else
      {
         return getRequest(uriInfo, ContentType.JSON, path, OperationNames.READ_RESOURCE);
      }
   }

   //----------------------------------------- XML Handlers -----------------------------------------//
   @GET
   @Produces(MediaType.APPLICATION_XML)
   @RolesAllowed("administrators")
   public Response xmlGetRequest(@Context UriInfo uriInfo)
   {
      return xmlGetRequest(uriInfo, "");
   }

   @GET
   @Path("/{path:.*}")
   @Produces(MediaType.APPLICATION_XML)
   @RolesAllowed("administrators")
   public Response xmlGetRequest(@Context UriInfo uriInfo, @PathParam("path") String path)
   {
      String operationName = OperationNames.READ_RESOURCE;
      if (path.endsWith(".xml"))
      {
         path = path.substring(0, path.lastIndexOf(".xml"));
         operationName = OperationNames.READ_CONFIG_AS_XML;
      }

      return getRequest(uriInfo, ContentType.XML, path, operationName);
   }

   //----------------------------------------- JSON Handlers -----------------------------------------//
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @RolesAllowed("administrators")
   public Response jsonGetRequest(@Context UriInfo uriInfo)
   {
      return jsonGetRequest(uriInfo, "");
   }

   @GET
   @Path("/{path:.*}")
   @Produces(MediaType.APPLICATION_JSON)
   @RolesAllowed("administrators")
   public Response jsonGetRequest(@Context UriInfo uriInfo, @PathParam("path") String path)
   {
      String operationName = OperationNames.READ_RESOURCE;
      return getRequest(uriInfo, ContentType.JSON, path, operationName);
   }

   //----------------------------------------- ZIP Handlers -----------------------------------------//
   @GET
   @Path("/{path:.*}")
   @Produces("application/zip")
   @RolesAllowed("administrators")
   public Response zipGetRequest(@Context UriInfo uriInfo, @PathParam("path") String path)
   {
      if (path.endsWith(".zip"))
      {
         path = path.substring(0, path.lastIndexOf(".zip"));
      }
      return getRequest(uriInfo, ContentType.ZIP, path, OperationNames.EXPORT_RESOURCE);
   }

   @PUT
   @Path("/{path:.*}")
   @Consumes("application/zip")
   @RolesAllowed("administrators")
   public Response customPutRequest(@Context UriInfo uriInfo, @PathParam("path") String path, InputStream data)
   {
      ContentType contentType = ContentType.ZIP;
      String operationName = OperationNames.IMPORT_RESOURCE;

      PathAddress address = PathAddress.pathAddress(trim(path.split("/")));
      try
      {
         controller.execute(ManagedRequest.Factory.create(operationName, address, uriInfo.getQueryParameters(), data, contentType));

         return Response.ok().build();
      }
      //TODO: Wrap controller execution in a callback and reuse exception handling for all http methods
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
         return failure(e.getMessage(), operationName, Status.INTERNAL_SERVER_ERROR, contentType);
      }
      catch (Exception e)
      {
         String message = "Error processing operation: " + operationName + ", address: " + address + ", content-type: " + contentType;
         log.error(message, e);

         return failure(message, operationName, Status.INTERNAL_SERVER_ERROR, contentType);
      }
   }

   //----------------------------------------- Private Get Impl -----------------------------------------//
   private Response getRequest(UriInfo uriInfo, ContentType contentType, String path, String operationName)
   {
      if (contentType == null)
      {
         return Response.notAcceptable(Variant.mediaTypes(ContentTypeUtils.mediaTypes()).build()).build();
      }

      MultivaluedMap<String, String> parameters = uriInfo.getQueryParameters();
      String op = parameters.getFirst("op");
      if (op != null)
      {
         operationName = op;
         if (operationName.equals(OperationNames.READ_CONFIG_AS_XML))
         {
            contentType = ContentType.XML;
         }
         else if (operationName.equals(OperationNames.EXPORT_RESOURCE))
         {
            contentType = ContentType.ZIP;
         }
      }

      final PathAddress address = PathAddress.pathAddress(trim(path.split("/")));
      try
      {
         ManagedResponse resp = controller.execute(ManagedRequest.Factory.create(operationName, address, parameters, contentType));
         if (resp == null)
         {
            return failure("No response returned.", operationName, Status.INTERNAL_SERVER_ERROR, contentType);
         }

         return success(resp, contentType);
      }
      //TODO: Wrap controller execution in a callback and reuse exception handling for all http methods
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
         return failure(e.getMessage(), operationName, Status.INTERNAL_SERVER_ERROR, contentType);
      }
      catch (Exception e)
      {
         String message = "Error processing operation: " + operationName + ", address: " + address + ", content-type: " + contentType;
         log.error(message, e);

         return failure(message, operationName, Status.INTERNAL_SERVER_ERROR, contentType);
      }
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

   private Response success(ManagedResponse response, ContentType contentType)
   {
      MediaType mediaType = ContentTypeUtils.getMediaType(contentType);
      return Response.ok(response.getResult()).type(mediaType).build();
   }
}
