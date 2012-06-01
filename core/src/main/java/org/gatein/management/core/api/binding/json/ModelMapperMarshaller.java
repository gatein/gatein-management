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
import org.gatein.management.api.model.ModelList;
import org.gatein.management.api.model.ModelProvider;
import org.gatein.management.api.model.ModelValue;
import org.gatein.management.core.api.model.DmrModelValue;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class ModelMapperMarshaller<T> implements Marshaller<T>
{
   private final ModelProvider.ModelMapper<T> mapper;
   private final Marshaller<ModelValue> marshaller = ModelValueMarshaller.INSTANCE;

   public ModelMapperMarshaller(ModelProvider.ModelMapper<T> mapper)
   {
      this.mapper = mapper;
   }

   @Override
   @SuppressWarnings("unchecked")
   public void marshal(T object, OutputStream outputStream, boolean pretty) throws BindingException
   {
      if (Collection.class.isAssignableFrom(object.getClass()))
      {
         Collection<T> collection = (Collection<T>) object;
         ModelList list = DmrModelValue.newModel().setEmptyList();
         for (T t : collection)
         {
            mapper.to(list.add(), t);
         }
         marshaller.marshal(list, outputStream, pretty);
      }
      else
      {
         marshaller.marshal(mapper.to(DmrModelValue.newModel(), object), outputStream, pretty);
      }
   }

   @Override
   @SuppressWarnings("unchecked")
   public T unmarshal(InputStream inputStream) throws BindingException
   {
      ModelValue value = marshaller.unmarshal(inputStream);
      if (value.getValueType() == ModelValue.ModelValueType.LIST)
      {
         ModelList values = value.asValue(ModelList.class);
         List<T> list = new ArrayList<T>(values.size());
         for (ModelValue v : values)
         {
            list.add(mapper.from(v));
         }

         return (T) list;
      }
      else
      {
         return mapper.from(marshaller.unmarshal(inputStream));
      }
   }
}
