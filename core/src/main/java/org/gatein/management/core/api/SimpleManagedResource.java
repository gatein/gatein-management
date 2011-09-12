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

package org.gatein.management.core.api;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.api.ManagedDescription;
import org.gatein.management.api.ManagedResource;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.PathAddressIterator;
import org.gatein.management.api.PathTemplateResolver;
import org.gatein.management.api.exceptions.ManagementException;
import org.gatein.management.api.operation.OperationHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.PatternSyntaxException;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class SimpleManagedResource extends AbstractManagedResource
{
   private static final Logger log = LoggerFactory.getLogger(SimpleManagedResource.class);

   private volatile ConcurrentMap<String, SimpleManagedResource> children = new ConcurrentHashMap<String, SimpleManagedResource>();
   private volatile ConcurrentMap<String, OperationEntry> operations = new ConcurrentHashMap<String, OperationEntry>();

   protected final ManagedDescription description;

   public SimpleManagedResource(PathElement pathElement, AbstractManagedResource parent, ManagedDescription description)
   {
      super(pathElement, parent);
      this.description = description;
   }

   //------------------------------- Registration Methods -------------------------------//
   
   @Override
   public Registration registerSubResource(String name, ManagedDescription description)
   {
      if (name == null) throw new IllegalArgumentException("name is null");
      if (description == null) throw new IllegalArgumentException("description is null");

      if (name.startsWith("/")) name = name.substring(1, name.length());

      PathElement element;
      try
      {
         element = PathElement.pathElement(name);
      }
      catch (PatternSyntaxException e)
      {
         throw new ManagementException("Could not parse path template " + name, e);
      }

      SimpleManagedResource resource = new SimpleManagedResource(element, this, description);
      if (children.putIfAbsent(element.getValue(), resource) != null)
      {
         throw new IllegalArgumentException("Resource " + name + " already exists for path " + getPath());
      }

      return resource;
   }

   @Override
   public void registerOperationHandler(String operationName, OperationHandler operationHandler, ManagedDescription description)
   {
      registerOperationHandler(operationName, operationHandler, description, false);
   }

   @Override
   public void registerOperationHandler(String operationName, OperationHandler operationHandler, ManagedDescription description, boolean inherited)
   {
      if (operations.putIfAbsent(operationName, new OperationEntry(operationHandler, description, inherited)) != null)
      {
         throw new IllegalArgumentException("A handler is already registered for operation " + operationName + " at path " + getPath());
      }
   }

   //------------------------------- Resource Descriptions -------------------------------//

   @Override
   protected ManagedDescription getResourceDescription(PathAddressIterator iterator)
   {
      if (iterator.hasNext())
      {
         String name = iterator.next();
         AbstractManagedResource resource = findDescendant(iterator, name, new StringBuilder());

         return (resource != null) ? resource.getResourceDescription(iterator) : null;
      }
      else
      {
         return description;
      }
   }

   //------------------------------- Operation information -------------------------------//

   @Override
   protected OperationEntry getOperationEntry(PathAddressIterator iterator, String operationName)
   {
      if (operationName == null) return null;
      
      OperationEntry entry = operations.get(operationName);
      if (iterator.hasNext())
      {
         String name = iterator.next();
         AbstractManagedResource resource = findDescendant(iterator, name, new StringBuilder());

         return (resource != null) ? resource.getOperationEntry(iterator, operationName) : null;
      }
      else
      {
         if (entry == null)
         {
            AbstractManagedResource parent = this.parent;
            while (parent != null)
            {
               OperationEntry parentEntry = parent.getOperationEntry(PathAddressIterator.EMPTY, operationName);
               if (parentEntry != null && parentEntry.isInherited())
               {
                  entry = parentEntry;
                  break;
               }
               parent = parent.parent;
            }
         }

         return entry;
      }
   }

   @Override
   protected void getOperationEntries(PathAddressIterator iterator, Map<String, OperationEntry> entries)
   {
      if (iterator.hasNext())
      {
         String name = iterator.next();
         AbstractManagedResource resource = findDescendant(iterator, name, new StringBuilder());

         if (resource != null)
         {
            resource.getOperationEntries(iterator, entries);
         }
      }
      else
      {
         for (Map.Entry<String, OperationEntry> entry : operations.entrySet())
         {
            entries.put(entry.getKey(), entry.getValue());
         }

         AbstractManagedResource parent = this.parent;
         while (parent != null)
         {
            Map<String, OperationEntry> parentMap = new HashMap<String, OperationEntry>();
            parent.getOperationEntries(PathAddress.empty().iterator(), parentMap);
            for (Map.Entry<String, OperationEntry> entry : parentMap.entrySet())
            {
               if (entry.getValue().isInherited() && !entries.containsKey(entry.getKey()))
               {
                  entries.put(entry.getKey(), entry.getValue());
               }
            }
            
            parent = parent.parent;
         }
      }
   }
   
   //------------------------------- SubResource information -------------------------------//

   @Override
   protected AbstractManagedResource getSubResource(PathAddressIterator iterator)
   {
      if (iterator.hasNext())
      {
         String name = iterator.next();
         AbstractManagedResource resource = findDescendant(iterator, name, new StringBuilder());

         return (resource != null) ? resource.getSubResource(iterator) : null;
      }
      else
      {
         return this;
      }
   }

   @Override
   public ManagedResource getSubResource(String childName)
   {
      return children.get(childName);
   }

   @Override
   protected Set<String> getChildNames(PathAddressIterator iterator)
   {
      if (iterator.hasNext())
      {
         String name = iterator.next();
         AbstractManagedResource resource = findDescendant(iterator, name, new StringBuilder());

         return (resource == null) ? Collections.<String>emptySet() : resource.getChildNames(iterator);
      }
      else
      {
         return Collections.unmodifiableSet(new HashSet<String>(children.keySet()));
      }
   }

   //------------------------------- Private stuff -------------------------------//

   private AbstractManagedResource findDescendant(PathAddressIterator iterator, String childName, StringBuilder path)
   {
      AbstractManagedResource child = children.get(childName);

      while (child == null)
      {
         path.append("/").append(childName);
         child = findMatch(iterator, path);
         if (iterator.hasNext())
         {
            child = findDescendant(iterator, iterator.next(), path);
         }
         else
         {
            break;
         }
      }

      return child;
   }

   private AbstractManagedResource findMatch(PathAddressIterator iterator, final StringBuilder path)
   {
      for (final SimpleManagedResource resource : children.values())
      {
         if (resource.pathElement.matches(path.toString()))
         {
            PathAddressIterator pai = new PathAddressIterator(iterator.currentAddress());
            StringBuilder sb = new StringBuilder(path);
            while (pai.hasNext())
            {
               String next = pai.next();
               sb.append("/").append(next);
               if (resource.pathElement.matches(sb.toString()))
               {
                  AbstractManagedResource found = resource.findMatch(pai, new StringBuilder(next));
                  if (found == null)
                  {
                     path.append("/").append(next);
                     iterator.next();
                  }
                  else
                  {
                     break;
                  }
               }
               else
               {
                  break;
               }
            }

            iterator.originalAddress().addPathTemplateResolver(
               new PathTemplateResolverImpl(resource.pathElement, path.toString()));
            
            if (iterator.hasNext())
            {
               pai = new PathAddressIterator(iterator.currentAddress().copy());
               AbstractManagedResource subResource = resource.getSubResource(pai);
               if (subResource != null)
               {
                  PathAddress address = pai.originalAddress();
                  for (PathTemplateResolver resolver : address.getPathTemplateResolvers())
                  {
                     iterator.originalAddress().addPathTemplateResolver(resolver);
                  }
                  while(iterator.hasNext()) iterator.next();
                  return subResource;
               }
            }
            else
            {
               return resource;
            }
         }
      }

      return null;
   }

   private static class PathTemplateResolverImpl implements PathTemplateResolver
   {
      private PathElement pathElement;
      private String path;

      public PathTemplateResolverImpl(PathElement pathElement, String path)
      {
         this.pathElement = pathElement;
         this.path = path;
      }
      @Override
      public String resolve(String templateName)
      {
         return pathElement.resolve(templateName, path);
      }
   }
}
