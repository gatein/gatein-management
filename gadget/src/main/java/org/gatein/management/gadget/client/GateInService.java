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

package org.gatein.management.gadget.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.ui.SuggestOracle;

import java.util.List;

/**
 * {@code GateInService}
 * <p/>
 * Created on Jan 3, 2011, 12:28:43 PM
 *
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 * @version 1.0
 */
public interface GateInService extends RemoteService
{

   /**
    * Update the Tree item asynchronously
    *
    * @param containerName name of portal container
    * @param item          The item to be updated
    * @return the updated tree node
    * @throws Exception
    */
   public TreeNode updateItem(String containerName, TreeNode item) throws Exception;

   /**
    * Lookup for the user site having the given username
    *
    * @param containerName the portal container name
    * @param username      the user name
    * @return the tree node containing information about the user site (if exists)
    * @throws Exception
    */
   public TreeNode getUserSite(String containerName, String username) throws Exception;

   /**
    * Retrieve the list of usernames according to the user input
    *
    * @param containerName the portal container name
    * @param request       the user request
    * @return a response with the relevant usernames
    * @throws Exception
    */
   public SuggestOracle.Response getUsername(String containerName, SuggestOracle.Request request) throws Exception;

   /**
    * Retrieve asynchronously the list of root nodes
    *
    * @param containerName The portal container name
    * @return The list of the root nodes
    * @throws Exception
    */
   public List<TreeNode> getRootNodes(String containerName) throws Exception;
}
