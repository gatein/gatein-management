/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.gatein.management.core.api.binding.json;

import org.gatein.management.api.binding.BindingException;
import org.gatein.management.api.binding.Marshaller;
import org.gatein.management.api.model.ResourceModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class ResourceModelMarshaller implements Marshaller<ResourceModel>
{
   @Override
   public void marshal(ResourceModel object, OutputStream outputStream) throws BindingException
   {
      PrintWriter writer = new PrintWriter(outputStream);
      try
      {
         object.write(new PrintWriter(outputStream), true);
         writer.flush();
      }
      catch (IOException e)
      {
         throw new BindingException(e);
      }
      finally {
         writer.close();
      }
   }

   @Override
   public ResourceModel unmarshal(InputStream inputStream) throws BindingException
   {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
   }
}
