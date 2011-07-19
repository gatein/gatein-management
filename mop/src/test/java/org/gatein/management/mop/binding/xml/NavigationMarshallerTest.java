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

package org.gatein.management.mop.binding.xml;

import org.exoplatform.portal.config.model.NavigationFragment;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.mop.Visibility;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class NavigationMarshallerTest
{
   @Test
   public void testNavigationUnmarshalling()
   {
      NavigationMarshaller marshaller = new NavigationMarshaller();
      PageNavigation data = marshaller.unmarshal(getClass().getResourceAsStream("/navigation.xml"));
      assertNotNull(data);
      assertEquals(111, data.getPriority());
      assertNotNull(data.getFragment());
      assertEquals(7, data.getFragment().getNodes().size());
      PageNode node = data.getFragment().getNodes().get(0);
      verifyNode(node, "home", "#{portal.classic.home}", "home", Visibility.DISPLAYED, "portal::classic::homepage", null, null, null, 1);
      node = node.getNodes().get(0);
      Date start = createDate(2011, 1, 10, 12, 13, 55);
      Date end = createDate(2011, 1, 17, 17, 14, 0);
      verifyNode(node, "home-1", "Home 1", "home/home-1", Visibility.TEMPORAL, null, start, end, "StarAward", 1);
      node = node.getNodes().get(0);
      verifyNode(node, "empty", "Empty", "home/home-1/empty", Visibility.HIDDEN, "portal::classic::empty-page", null, null, null, 0);

      node = data.getFragment().getNodes().get(5);
      verifyNode(node, "notfound", "NotFound", "notfound", Visibility.SYSTEM, null, null, null, null, 0);

      node = data.getFragment().getNodes().get(6);
      verifyNode(node, "n0", "n0", "n0", Visibility.DISPLAYED, "portal::classic::n0", null, null, null, 1);
      node = node.getNodes().get(0);
      verifyNode(node, "n0", "n0", "n0/n0", Visibility.DISPLAYED, "portal::classic::n0_n0", null, null, null, 10);
      for (int i=0; i<10; i++)
      {
         String name = "n" + i;
         String uri = "n0/n0/n" + i;
         String pageref = uri.replace("/", "_");

         PageNode child = node.getNodes().get(i);
         verifyNode(child, name, name, uri, Visibility.DISPLAYED, "portal::classic::" + pageref, null, null, null, 0);
      }
   }

   @Test
   public void testEmptyNavigationUnmarshalling()
   {
      NavigationMarshaller marshaller = new NavigationMarshaller();
      PageNavigation data = marshaller.unmarshal(getClass().getResourceAsStream("/empty-navigation.xml"));
      assertNotNull(data);
      assertEquals(3, data.getPriority());
      assertNotNull(data.getFragment().getNodes());
      assertTrue(data.getFragment().getNodes().isEmpty());
   }

   @Test
   public void testFragmentedNavigationUnmarshalling()
   {
      NavigationMarshaller marshaller = new NavigationMarshaller();
      PageNavigation data = marshaller.unmarshal(getClass().getResourceAsStream("/fragment-navigation.xml"));
      assertNotNull(data);
      assertNotNull(data.getFragments());
      assertEquals(2, data.getFragments().size());

      NavigationFragment fragment = data.getFragments().get(0);
      assertNotNull(fragment);
      assertEquals("home", fragment.getParentURI());
      assertNotNull(fragment.getNodes());
      assertEquals(1, fragment.getNodes().size());
      PageNode node = fragment.getNode("home-1");
      assertNotNull(node);
      assertNotNull(node.getNodes());
      assertEquals(2, node.getNodes().size());
      assertNotNull(node.getNode("home-1-1"));
      assertNotNull(node.getNode("home-1-2"));

      fragment = data.getFragments().get(1);
      assertNotNull(fragment);
      assertEquals("foo-bar", fragment.getParentURI());
      assertNotNull(fragment.getNodes());
      assertEquals(2, fragment.getNodes().size());
      assertNotNull(fragment.getNode("foo"));
      assertNotNull(fragment.getNode("bar"));
   }

   @Test
   public void testLocaleNavigationUnmarshalling()
   {
      NavigationMarshaller marshaller = new NavigationMarshaller();
      PageNavigation data = marshaller.unmarshal(getClass().getResourceAsStream("/locale-navigation.xml"));
      assertNotNull(data);
      assertNotNull(data.getFragment().getNodes());

      PageNode node = data.getFragment().getNode("hello-node");
      assertNotNull(node);
      assertNotNull(node.getLabels());
      assertEquals(8, node.getLabels().size());

      Locale locale = Locale.getDefault();

      String cn = "Dobrý den";
      String fr = "Bonjour";
      String en = "Hello";
      String es = "Hola";
      String ja = "こんにちは";
      String it = "Ciào";
      String zh = "你好";
      String zh_tw = "Li-ho";

      assertEquals(cn, node.getLabels().getExtended(locale).get(new Locale("cn")));
      assertEquals(fr, node.getLabels().getExtended(locale).get(new Locale("fr")));
      assertEquals(en, node.getLabels().getExtended(locale).get(new Locale("en")));
      assertEquals(es, node.getLabels().getExtended(locale).get(new Locale("es")));
      assertEquals(ja, node.getLabels().getExtended(locale).get(new Locale("ja")));
      assertEquals(it, node.getLabels().getExtended(locale).get(new Locale("it")));
      assertEquals(zh, node.getLabels().getExtended(locale).get(new Locale("zh")));
      assertEquals(zh_tw, node.getLabels().getExtended(locale).get(Locale.TAIWAN));

      node = data.getFragment().getNode("hello-node2");
      assertNotNull(node);
      assertNotNull(node.getLabels());
      assertEquals(8, node.getLabels().size());

      assertEquals(cn, node.getLabels().getExtended(locale).get(new Locale("cn")));
      assertEquals(fr, node.getLabels().getExtended(locale).get(new Locale("fr")));
      assertEquals(en, node.getLabels().getExtended(locale).get(new Locale("en")));
      assertEquals(es, node.getLabels().getExtended(locale).get(new Locale("es")));
      assertEquals(ja, node.getLabels().getExtended(locale).get(new Locale("ja")));
      assertEquals(it, node.getLabels().getExtended(locale).get(new Locale("it")));
      assertEquals(zh, node.getLabels().getExtended(locale).get(new Locale("zh")));
      assertEquals(zh_tw, node.getLabels().getExtended(locale).get(Locale.TAIWAN));
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

      PageNode expectedChild = newPageNode("Node 1-1", "Icon-1", "node-1",
         null, null, Visibility.DISPLAYED, null, new ArrayList<PageNode>());
      ArrayList<PageNode> children = new ArrayList<PageNode>(Collections.singletonList(expectedChild));
      PageNode expectedNode = newPageNode("Node", "Icon", "node", start, end, Visibility.HIDDEN, "page-ref", children);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      NavigationMarshaller marshaller = new NavigationMarshaller();
      PageNavigation expected = newPageNavigation("", "", 123, new ArrayList<PageNode>(Collections.singletonList(expectedNode)));
      marshaller.marshal(expected, baos);

      PageNavigation actual = marshaller.unmarshal(new ByteArrayInputStream(baos.toByteArray()));

      assertNotNull(actual);
      assertNull(actual.getOwnerType());
      assertNull(actual.getOwnerId());
      assertEquals(expected.getPriority(), actual.getPriority());
      assertNotNull(expected.getFragment().getNodes());
      assertEquals(expected.getFragment().getNodes().size(), actual.getFragment().getNodes().size());

      PageNode actualNode = actual.getFragment().getNodes().get(0);
      compareNode(expectedNode, actualNode);

      assertNotNull(actualNode.getNodes());
      assertEquals(expectedNode.getNodes().size(), actualNode.getNodes().size());
      compareNode(expectedChild, actualNode.getNodes().get(0));
   }

   private PageNavigation newPageNavigation(String ownerType, String ownerId, int priority, ArrayList<PageNode> children)
   {
      PageNavigation pageNavigation = new PageNavigation();
      pageNavigation.setOwnerType(ownerType);
      pageNavigation.setOwnerId(ownerId);
      pageNavigation.setPriority(priority);
      NavigationFragment fragment = new NavigationFragment();
      fragment.setNodes(children);
      pageNavigation.addFragment(fragment);

      return pageNavigation;
   }

   private void verifyNode(PageNode node, String name, String label, String uri, Visibility visibility,
                           String pageRef, Date start, Date end, String icon, int children)
   {
      assertNotNull(node);
      assertEquals(name, node.getName());
      assertEquals(label, node.getLabel());
      assertEquals(visibility, node.getVisibility());
      assertEquals(pageRef, node.getPageReference());
      assertEquals(start, node.getStartPublicationDate());
      assertEquals(end, node.getEndPublicationDate());
      assertEquals(icon, node.getIcon());
      assertNotNull(node.getNodes());
      assertEquals(children, node.getNodes().size());
   }

   private void compareNode(PageNode expected, PageNode actual)
   {
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

   private PageNode newPageNode(String name, String icon, String label, Date start, Date end, Visibility visibility, String pageref, ArrayList<PageNode> pageNodes)
   {
      PageNode pageNode = new PageNode();
      pageNode.setName(name);
      pageNode.setIcon(icon);
      pageNode.setLabel(label);
      pageNode.setStartPublicationDate(start);
      pageNode.setEndPublicationDate(end);
      pageNode.setVisibility(visibility);
      pageNode.setPageReference(pageref);
      pageNode.setChildren(pageNodes);

      return pageNode;
   }
}
