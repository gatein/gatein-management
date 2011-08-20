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
