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

package org.gatein.management.pomdata.core.api;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.pom.data.ModelDataStorage;
import org.exoplatform.portal.pom.data.PortalData;
import org.exoplatform.portal.pom.data.PortalKey;
import org.gatein.management.ManagementException;
import org.gatein.management.pomdata.api.SiteManagementService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
//TODO: Add debugging
public class SiteManagementServiceImpl implements SiteManagementService
{
   private ModelDataStorage dataStorage;

   public SiteManagementServiceImpl(ModelDataStorage dataStorage)
   {
      this.dataStorage = dataStorage;
   }

   @Override
   public List<PortalData> getPortalData(String ownerType) throws ManagementException
   {
      Query<PortalData> query = new Query<PortalData>(ownerType, null, PortalData.class);
      try
      {
         LazyPageList<PortalData> results = dataStorage.find(query);

         List<PortalData> sites = new ArrayList<PortalData>(results.getAll());
         //TODO: Do we want sort on site name, or accept order from data storage
//         Collections.sort(sites, new Comparator<PortalData>()
//         {
//            @Override
//            public int compare(PortalData data1, PortalData data2)
//            {
//               return data1.getName().compareTo(data2.getName());
//            }
//         });
         return sites;

      }
      catch (Exception e)
      {
         throw new ManagementException("Could not get portal data for ownerType " + ownerType, e);
      }
   }

   @Override
   public PortalData getPortalData(String ownerType, String ownerId) throws ManagementException
   {
      try
      {
         return dataStorage.getPortalConfig(new PortalKey(ownerType, ownerId));
      }
      catch (Exception e)
      {
         throw new ManagementException("Could not get portal data for ownerType " + ownerType + " and ownerId " + ownerId, e);
      }
   }
}
