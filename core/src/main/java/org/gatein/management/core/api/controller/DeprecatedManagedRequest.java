package org.gatein.management.core.api.controller;

import org.gatein.management.api.ContentType;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.controller.ManagedRequest;
import org.gatein.management.api.operation.OperationNames;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Used to support deprecated READ_CONFIG_AS_XML request, hardcoding operation name and content type
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class DeprecatedManagedRequest implements ManagedRequest
{
   private final ManagedRequest request;

   public DeprecatedManagedRequest(ManagedRequest request)
   {
      this.request = request;
   }

   @Override
   public String getOperationName()
   {
      return OperationNames.READ_CONFIG;
   }

   @Override
   public PathAddress getAddress()
   {
      return request.getAddress();
   }

   @Override
   public Map<String, List<String>> getAttributes()
   {
      return request.getAttributes();
   }

   @Override
   public InputStream getDataStream()
   {
      return request.getDataStream();
   }

   @Override
   public ContentType getContentType()
   {
      return ContentType.XML;
   }

   @Override
   public Locale getLocale()
   {
      return request.getLocale();
   }

   @Override
   public void setLocale(Locale locale)
   {
      request.setLocale(locale);
   }
}
