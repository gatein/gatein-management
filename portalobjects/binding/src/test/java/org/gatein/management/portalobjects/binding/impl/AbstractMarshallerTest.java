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

package org.gatein.management.portalobjects.binding.impl;

import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.pom.data.ApplicationData;
import org.exoplatform.portal.pom.data.BodyData;
import org.exoplatform.portal.pom.data.ComponentData;
import org.exoplatform.portal.pom.data.ContainerData;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.portlet.Preference;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public abstract class AbstractMarshallerTest
{
   protected void compareComponents(List<ComponentData> expectedComponents, List<ComponentData> actualComponents)
   {
      assertEquals(expectedComponents.size(), actualComponents.size());
      for (int i=0; i<expectedComponents.size(); i++)
      {
         ComponentData expected = expectedComponents.get(i);
         ComponentData actual = actualComponents.get(i);
         assertEquals(expected.getClass(), actual.getClass());

         if (expected instanceof ApplicationData)
         {
            compareApplicationData((ApplicationData) expected, (ApplicationData) actual);
         }
         else if (expected instanceof BodyData)
         {
            compareBodyData((BodyData) expected, (BodyData) actual);
         }
         else if (expected instanceof ContainerData)
         {
            compareContainerData((ContainerData) expected, (ContainerData) actual);
         }
      }
   }

   protected void compareContainerData(ContainerData expected, ContainerData actual)
   {
      assertNull(actual.getStorageId());
      assertNull(actual.getStorageName());
      assertNull(actual.getId());
      assertEquals(expected.getName(), actual.getName());
      assertEquals(expected.getIcon(), actual.getIcon());
      assertEquals(expected.getTemplate(), actual.getTemplate());
      assertEquals(expected.getFactoryId(), actual.getFactoryId());
      assertEquals(expected.getTitle(), actual.getTitle());
      assertEquals(expected.getDescription(), actual.getDescription());
      assertEquals(expected.getWidth(), actual.getWidth());
      assertEquals(expected.getHeight(), actual.getHeight());
      assertEquals(expected.getAccessPermissions(), actual.getAccessPermissions());

      compareComponents(expected.getChildren(), actual.getChildren());
   }

   protected void compareApplicationData(ApplicationData expected, ApplicationData actual)
   {
      assertNull(actual.getStorageId());
      assertNull(actual.getStorageName());
      assertEquals(expected.getType(), actual.getType());
      if (expected.getState() == null)
      {
         assertNull(actual.getState());
      }
      else
      {
         assertNotNull(actual.getState());
         compareApplicationState(actual.getState(), expected.getState());
      }

      assertNull(actual.getStorageId());
      assertNull(actual.getStorageName());
      assertNull(actual.getId());
      assertEquals(expected.getTitle(), actual.getTitle());
      assertEquals(expected.getIcon(), actual.getIcon());
      assertEquals(expected.getDescription(), actual.getDescription());
      assertEquals(expected.isShowInfoBar(), actual.isShowInfoBar());
      assertEquals(expected.isShowApplicationState(), actual.isShowApplicationState());
      assertEquals(expected.isShowApplicationMode(), actual.isShowApplicationMode());
      assertEquals(expected.getTheme(), actual.getTheme());
      assertEquals(expected.getWidth(), actual.getWidth());
      assertEquals(expected.getHeight(), actual.getHeight());
      assertEquals(expected.getProperties(), actual.getProperties());
      assertEquals(expected.getAccessPermissions(), actual.getAccessPermissions());
   }

   protected void compareApplicationState(ApplicationState expected, ApplicationState actual)
   {
      assertEquals(expected.getClass(), actual.getClass());
      if (expected instanceof TransientApplicationState)
      {
         TransientApplicationState expectedTas = (TransientApplicationState) expected;
         TransientApplicationState actualTas = (TransientApplicationState) actual;
         assertEquals(expectedTas.getContentId(), actualTas.getContentId());
         assertNull(actualTas.getOwnerType());
         assertNull(actualTas.getOwnerId());
         assertNull(actualTas.getUniqueId());
         if (expectedTas.getContentState() == null)
         {
            assertNull(actualTas.getContentState());
         }
         else
         {
            assertEquals(expectedTas.getContentState().getClass(), actualTas.getContentState().getClass());
            if (expectedTas.getContentState() instanceof Portlet)
            {
               comparePortlet((Portlet) expectedTas.getContentState(), (Portlet) actualTas.getContentState());
            }
         }
      }
   }

   protected void comparePortlet(Portlet expected, Portlet actual)
   {
      for (Preference expectedPref : expected)
      {
         Preference actualPref = actual.getPreference(expectedPref.getName());
         assertNotNull(actualPref);
         assertEquals(expectedPref.getName(), actualPref.getName());
         assertEquals(expectedPref.getValues(), actualPref.getValues());
         assertEquals(expectedPref.isReadOnly(), actualPref.isReadOnly());
      }
   }

   protected void compareBodyData(BodyData expected, BodyData actual)
   {
      assertNull(actual.getStorageId());
      assertNull(actual.getStorageName());
      assertEquals(expected.getType(), actual.getType());
   }
}
