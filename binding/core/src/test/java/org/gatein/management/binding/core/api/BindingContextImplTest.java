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

package org.gatein.management.binding.core.api;

import org.gatein.management.binding.api.BindingException;
import org.gatein.management.binding.api.Marshaller;
import org.gatein.management.binding.core.api.mock.IntegerMockMarshaller;
import org.gatein.management.binding.core.api.mock.NoBindingsMarshaller;
import org.gatein.management.binding.core.api.mock.NumberMockMarshaller;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class BindingContextImplTest
{

   @Test
   public void testSimpleBinding()
   {
      Set<Class<? extends Marshaller>> classes = new LinkedHashSet<Class<? extends Marshaller>>();
      classes.add(IntegerMockMarshaller.class);

      BindingContextImpl<Integer> context = new BindingContextImpl<Integer>(Integer.class, classes);
      Marshaller<Integer> marshaller = context.createMarshaller();
      Assert.assertNotNull(marshaller);
      Assert.assertEquals(IntegerMockMarshaller.class, marshaller.getClass());
   }

   @Test
   public void testMultipleBindings()
   {
      Set<Class<? extends Marshaller>> classes = new LinkedHashSet<Class<? extends Marshaller>>();
      classes.add(IntegerMockMarshaller.class);
      classes.add(NumberMockMarshaller.class);

      BindingContextImpl<Integer> context = new BindingContextImpl<Integer>(Integer.class, classes);
      Marshaller<Integer> marshaller = context.createMarshaller();
      Assert.assertNotNull(marshaller);
      Assert.assertEquals(IntegerMockMarshaller.class, marshaller.getClass());

      BindingContextImpl<Double> context2 = new BindingContextImpl<Double>(Double.class, classes);
      Marshaller<Double> marshaller2 = context2.createMarshaller();
      Assert.assertNotNull(marshaller2);
      Assert.assertEquals(NumberMockMarshaller.class, marshaller2.getClass());

      BindingContextImpl<Integer> context3 = new BindingContextImpl<Integer>(Integer.class, classes);
      Marshaller<Integer> marshaller3 = context3.createMarshaller(NumberMockMarshaller.class);
      Assert.assertNotNull(marshaller3);
      Assert.assertEquals(NumberMockMarshaller.class, marshaller3.getClass());
   }

   // -- Some negative tests -- //

   @Test
   @SuppressWarnings("unchecked")
   public void testNullsInConstructor()
   {
      try
      {
         new BindingContextImpl(null, null);
         Assert.fail("Constructor must not accept null arguments.");
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         new BindingContextImpl(String.class, null);
         Assert.fail("Constructor must not accept null arguments.");
      }
      catch (IllegalArgumentException e)
      {
      }
   }
   
   @Test
   public void testNoBindingsAnnotation()
   {
      Set<Class<? extends Marshaller>> classes = new LinkedHashSet<Class<? extends Marshaller>>();
      classes.add(NoBindingsMarshaller.class);

      BindingContextImpl<String> context = new BindingContextImpl<String>(String.class, classes);
      try
      {
         context.createMarshaller();
         Assert.fail("Cannot create marshaller without Bindings annotation");
      }
      catch (BindingException e)
      {
         // pass
      }
   }
}
