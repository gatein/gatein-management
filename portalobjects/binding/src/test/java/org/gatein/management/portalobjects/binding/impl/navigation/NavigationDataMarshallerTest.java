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

package org.gatein.management.portalobjects.binding.impl.navigation;

import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.pom.data.NavigationData;
import org.exoplatform.portal.pom.data.NavigationNodeData;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class NavigationDataMarshallerTest
{
   @Test
   public void testNavigationUnmarshalling()
   {
      NavigationDataMarshaller marshaller = new NavigationDataMarshaller();
      NavigationData data = marshaller.unmarshal(getClass().getResourceAsStream("/navigation.xml"));
      assertNotNull(data);
      assertEquals(111, data.getPriority());
      assertNotNull(data.getNodes());
      assertEquals(6, data.getNodes().size());
      NavigationNodeData node = data.getNodes().get(0);
      verifyNode(node, "home", "#{portal.classic.home}", "home", Visibility.DISPLAYED, "portal::classic::homepage", null, null, null, 1);
      node = node.getNodes().get(0);
      Date start = createDate(2011, 1, 10, 12, 13, 55);
      Date end = createDate(2011, 1, 17, 17, 14, 0);
      verifyNode(node, "home-1", "Home 1", "home/home-1", Visibility.TEMPORAL, null, start, end, "StarAward", 1);
      node = node.getNodes().get(0);
      verifyNode(node, "empty", "Empty", "home/home-1/empty", Visibility.HIDDEN, "portal::classic::empty-page", null, null, null, 0);

      node = data.getNodes().get(5);
      verifyNode(node, "notfound", "NotFound", "notfound", Visibility.SYSTEM, null, null, null, null, 0);
   }

   @Test
   public void testEmptyNavigationUnmarshalling()
   {
      NavigationDataMarshaller marshaller = new NavigationDataMarshaller();
      NavigationData data = marshaller.unmarshal(getClass().getResourceAsStream("/empty-navigation.xml"));
      assertNotNull(data);
      assertEquals(3, data.getPriority());
      assertNotNull(data.getNodes());
      assertTrue(data.getNodes().isEmpty());
   }

   @Test
   public void testNavigationMarshalling()
   {
      Calendar startCal = Calendar.getInstance();
      startCal.set(Calendar.MILLISECOND, 0);
      Date start = startCal.getTime();
      Calendar endCal = Calendar.getInstance();
      endCal.set(Calendar.MILLISECOND, 0);
      Date end = endCal.getTime();

      NavigationNodeData expectedChild = new NavigationNodeData("node/node-1", "Node 1-1", "Icon-1", "node-1",
         null, null, null, null, Collections.<NavigationNodeData>emptyList());
      List<NavigationNodeData> children = Collections.singletonList(expectedChild);
      NavigationNodeData expectedNode = new NavigationNodeData("node", "Node", "Icon", "node", start, end, Visibility.DISPLAYED, "page-ref", children);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      NavigationDataMarshaller marshaller = new NavigationDataMarshaller();
      NavigationData expected = new NavigationData("", "", 123, Collections.singletonList(expectedNode));
      marshaller.marshal(expected, baos);

      NavigationData actual = marshaller.unmarshal(new ByteArrayInputStream(baos.toByteArray()));

      assertNotNull(actual);
      assertEquals("", actual.getOwnerType());
      assertEquals("", actual.getOwnerId());
      assertEquals(expected.getPriority(), actual.getPriority());
      assertNotNull(expected.getNodes());
      assertEquals(expected.getNodes().size(), actual.getNodes().size());

      NavigationNodeData actualNode = actual.getNodes().get(0);
      compareNode(expectedNode, actualNode);

      assertNotNull(actualNode.getNodes());
      assertEquals(expectedNode.getNodes().size(), actualNode.getNodes().size());
      compareNode(expectedChild, actualNode.getNodes().get(0));
   }

   private void verifyNode(NavigationNodeData node, String name, String label, String uri, Visibility visibility,
                           String pageRef, Date start, Date end, String icon, int children)
   {
      assertNotNull(node);
      assertEquals(name, node.getName());
      assertEquals(label, node.getLabel());
      assertEquals(uri, node.getURI());
      assertEquals(visibility, node.getVisibility());
      assertEquals(pageRef, node.getPageReference());
      assertEquals(start, node.getStartPublicationDate());
      assertEquals(end, node.getEndPublicationDate());
      assertEquals(icon, node.getIcon());
      assertNotNull(node.getNodes());
      assertEquals(children, node.getNodes().size());
   }

   private void compareNode(NavigationNodeData expected, NavigationNodeData actual)
   {
      assertEquals(expected.getURI(), actual.getURI());
      assertEquals(expected.getLabel(), actual.getLabel());
      assertEquals(expected.getIcon(), actual.getIcon());
      assertEquals(expected.getName(), actual.getName());
      assertEquals(expected.getStartPublicationDate(), actual.getStartPublicationDate());
      assertEquals(expected.getEndPublicationDate(), actual.getEndPublicationDate());
      assertEquals(expected.getVisibility(), actual.getVisibility());
      assertEquals(expected.getPageReference(), actual.getPageReference());
      assertEquals(expected.getNodes().size(), actual.getNodes().size());
   }

   private Date createDate(int year, int month, int day, int hour, int minute, int seconds)
   {
      Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("US/Eastern"));
      cal.set(Calendar.YEAR, year);
      cal.set(Calendar.MONTH, month-1);
      cal.set(Calendar.DAY_OF_MONTH, day);
      cal.set(Calendar.HOUR_OF_DAY, hour);
      cal.set(Calendar.MINUTE, minute);
      cal.set(Calendar.SECOND, seconds);
      cal.set(Calendar.MILLISECOND, 0);
      
      return cal.getTime();
   }
}
