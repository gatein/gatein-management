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

import org.gatein.management.binding.api.BindingContext;
import org.gatein.management.binding.api.BindingException;
import org.gatein.management.binding.api.Marshaller;
import org.gatein.management.binding.core.api.mock.IntegerMockMarshaller;
import org.gatein.management.binding.core.api.mock.NoBindingsMarshaller;
import org.gatein.management.binding.core.api.mock.NumberMockMarshaller;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class BindingProviderImplTest
{
   @Test
   public void testOrderOfRegisteredMarshallers()
   {
      BindingProviderImpl provider = new BindingProviderImpl();
      provider.registerMarshaller(IntegerMockMarshaller.class);
      provider.registerMarshaller(NumberMockMarshaller.class);

      BindingContext<Integer> context = provider.createContext(Integer.class);
      Marshaller<Integer> marshaller = context.createMarshaller();

      Assert.assertNotNull(marshaller);
      Assert.assertEquals(NumberMockMarshaller.class, marshaller.getClass());


      provider = new BindingProviderImpl();
      provider.registerMarshaller(NumberMockMarshaller.class);
      provider.registerMarshaller(IntegerMockMarshaller.class);

      context = provider.createContext(Integer.class);
      marshaller = context.createMarshaller();

      Assert.assertNotNull(marshaller);
      Assert.assertEquals(IntegerMockMarshaller.class, marshaller.getClass());
   }

   @Test
   public void testNullMarshaller()
   {
      BindingProviderImpl provider = new BindingProviderImpl();
      try
      {
         provider.registerMarshaller(null);
         Assert.fail("Cannot register null marshaller.");
      }
      catch (BindingException e)
      {
      }
   }
   
   @Test
   public void testDupMarshallers()
   {
      BindingProviderImpl provider = new BindingProviderImpl();
      provider.registerMarshaller(IntegerMockMarshaller.class);
      try
      {
         provider.registerMarshaller(IntegerMockMarshaller.class);
         Assert.fail("Cannot register two marshallers of same class.");
      }
      catch (BindingException e)
      {
      }
   }

   @Test
   public void testNoBindings()
   {
      BindingProviderImpl provider = new BindingProviderImpl();
      provider.registerMarshaller(IntegerMockMarshaller.class);
      provider.registerMarshaller(NumberMockMarshaller.class);
      try
      {
         provider.registerMarshaller(NoBindingsMarshaller.class);
         Assert.fail("Cannot register marshaller without a Bindings annotation.");
      }
      catch (BindingException be)
      {
      }
      
      BindingContext<Integer> context = provider.createContext(Integer.class);
      Marshaller<Integer> marshaller = context.createMarshaller();

      Assert.assertNotNull(marshaller);
      Assert.assertEquals(NumberMockMarshaller.class, marshaller.getClass());
   }
}
