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

package org.gatein.management.gadget.server;

import com.google.gwt.user.client.ui.SuggestOracle.Request;
import com.google.gwt.user.client.ui.SuggestOracle.Response;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.portal.config.model.PortalConfig;
import org.gatein.management.gadget.client.GateInService;
import org.gatein.management.gadget.client.ItemSuggestion;
import org.gatein.management.gadget.client.TreeNode;
import org.gatein.management.gadget.server.util.PortalService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.gatein.management.gadget.server.ContainerRequestHandler.*;

/**
 * {@code GateInServiceImpl}
 * <p>
 * The {@code GateInService} remote servlet implementation.
 * </p>
 * Created on Jan 3, 2011, 12:30:45 PM
 *
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 * @version 1.0
 */
public class GateInServiceImpl extends RemoteServiceServlet implements GateInService
{

   /**
    * Create a new instance of {@code GateInServiceImpl}
    */
   public GateInServiceImpl()
   {
      super();
   }

   /**
    * Update the Tree item asynchronously
    *
    * @param containerName name of portal container
    * @param tn          The item to be updated
    * @return the updated tree node
    * @throws Exception
    */
   public TreeNode updateItem(String containerName, TreeNode tn)
   {
      String name = tn.getText();

      // TODO


//        doInRequest(portalContainer, new ContainerCallback<Void>() {
//            @Override
//            public Void doInContainer(ExoContainer container) {
//                return null;
//            }
//        });
      return tn;
   }

   /**
    * Retrieve asynchronously the list of root nodes
    *
    * @param containerName The portal container name
    * @return The list of the root nodes
    * @throws Exception
    */
   public List<TreeNode> getRootNodes(String containerName) throws Exception
   {

      return doInRequest(containerName, new ContainerCallback<List<TreeNode>>()
      {

         public List<TreeNode> doInContainer(ExoContainer container)
         {

            PortalService portalService = PortalService.create(container);
            Collection<PortalConfig> portalSites = portalService.getPortalConfigs(PortalConfig.PORTAL_TYPE);
            Collection<PortalConfig> groupSites = portalService.getPortalConfigs(PortalConfig.GROUP_TYPE);
            // create root nodes
            TreeNode portalNode = getRootNode(PortalConfig.PORTAL_TYPE, "Portal sites", portalSites);
            TreeNode groupNode = getRootNode(PortalConfig.GROUP_TYPE, "Group sites", groupSites);
            List<TreeNode> nodes = new ArrayList<TreeNode>();
            nodes.add(portalNode);
            nodes.add(groupNode);

            return nodes;
         }
      });
   }

   /**
    * Create a {@code TreeNode} and attach to it it's children
    *
    * @param type    the site type (ownerType)
    * @param name    the node name
    * @param configs the list of sites representing the sub-nodes
    * @return a {@code TreeNode}
    */
   private TreeNode getRootNode(String type, String name, Collection<PortalConfig> configs)
   {

      TreeNode tn = new TreeNode(name);
      tn.setType(type);
      for (PortalConfig pc : configs)
      {
         TreeNode child = new TreeNode(pc.getName());
         child.setType(type);
         child.setExportable(true);
         child.setSiteName(pc.getName());
         StringBuilder sb = new StringBuilder("<ul>");
         sb.append("<li> Name : ").append(pc.getName()).append("</li>");
         sb.append("<li> Type : ").append(pc.getType()).append("</li>");
         sb.append("<li> Skin : ").append(pc.getSkin()).append("</li>");
         sb.append("<li> Edit permission : ").append(pc.getEditPermission()).append("</li>");
         sb.append("<li> Access permissions : <ul>");
         for (String s : pc.getAccessPermissions())
         {
            sb.append("<li>").append(s).append("</li>");
         }
         sb.append("</ul></li></ul>");
         child.setNodeInfo(sb.toString());
         tn.addChild(child);
      }

      return tn;
   }

   /**
    * Retrieve the list of usernames according to the user input
    *
    * @param containerName the portal container name
    * @param request       the user request
    * @return a response with the relevant usernames
    * @throws Exception
    */
   public Response getUsername(String containerName, final Request request) throws Exception
   {

      return doInRequest(containerName, new ContainerCallback<Response>()
      {

         public Response doInContainer(ExoContainer container)
         {
            PortalService portalService = PortalService.create(container);
            String query = request.getQuery();
            List<String> users = portalService.getUsers(query);
            Response response = new Response();
            List<Suggestion> suggestions = new ArrayList<Suggestion>();
            for (String usr : users)
            {
               suggestions.add(new ItemSuggestion(usr));
            }
            response.setSuggestions(suggestions);

            return response;
         }
      });
   }

   /**
    * Lookup for the user site having the given username
    *
    * @param containerName the portal container name
    * @param username      the user name
    * @return the tree node containing information about the user site (if exists)
    * @throws Exception
    */
   public TreeNode getUserSite(String containerName, final String username) throws Exception
   {

      return doInRequest(containerName, new ContainerCallback<TreeNode>()
      {

         @Override
         public TreeNode doInContainer(ExoContainer container) throws Exception
         {
            PortalService portalService = PortalService.create(container);
            TreeNode node = new TreeNode();
            PortalConfig pc = portalService.getPortalConfig(PortalConfig.USER_TYPE, username);
            if (pc == null)
            {
               node.setText("User not found");
               node.setNodeInfo("No user with the username : " + username);
            }
            else
            {
               node.setText(pc.getName());
               node.setType(pc.getType());
               node.setExportable(true);
               node.setSiteName(pc.getName());
               StringBuilder sb = new StringBuilder("<ul>");
               sb.append("<li> Name : ").append(pc.getName()).append("</li>");
               sb.append("<li> Type : ").append(pc.getType()).append("</li>");
               sb.append("<li> Skin : ").append(pc.getSkin()).append("</li>");
               sb.append("<li> Edit permission : ").append(pc.getEditPermission()).append("</li>");
               sb.append("<li> Access permissions : <ul>");
               for (String s : pc.getAccessPermissions())
               {
                  sb.append("<li>").append(s).append("</li>");
               }
               sb.append("</ul></li></ul>");
               node.setNodeInfo(sb.toString());
            }

            return node;
         }
      });
   }
}
