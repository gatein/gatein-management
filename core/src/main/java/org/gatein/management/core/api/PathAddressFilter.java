package org.gatein.management.core.api;

import org.gatein.management.api.PathAddress;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public abstract class PathAddressFilter
{
   private static final String TEMPLATE_NAME_REGEX = "(\\w[\\w\\.-]*):(.*)";
   private static final Pattern TEMPLATE_REGEX = Pattern.compile(TEMPLATE_NAME_REGEX);

   public static PathAddressFilter parse(final List<String> filters) throws Exception
   {
      if (filters == null || filters.isEmpty()) return EVERYTHING;

      final List<Filter> filterList = new ArrayList<Filter>(filters.size());
      for (String filter : filters)
      {
         Matcher matcher = TEMPLATE_REGEX.matcher(filter);
         if (matcher.find())
         {
            String pathTemplate = matcher.group(1);
            String expression = matcher.group(2);
            filterList.add(new PathTemplateFilter(pathTemplate, expression));
         }
         else
         {
            filterList.add(new PathFilter(filter));
         }
      }

      return new PathAddressFilter()
      {
         @Override
         public boolean accept(PathAddress address)
         {
            boolean proceed = false;
            for (Filter filter : filterList)
            {
               boolean match = filter.matches(address);
               if (filter.isExclusion() && match)
               {
                  return false;
               }
               else if (match)
               {
                  proceed = true;
               }
            }

            return proceed;
         }
      };
   }

   public abstract boolean accept(PathAddress address);

   private PathAddressFilter(){}

   private static List<String> split(String string, String regex)
   {
      String[] split = string.split(regex);
      List<String> trimmed = new ArrayList<String>(split.length);
      for (String s : split)
      {
         if (s != null && !s.equals(""))
         {
            trimmed.add(s);
         }
      }

      return trimmed;
   }

   private static final PathAddressFilter EVERYTHING = new PathAddressFilter()
   {
      @Override
      public boolean accept(PathAddress address)
      {
         return true;
      }
   };

   private static interface Filter
   {
      boolean matches(PathAddress pathAddress);

      boolean isExclusion();
   }

   private static class PathTemplateFilter implements Filter
   {
      private String pathTemplate;
      private List<String> paths;
      private boolean exclusion;

      public PathTemplateFilter(String pathTemplate, String pathExpression)
      {
         this.pathTemplate = pathTemplate;
         exclusion = pathExpression.charAt(0) == '!';
         paths = (exclusion) ? split(pathExpression.substring(1), ",") : split(pathExpression, ",");
      }

      @Override
      public boolean matches(PathAddress pathAddress)
      {
         String resolved = pathAddress.resolvePathTemplate(pathTemplate);
         if (resolved != null)
         {
            for (String path : paths)
            {
               if (path.equals("*")) return true;

               if (resolved.equals(path))
               {
                  return true;
               }
            }
         }

         return false;
      }

      @Override
      public boolean isExclusion()
      {
         return exclusion;
      }
   }

   private static class PathFilter implements Filter
   {
      private List<String> paths;
      private boolean exclusion;

      public PathFilter(String pathExpression)
      {
         exclusion = pathExpression.charAt(0) == '!';
         paths = (exclusion) ? split(pathExpression.substring(1), ",") : split(pathExpression, ",");
      }

      @Override
      public boolean matches(PathAddress pathAddress)
      {
         for (String path : paths)
         {
            if (path.equals("*")) return true;

            if (path.charAt(0) != '/')
            {
               path = "/" + path;
            }
            if (pathAddress.toString().equals(path))
            {
               return true;
            }
         }

         return false;
      }

      @Override
      public boolean isExclusion()
      {
         return exclusion;
      }
   }
}
