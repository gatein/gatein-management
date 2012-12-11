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

package org.gatein.management.core.api.model;

import org.gatein.management.api.model.Model;
import org.gatein.management.api.model.ModelBoolean;
import org.gatein.management.api.model.ModelList;
import org.gatein.management.api.model.ModelNumber;
import org.gatein.management.api.model.ModelObject;
import org.gatein.management.api.model.ModelReference;
import org.gatein.management.api.model.ModelString;
import org.gatein.management.api.model.ModelValue;
import org.jboss.dmr.ModelNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public abstract class DmrModelValue implements ModelValue
{
   final ModelNode value;

   DmrModelValue(final ModelNode value)
   {
      this.value = value;
   }

   @Override
   public boolean isDefined()
   {
      return getValueType() != ModelValueType.UNDEFINED;
   }

   @Override
   public <T extends ModelValue> T asValue(Class<T> valueType)
   {
      if (valueType.isAssignableFrom(getClass()))
      {
         return valueType.cast(this);
      }
      else if (getValueType() == ModelValueType.UNDEFINED)
      {
         ModelValue mv;
         if (ModelReference.class.isAssignableFrom(valueType))
         {
            mv = new DmrModelReference(value);
         }
         else if (ModelObject.class.isAssignableFrom(valueType))
         {
            mv = new DmrModelObject(value);
         }
         else if (ModelList.class.isAssignableFrom(valueType))
         {
            mv = new DmrModelList(value);
         }
         else if (ModelString.class.isAssignableFrom(valueType))
         {
            mv = new DmrModelString(value);
         }
         else if (ModelNumber.class.isAssignableFrom(valueType))
         {
            mv = new DmrModelNumber(value);
         }
         else if (ModelBoolean.class.isAssignableFrom(valueType))
         {
            mv = new DmrModelBoolean(value);
         }
         else
         {
            throw new IllegalArgumentException("No mapping defined for ModelValue type " + valueType);
         }

         return valueType.cast(mv);
      }
      else
      {
         ModelValue value = asValue(this.value);
         try
         {
            return valueType.cast(value);
         }
         catch (ClassCastException e)
         {
            throw new IllegalArgumentException("ModelValue type is " + value.getValueType() + " and cannot be cast to " + valueType.getName());
         }
      }
   }

   @Override
   public final ModelValueType getValueType()
   {
      return getValueType(value);
   }

   @Override
   public String toString()
   {
      return toJsonString(false);
   }

   @Override
   public String toJsonString(boolean pretty)
   {
      StringWriter sw = new StringWriter();
      toJson(new PrintWriter(sw), pretty);
      return sw.toString();
   }

   @Override
   public void toJsonStream(OutputStream outputStream, boolean pretty)
   {
      toJson(new PrintWriter(outputStream), pretty);
   }

   private void toJson(PrintWriter writer, boolean pretty)
   {
      value.writeJSONString(writer, !pretty);
      if (pretty) writer.write('\n');
      writer.flush();
   }

   @Override
   public ModelValue fromJsonString(String json)
   {
      return readFromJsonString(json);
   }

   @Override
   public <T extends ModelValue> T fromJsonString(String json, Class<T> valueType)
   {
      return readFromJsonString(json, valueType);
   }

   @Override
   public ModelValue fromJsonStream(InputStream inputStream) throws IOException
   {
      return readFromJsonStream(inputStream);
   }

   @Override
   public <T extends ModelValue> T fromJsonStream(InputStream inputStream, Class<T> valueType) throws IOException
   {
      return readFromJsonStream(inputStream, valueType);
   }

   public static ModelValue readFromJsonStream(InputStream inputStream) throws IOException
   {
      try
      {
         return asValue(ModelNode.fromJSONStream(inputStream));
      }
      catch (IOException e)
      {
         throw new IOException(e.getMessage());
      }
   }

   public static <T extends ModelValue> T readFromJsonStream(InputStream inputStream, Class<T> valueType) throws IOException
   {
      return valueType.cast(readFromJsonStream(inputStream));
   }

   public static ModelValue readFromJsonString(String json)
   {
      return asValue(ModelNode.fromJSONString(json));
   }

   public static <T extends ModelValue> T readFromJsonString(String json, Class<T> valueType)
   {
      return valueType.cast(readFromJsonString(json));
   }

   public static Model newModel()
   {
      return new DmrModel(new ModelNode());
   }

   @Override
   public int hashCode()
   {
      return value.hashCode();
   }

   @Override
   public boolean equals(Object other)
   {
      return (other instanceof DmrModelValue) && equals((DmrModelValue) other);
   }

   public boolean equals(DmrModelValue other)
   {
      return value.equals(other.value);
   }

   static ModelValue asValue(ModelNode value)
   {
      ModelValueType valueType = getValueType(value);
      switch (valueType)
      {
         case LIST:
            return new DmrModelList(value);
         case REFERENCE:
            return new DmrModelReference(value);
         case OBJECT:
            return new DmrModelObject(value);
         case BOOLEAN:
            return new DmrModelBoolean(value);
         case NUMBER:
            return new DmrModelNumber(value);
         case STRING:
            return new DmrModelString(value);
         case UNDEFINED:
            return new DmrModel(value);
         default:
            throw new AssertionError(valueType);
      }
   }

   private static ModelValueType getValueType(ModelNode value)
   {
      switch (value.getType())
      {
         case LIST:
            return ModelValueType.LIST;
         case OBJECT:
            if (DmrModelReference.isReference(value)) return ModelValueType.REFERENCE;

            return ModelValueType.OBJECT;
         case BOOLEAN:
            return ModelValueType.BOOLEAN;
         case BIG_DECIMAL:
         case BIG_INTEGER:
         case DOUBLE:
         case INT:
         case LONG:
            return ModelValueType.NUMBER;
         case STRING:
            return ModelValueType.STRING;
         case UNDEFINED:
            return ModelValueType.UNDEFINED;
         default:
            throw new IllegalStateException("No mapping for model type " + value.getType());
      }
   }
}
