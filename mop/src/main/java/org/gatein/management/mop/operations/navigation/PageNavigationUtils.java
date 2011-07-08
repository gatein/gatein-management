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

package org.gatein.management.mop.operations.navigation;

import org.exoplatform.portal.config.model.LocalizedValue;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeModel;
import org.exoplatform.portal.mop.navigation.Scope;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PageNavigationUtils
{
   private PageNavigationUtils(){}

   public static PageNavigation loadPageNavigation(NavigationKey key, NavigationService navigationService, DescriptionService descriptionService)
   {
      NodeContext<NodeContext<?>> node;
      NavigationContext navigation = navigationService.loadNavigation(key.getSiteKey());
      if (navigation == null) return null;
      
      if (key.getNavUri() != null)
      {
         PathScope scope = new PathScope(key.getNavUri());
         node = navigationService.loadNode(NodeModel.SELF_MODEL, navigation, scope, null);
         if (scope.getFoundId() == null) return null;

         node = node.getDescendant(scope.getFoundId());
         return createFragmentedPageNavigation(descriptionService, navigation, node);
      }
      else
      {
         node = navigationService.loadNode(NodeModel.SELF_MODEL, navigation, Scope.ALL, null);
         if (node == null) return null;

         return createPageNavigation(descriptionService, navigation, node);
      }
   }

   public static PageNavigation createPageNavigation(DescriptionService service, NavigationContext navigation, NodeContext<NodeContext<?>> node)
   {
      PageNavigation pageNavigation = new PageNavigation();
      pageNavigation.setPriority(navigation.getState().getPriority());
      pageNavigation.setOwnerType(navigation.getKey().getTypeName());
      pageNavigation.setOwnerId(navigation.getKey().getName());

      ArrayList<PageNode> children = new ArrayList<PageNode>(node.getNodeCount());
      for (NodeContext<?> child : node.getNodes())
      {
         @SuppressWarnings("unchecked")
         NodeContext<NodeContext<?>> childNode = (NodeContext<NodeContext<?>>) child;
         children.add(createPageNode(service, childNode));
      }

      pageNavigation.setNodes(children);

      return pageNavigation;
   }

   private static PageNavigation createFragmentedPageNavigation(DescriptionService service, NavigationContext navigation, NodeContext<NodeContext<?>> node)
   {
      PageNavigation pageNavigation = new PageNavigation();
      pageNavigation.setPriority(navigation.getState().getPriority());
      pageNavigation.setOwnerType(navigation.getKey().getTypeName());
      pageNavigation.setOwnerId(navigation.getKey().getName());

      ArrayList<PageNode> children = new ArrayList<PageNode>(1);
      children.add(createPageNode(service, node));

      pageNavigation.setNodes(children);

      return pageNavigation;
   }

   private static PageNode createPageNode(DescriptionService service, NodeContext<NodeContext<?>> node)
   {
      PageNode pageNode = new PageNode();
      pageNode.setName(node.getName());

      if (node.getState().getLabel() == null)
      {
         Map<Locale, Described.State> descriptions = service.getDescriptions(node.getId());
         if (descriptions != null && !descriptions.isEmpty())
         {
            ArrayList<LocalizedValue> labels = new ArrayList<LocalizedValue>(descriptions.size());
            for (Map.Entry<Locale, Described.State> entry : descriptions.entrySet())
            {
               labels.add(new LocalizedValue(entry.getValue().getName(), entry.getKey()));
            }

            pageNode.setLabels(labels);
         }
      }
      else
      {
         pageNode.setLabel(node.getState().getLabel());
      }

      pageNode.setIcon(node.getState().getIcon());
      long startPublicationTime = node.getState().getStartPublicationTime();
      if (startPublicationTime != -1)
      {
         pageNode.setStartPublicationDate(new Date(startPublicationTime));
      }

      long endPublicationTime = node.getState().getEndPublicationTime();
      if (endPublicationTime != -1)
      {
         pageNode.setEndPublicationDate(new Date(endPublicationTime));
      }

      pageNode.setVisibility(node.getState().getVisibility());
      pageNode.setPageReference(node.getState().getPageRef());

      if (node.getNodes() != null)
      {
         ArrayList<PageNode> children = new ArrayList<PageNode>(node.getNodeCount());
         for (NodeContext<?> child : node.getNodes())
         {
            @SuppressWarnings("unchecked")
            NodeContext<NodeContext<?>> childNode = (NodeContext<NodeContext<?>>) child;
            children.add(createPageNode(service,  childNode));
         }

         pageNode.setChildren(children);
      }
      else
      {
         pageNode.setChildren(new ArrayList<PageNode>(0));
      }

      return pageNode;
   }
}
