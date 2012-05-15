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
import org.gatein.management.api.model.ModelProvider;
import org.gatein.management.api.model.ModelValue;
import org.gatein.management.core.api.model.DmrModelValue;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class ModelMapperMarshaller<T> implements Marshaller<T>
{
   private final ModelProvider.Mapper<T> mapper;
   private final Marshaller<ModelValue> marshaller = ModelValueMarshaller.INSTANCE;

   public ModelMapperMarshaller(ModelProvider.Mapper<T> mapper)
   {
      this.mapper = mapper;
   }

   @Override
   public void marshal(T object, OutputStream outputStream, boolean pretty) throws BindingException
   {
      marshaller.marshal(mapper.to(DmrModelValue.newModel(), object), outputStream, pretty);
   }

   @Override
   public T unmarshal(InputStream inputStream) throws BindingException
   {
      return mapper.from(marshaller.unmarshal(inputStream));
   }
}
