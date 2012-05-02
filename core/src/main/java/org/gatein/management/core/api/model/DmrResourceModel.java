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

import org.gatein.management.api.model.ResourceModel;
import org.gatein.management.api.model.ResourceModelType;
import org.jboss.dmr.ModelNode;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class DmrResourceModel implements ResourceModel
{
   private final ModelNode modelNode;

   public DmrResourceModel()
   {
      this(new ModelNode());
   }

   private DmrResourceModel(final ModelNode modelNode)
   {
      this.modelNode = modelNode;
   }

   @Override
   public ResourceModel get(String name)
   {
      return new DmrResourceModel(modelNode.get(name));
   }

   @Override
   public ResourceModel get(String... names)
   {
      return new DmrResourceModel(modelNode.get(names));
   }

   @Override
   public ResourceModel get(int index)
   {
      return new DmrResourceModel(modelNode.get(index));
   }

   @Override
   public ResourceModel add()
   {
      return new DmrResourceModel(modelNode.add());
   }

   @Override
   public ResourceModel add(int value)
   {
      add().set(value);
      return this;
   }

   @Override
   public ResourceModel add(boolean value)
   {
      modelNode.add(value);
      return this;
   }

   @Override
   public ResourceModel add(double value)
   {
      modelNode.add(value);
      return this;
   }

   @Override
   public ResourceModel add(long value)
   {
      modelNode.add(value);
      return this;
   }

   @Override
   public ResourceModel add(String value)
   {
      modelNode.add(value);
      return this;
   }

   @Override
   public ResourceModel add(ResourceModel value)
   {
      modelNode.add(((DmrResourceModel) value).modelNode);
      return this;
   }

   @Override
   public ResourceModel set(int value)
   {
      modelNode.set(value);
      return this;
   }

   @Override
   public ResourceModel set(String name, int value)
   {
      get(name).set(value);
      return this;
   }

   @Override
   public ResourceModel set(boolean value)
   {
      modelNode.set(value);
      return this;
   }

   @Override
   public ResourceModel set(String name, boolean value)
   {
      get(name).set(value);
      return this;
   }

   @Override
   public ResourceModel set(double value)
   {
      modelNode.set(value);
      return this;
   }

   @Override
   public ResourceModel set(String name, double value)
   {
      get(name).set(value);
      return this;
   }

   @Override
   public ResourceModel set(long value)
   {
      modelNode.set(value);
      return this;
   }

   @Override
   public ResourceModel set(String name, long value)
   {
      get(name).set(value);
      return this;
   }

   @Override
   public ResourceModel set(String value)
   {
      modelNode.set(value);
      return this;
   }

   @Override
   public ResourceModel set(String name, String value)
   {
      get(name).set(value);
      return this;
   }

   @Override
   public ResourceModel set(ResourceModel value)
   {
      modelNode.set(((DmrResourceModel) value).modelNode);
      return this;
   }

   @Override
   public ResourceModel set(String name, ResourceModel value)
   {
      get(name).set(value);
      return this;
   }

   @Override
   public ResourceModel set(Collection<ResourceModel> values)
   {
      if (values == null) throw new IllegalArgumentException("values is null");
      List<ModelNode> nodes = new ArrayList<ModelNode>(values.size());
      for (ResourceModel value : values)
      {
         nodes.add(((DmrResourceModel) value).modelNode);
      }

      modelNode.set(nodes);
      return this;
   }

   @Override
   public ResourceModel set(String name, Collection<ResourceModel> values)
   {
      get(name).set(values);
      return this;
   }

   @Override
   public Set<String> keys()
   {
      return modelNode.keys();
   }

   @Override
   public ResourceModelType getModelType()
   {
      switch (modelNode.getType())
      {
         case LIST:
            return ResourceModelType.LIST;
         case OBJECT:
            return ResourceModelType.OBJECT;
         case BIG_DECIMAL:
         case BIG_INTEGER:
         case BOOLEAN:
         case BYTES:
         case DOUBLE:
         case INT:
         case LONG:
         case STRING:
            return ResourceModelType.VALUE;
         case UNDEFINED:
            return ResourceModelType.UNDEFINED;
         default:
            throw new IllegalStateException("No mapping for model type " + modelNode.getType());
      }
   }

   @Override
   public String asString(boolean pretty)
   {
      return modelNode.toJSONString(!pretty);
   }

   @Override
   public void write(PrintWriter writer, boolean pretty)
   {
      modelNode.writeJSONString(writer, !pretty);
   }

   @Override
   public String toString()
   {
      return asString(true);
   }

   /*
   public String toXml(String rootElement, boolean pretty)
   {
      StringWriter sw = new StringWriter();
      writeXml(new PrintWriter(sw), rootElement, pretty);
      return sw.toString();
   }


   public void writeXml(PrintWriter writer, String rootElement, boolean pretty)
   {
      writer.append("<").append(rootElement).append(">");
      formatXml(writer, modelNode, 0, pretty);
      writer.append("</").append(rootElement).append(">");
   }

   private static void formatXml(PrintWriter writer, ModelNode node, int level, boolean pretty)
   {
      if (node.getType() == ModelType.LIST)
      {
         for (ModelNode item : node.asList())
         {
            if (pretty) nl(writer, level+1);
            writer.append("<item>");
            formatXml(writer, item, level+1, pretty);
            writer.append("</item>");
         }
         if (pretty) nl(writer, level);
      }
      else if (node.getType() == ModelType.OBJECT)
      {
         for (String key : node.keys())
         {
            if (pretty) nl(writer, level+1);

            writer.append("<").append(key);
            if (node.get(key).getType() == ModelType.LIST)
            {
               writer.append(" list=\"true\">");
            }
            else
            {
               writer.append(">");
            }
            formatXml(writer, node.get(key), level+1, pretty);
            writer.append("</").append(key).append(">");
         }
         if (pretty) nl(writer, level);
      }
      else if (node.getType() == ModelType.UNDEFINED)
      {
      }
      else if (node.getType() != ModelType.EXPRESSION || node.getType() != ModelType.PROPERTY)
      {
         writer.append(node.asString());
      }
      else
      {
         throw new RuntimeException("Unsupported model type " + node.getType());
      }
   }

   private static void indent(PrintWriter writer, int indent)
   {
      for (int i=0; i<indent; i++)
      {
         writer.append("    ");
      }
   }

   private static void nl(PrintWriter writer, int indent)
   {
      writer.append('\n');
      indent(writer, indent);
   }
   */
}
