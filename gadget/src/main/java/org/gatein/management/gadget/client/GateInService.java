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
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.client.ui.SuggestOracle;

import java.util.List;

/**
 * {@code GateInService}
 * <p/>
 * Created on Jan 3, 2011, 12:28:43 PM
 *
 * @author Nabil Benothman
 * @version 1.0
 */
@RemoteServiceRelativePath("/gtnService")
public interface GateInService extends RemoteService
{

   /**
    * @param item
    * @throws Exception
    */
   public TreeNode updateItem(String containerName, TreeNode item) throws Exception;

   /**
    * @param username
    * @return
    */
   public TreeNode getUserSite(String containerName, String username) throws Exception;

   /**
    * @param req
    * @return
    */
   public SuggestOracle.Response getUsername(String containerName, SuggestOracle.Request request) throws Exception;

   /**
    * @param containerName
    * @return
    * @throws Exception
    */
   public List<TreeNode> getRootNodes(String containerName) throws Exception;
}
