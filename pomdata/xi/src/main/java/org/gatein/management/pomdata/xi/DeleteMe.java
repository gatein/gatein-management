/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.gatein.management.pomdata.xi;

import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.pom.data.ApplicationData;
import org.exoplatform.portal.pom.data.ContainerData;
import org.exoplatform.portal.pom.data.NavigationData;
import org.exoplatform.portal.pom.data.NavigationNodeData;
import org.exoplatform.portal.pom.data.PageData;
import org.exoplatform.portal.pom.data.PortalData;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.gatein.management.pomdata.client.api.PomDataClient;

import java.net.InetAddress;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class DeleteMe
{
   public static void main(String[] args) throws Exception
   {
      PomDataClient client = PomDataClient.Factory.create(InetAddress.getByName("localhost"), 8080, "portal");

//      PortalData data = client.getPortalConfig("group", "/platform/administrators");

      //PortalData data = client.getPortalConfig("group", "/platform/users");

      /*PageData data = client.getPage("portal", "classic", "homepage");
      if (data != null)
      {
         ContainerData c1 = (ContainerData) data.getChildren().get(0);
         ContainerData c2 = (ContainerData) c1.getChildren().get(1);
         ContainerData c3 = (ContainerData) c2.getChildren().get(0);

         System.out.println(c3.getWidth());
         System.out.println(c3.getHeight());
         ApplicationData application = (ApplicationData) c3.getChildren().get(0);
         System.out.println(application.getWidth());
         System.out.println(application.getHeight());
      }
      else
      {
         System.out.println("Not found.");
      }*/
      PortalData data = client.getPortalConfig("portal", "classic");
      System.out.println(data.getPortalLayout());
   }
}
