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

package org.gatein.management.gadget.client;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

import java.io.Serializable;

/**
 * {@code ItemSuggestion}
 * <p/>
 * Created on Feb 10, 2011, 12:42:05 PM
 *
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 * @version 1.0
 */
public class ItemSuggestion implements IsSerializable, Suggestion, Serializable
{

   private String value;

   /**
    * Create a new instance of {@code ItemSuggestion}
    */
   public ItemSuggestion()
   {
      this("");
   }

   /**
    * Create a new instance of {@code ItemSuggestion}
    *
    * @param value
    */
   public ItemSuggestion(String value)
   {
      this.value = value;
   }

   /**
    * @return the string which will be displayed
    */
   public String getDisplayString()
   {
      return this.value;
   }

   /**
    * @return the string replacement
    */
   public String getReplacementString()
   {
      return this.value;
   }
}
