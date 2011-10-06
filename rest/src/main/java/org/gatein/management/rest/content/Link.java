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

package org.gatein.management.rest.content;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
@XmlRootElement(name = "link")
@XmlType(propOrder = {"rel", "href", "type", "method"})
public class Link
{
   private String rel;
   private String href;
   private String type;
   private String method;

   public Link()
   {
   }

   public Link(String rel, String href, String type, String method)
   {
      this.rel = rel;
      this.href = href;
      this.type = type;
      this.method = method;
   }

   @XmlAttribute
   public String getRel()
   {

      return rel;
   }

   public void setRel(String rel)
   {
      this.rel = rel;
   }

   @XmlAttribute
   public String getHref()
   {
      return href;
   }

   public void setHref(String href)
   {
      this.href = href;
   }

   @XmlAttribute
   public String getType()
   {
      return type;
   }

   public void setType(String type)
   {
      this.type = type;
   }

   @XmlAttribute
   public String getMethod()
   {
      return method;
   }

   public void setMethod(String method)
   {
      this.method = method;
   }
}
