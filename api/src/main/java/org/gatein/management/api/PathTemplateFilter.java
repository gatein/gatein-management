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

package org.gatein.management.api;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a filter which can be applied to a PathAddress to determine if the address accepts the filter.
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 *
 * @see PathAddress#accepts(PathTemplateFilter)
 */
public abstract class PathTemplateFilter
{
   private static final String PATH_TEMPLATE_REGEX = "\\s*([\\w\\.-]*)\\s*:\\s*(!)?([^!]*)";
   private static final Pattern PATH_TEMPLATE_PATTERN = Pattern.compile(PATH_TEMPLATE_REGEX);

   private static final String WILDCARD = "*";

   abstract FilteredAddress filter(PathAddress address);

   public abstract boolean hasPathTemplate(String pathTemplateName);

   /**
    * Used to parse a attributes to a filter attribute format.
    *
    * <p>Filter attribute format: [pathTemplate]:(!)?[value,...value];[pathTemplate]:(!)?[value,...value] to exclude</p>
    * <p>Examples</p>
    * <ul>
    *    <li><tt>foo:bar</tt> Filter an address whose path template resolves to 'bar'.</li>
    *    <li><tt>foo:bar;bar:foo</tt> Filter an address with two path templates, short cut for adding it as another item in the list.</li>
    *    <li><tt>foo:foo-bar,foobar</tt> Filter an address whose path template resolves to either 'foo-bar', or 'foobar'.</li>
    *    <li><tt>foo:!foo-bar,foobar</tt> Filter an address to exclude path template's resolving to 'foo-bar' or 'foobar' </li>
    * </ul>
    *
    * @param attributes list of attribute expressions for filter. A null list or an empty list will return a filter that
    * will filter nothing.
    * @return newly created filter based on attributes
    * @throws ParseException if the attributes couldn't be parsed.
    */
   public static PathTemplateFilter parse(List<String> attributes) throws ParseException
   {
      if (attributes == null || attributes.isEmpty())
      {
         return NO_OP_FILTER;
      }

      Builder builder = null;
      for (String attribute : attributes)
      {
         for (String attr : PathAddress.split(attribute, ";"))
         {
            Matcher matcher = PATH_TEMPLATE_PATTERN.matcher(attr);
            if (matcher.matches())
            {
               String pathTemplate = matcher.group(1);
               if (builder == null)
               {
                  builder = create(pathTemplate);
               }
               else
               {
                  builder.and(pathTemplate);
               }

               boolean exclusion = (matcher.group(2) != null);
               List<String> expressions = PathAddress.split(matcher.group(3), ",");
               for (String expression : expressions)
               {
                  boolean wildcard = WILDCARD.equals(expression);
                  if (exclusion)
                  {
                     if (wildcard)
                     {
                        builder.excludeAll();
                     }
                     else
                     {
                        builder.exclude(expression);
                     }
                  }
                  else
                  {
                     if (wildcard)
                     {
                        builder.includeAll();
                     }
                     else
                     {
                        builder.include(expression);
                     }
                  }
               }
            }
            else
            {
               throw new ParseException("Filter attribute '" + attribute + "' is not valid.", 0);
            }
         }
      }

      return (builder == null) ? NO_OP_FILTER : builder.build();
   }

   /**
    * Create a <code>Builder</code> to build a filtering object.
    * @param pathTemplate path template to include in builder.
    * @return a <code>Builder</code>
    */
   public static Builder create(String pathTemplate)
   {
      return new BuilderImpl(pathTemplate);
   }

   private static class SimpleFilter extends PathTemplateFilter
   {
      private final List<Expression> expressions;

      public SimpleFilter(List<Expression> expressions)
      {
         this.expressions = expressions;
      }

      @Override
      FilteredAddress filter(final PathAddress address)
      {
         return new FilteredAddress()
         {
            @Override
            public boolean isFiltered()
            {
               for (Expression expression : expressions)
               {
                  boolean filtered = expression.resolves(address);
                  if (filtered) return true;
               }

               return false;
            }

            @Override
            public boolean matches()
            {
               //TODO: Clean this logic up.
               boolean overall = true;
               Set<String> matchedTemplates = new HashSet<String>();
               for (Expression expression : expressions)
               {
                  if (matchedTemplates.contains(expression.templateName)) continue;

                  boolean match = expression.match(address);
                  if (match && expression.filterType == FilterType.exclusion && expression.resolves(address))
                  {
                     return false;
                  }
                  else if (expression.filterType == FilterType.exclusion)
                  {
                     overall = true;
                  }
                  else if (match && expression.resolves(address))
                  {
                     matchedTemplates.add(expression.templateName);
                     overall = true;
                  }
                  else
                  {
                     overall = overall && match;
                  }
               }

               return overall;
            }
         };
      }

      @Override
      public boolean hasPathTemplate(String pathTemplateName)
      {
         for (Expression expression : expressions)
         {
            if (expression.templateName.equals(pathTemplateName)) return true;
         }

         return false;
      }

      @Override
      public boolean equals(Object o)
      {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;

         SimpleFilter that = (SimpleFilter) o;

         if (!expressions.equals(that.expressions)) return false;

         return true;
      }

      @Override
      public int hashCode()
      {
         return expressions.hashCode();
      }

      @Override
      public String toString()
      {
         return "SimpleFilter{" +
            "expressions=" + expressions +
            '}';
      }
   }

   private static final PathTemplateFilter NO_OP_FILTER = new PathTemplateFilter()
   {
      @Override
      FilteredAddress filter(PathAddress address)
      {
         return new FilteredAddress()
         {
            @Override
            public boolean isFiltered()
            {
               return true;
            }

            @Override
            public boolean matches()
            {
               return true;
            }
         };
      }

      @Override
      public boolean hasPathTemplate(String pathTemplateName)
      {
         return false;
      }
   };

   public static interface Builder
   {
      /**
       * Adds expressions to include in filter.
       * @param includes the expressions to include for the filter
       * @return a <code>FilterTypeBuilder</code>
       */
      FilterTypeBuilder include(String...includes);

      /**
       * Adds a wildcard expression that will include all items matching expression and path template (if applicable)
       * @return a <code>FilterTypeBuilder</code>
       */
      FilterTypeBuilder includeAll();

      /**
       * Adds expressions to exclude in filter.
       * @param excludes the expressions to exclude for the filter
       * @return a <code>FilterTypeBuilder</code>
       */
      FilterTypeBuilder exclude(String...excludes);

      /**
       * Adds a wildcard expression that will exclude all items matching expression and path template (if applicable)
       * @return a <code>FilterTypeBuilder</code>
       */
      FilterTypeBuilder excludeAll();

      /**
       * The finished filter to be applied to a <code>PathAddress</code>
       * @return a newly created filter based on the state of the builder
       */
      PathTemplateFilter build();

      /**
       * Continue building filter with another path template variable.
       *
       * @param pathTemplate path template variable name
       * @return a <code>Builder</code>
       */
      Builder and(String pathTemplate);
   }

   public static interface FilterTypeBuilder
   {
      /**
       * The finished filter to be applied to a <code>PathAddress</code>
       * @return a newly created filter based on the state of the builder
       */
      PathTemplateFilter build();

      /**
       * Continue building filter with another path template variable.
       *
       * @param pathTemplate path template variable name
       * @return a <code>Builder</code>
       */
      Builder and(String pathTemplate);
   }

//   public static interface IncludeFilterTypeBuilder extends FilterTypeBuilder
//   {
//      IncludeFilterTypeBuilder include(String...includes);
//
//      IncludeFilterTypeBuilder includeAll();
//   }
//
//   public static interface ExcludeFilterTypeBuilder extends FilterTypeBuilder
//   {
//      ExcludeFilterTypeBuilder exclude(String...includes);
//
//      ExcludeFilterTypeBuilder excludeAll();
//   }

   private static class BuilderImpl implements Builder, FilterTypeBuilder
   {
      private String pathTemplate;
      private List<Expression> expressions;

      public BuilderImpl(String pathTemplate)
      {
         this.pathTemplate = pathTemplate;
         expressions = new ArrayList<Expression>();
      }

      @Override
      public FilterTypeBuilder include(String... includes)
      {
         if (includes == null) throw new IllegalArgumentException("includes is null");
         addExpression(FilterType.inclusion, includes);
         return this;
      }

      @Override
      public FilterTypeBuilder includeAll()
      {
         addExpression(FilterType.inclusion, WILDCARD);
         return this;
      }

      @Override
      public FilterTypeBuilder excludeAll()
      {
         addExpression(FilterType.exclusion, WILDCARD);
         return this;
      }

      @Override
      public FilterTypeBuilder exclude(String... excludes)
      {
         if (excludes == null) throw new IllegalArgumentException("excludes is null");
         addExpression(FilterType.exclusion, excludes);
         return this;
      }

      @Override
      public PathTemplateFilter build()
      {
         return new SimpleFilter(new ArrayList<Expression>(expressions));
      }

      @Override
      public Builder and(String pathTemplate)
      {
         this.pathTemplate = pathTemplate;
         return this;
      }

      private void addExpression(FilterType filterType, String... values)
      {
         for (String value : values)
         {
            expressions.add(new Expression(pathTemplate, value, filterType));
         }
      }
   }

   private static class Expression
   {
      private final String templateName;
      private final String value;
      private final FilterType filterType;

      private Expression(String templateName, String value, FilterType filterType)
      {
         this.templateName = templateName;
         this.value = value;
         this.filterType = filterType;
      }

      public boolean match(PathAddress address)
      {
         if (value.equals("*")) return true;

         if (templateName == null)
         {
            String path = value;
            if (path.charAt(0) != '/') path = "/" + path;

            return path.equals(address.toString());
         }
         else
         {
            String resolved = address.resolvePathTemplate(templateName);
            if (resolved == null) return true; // we match because no path template matches

            String path = value;
            if (value.charAt(0) == '/') path = value.substring(1); // strip the leading slash because path template's don't match leading slashes

            return resolved.equals(path);
         }
      }

      public boolean resolves(PathAddress address)
      {
         return address.resolvePathTemplate(templateName) != null;
      }

      @Override
      public boolean equals(Object o)
      {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;

         Expression expression = (Expression) o;

         if (!value.equals(expression.value)) return false;
         if (filterType != expression.filterType) return false;
         if (!templateName.equals(expression.templateName)) return false;

         return true;
      }

      @Override
      public int hashCode()
      {
         int result = templateName.hashCode();
         result = 31 * result + value.hashCode();
         result = 31 * result + filterType.hashCode();
         return result;
      }

      @Override
      public String toString()
      {
         return "expression{" +
            "templateName='" + templateName + '\'' +
            ", value='" + value + '\'' +
            ", filterType=" + filterType +
            '}';
      }
   }

   public static enum FilterType
   {
      inclusion, exclusion
   }
}
