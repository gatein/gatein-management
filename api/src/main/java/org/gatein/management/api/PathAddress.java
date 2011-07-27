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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PathAddress implements Iterable<String>
{
   public static final PathAddress EMPTY_ADDRESS = new PathAddress(Collections.<String>emptyList());

   public static PathAddress empty()
   {
      return EMPTY_ADDRESS;
   }

   public static PathAddress pathAddress(String... paths)
   {
      if (paths == null) throw new IllegalArgumentException("paths is null");

      return new PathAddress(Arrays.asList(paths));
   }

   public static PathAddress pathAddress(String addressPath)
   {
      if (addressPath == null) throw new IllegalArgumentException("addressString is null");

      if (addressPath.charAt(0) == '/')
      {
         addressPath = addressPath.substring(1, addressPath.length());
      }

      return new PathAddress(Arrays.asList(addressPath.split("/")));
   }

   List<String> pathList;
   private List<PathTemplateResolver> resolvers;

   PathAddress(final List<String> pathList)
   {
      this(pathList, new ArrayList<PathTemplateResolver>());
   }

   PathAddress(final List<String> pathList, final List<PathTemplateResolver> resolvers)
   {
      this.pathList = pathList;
      this.resolvers = resolvers;
   }

   /**
    * Create a new PathAddress appending the path to the end of the path address
    * @param path path to append
    * @return new PathAddress with path appended
    */
   public PathAddress append(String path)
   {
      return append(PathAddress.pathAddress(path));
   }

   /**
    * Create a new PathAddress appending the address to this PathAddress
    * @param address the address to append
    * @return new PathAddress with appended address
    */
   public PathAddress append(PathAddress address)
   {
      List<String> list = new ArrayList<String>(pathList.size() + address.pathList.size());
      list.addAll(pathList);
      list.addAll(address.pathList);

      return new PathAddress(list, new ArrayList<PathTemplateResolver>(resolvers));
   }

   /**
    * Get a portion of this address using segments starting at {@code start} (inclusive).
    *
    * @param start the start index
    * @return the partial address
    */
   public PathAddress subAddress(int start)
   {
      return new PathAddress(pathList.subList(start, pathList.size()));
   }

   /**
    * Get a portion of this address using segments between {@code start} (inclusive) and {@code end} (exclusive).
    *
    * @param start the start index
    * @param end   the end index
    * @return the partial address
    */
   public PathAddress subAddress(int start, int end)
   {
      return new PathAddress(pathList.subList(start, end));
   }

   public String getLastElement()
   {
      return pathList.size() == 0 ? null : pathList.get(pathList.size() - 1);
   }

   public String get(int index)
   {
      return pathList.get(index);
   }

   public String resolvePathTemplate(String templateName)
   {
      for (PathTemplateResolver resolver : resolvers)
      {
         String resolved = resolver.resolve(templateName);
         if (resolved != null) return resolved;
      }

      return null;
   }

   public void addPathTemplateResolver(PathTemplateResolver resolver)
   {
      resolvers.add(resolver);
   }

   public List<PathTemplateResolver> getPathTemplateResolvers()
   {
      return Collections.unmodifiableList(resolvers);
   }

   @Override
   public PathAddressIterator iterator()
   {
      return new PathAddressIterator(this);
   }

   public int size()
   {
      return pathList.size();
   }

   public PathAddress copy()
   {
      return new PathAddress(new ArrayList<String>(pathList), new ArrayList<PathTemplateResolver>(resolvers));
   }

   @Override
   public boolean equals(Object o)
   {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      PathAddress that = (PathAddress) o;

      return pathList.equals(that.pathList);
   }

   @Override
   public int hashCode()
   {
      return pathList.hashCode();
   }

   @Override
   public String toString()
   {
      Iterator<String> iterator = this.iterator();
      StringBuilder sb = new StringBuilder();
      while (iterator.hasNext())
      {
         sb.append("/").append(iterator.next());
      }

      return sb.toString();
   }
}
