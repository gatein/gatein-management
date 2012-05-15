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

package org.gatein.management.cli.crash.commands.scp;

import org.crsh.ssh.term.scp.SCPAction;
import org.crsh.util.IO;
import org.gatein.management.api.ContentType;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.controller.ManagedRequest;
import org.gatein.management.api.controller.ManagedResponse;
import org.gatein.management.api.controller.ManagementController;
import org.gatein.management.api.operation.OperationNames;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class SourceCommand extends SCPCommand
{
   protected SourceCommand(SCPAction action)
   {
      super(action);
   }

   @Override
   protected void execute(ManagementController controller, String path, Map<String, List<String>> attributes) throws Exception
   {
      ManagedResponse response = getResponse(controller, path, attributes);

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      response.writeResult(outputStream, true);
      outputStream.flush();
      outputStream.close();

      out.write("C0644 ".getBytes());
      out.write(Integer.toString(outputStream.size()).getBytes());
      out.write(" ".getBytes());
      out.write(getFileName().getBytes());
      out.write("\n".getBytes());
      out.flush();

      readAck();
      IO.copy(new ByteArrayInputStream(outputStream.toByteArray()), out);
      ack();
      readAck();
   }

   private ManagedResponse getResponse(ManagementController controller, String path, Map<String, List<String>> attributes)
   {
      String operationName = OperationNames.EXPORT_RESOURCE;
      ContentType contentType = ContentType.ZIP;
      if (path.endsWith(".xml"))
      {
         operationName = OperationNames.READ_CONFIG;
         contentType = ContentType.XML;
         path = path.substring(0, path.lastIndexOf(".xml"));
      }
      else if (path.endsWith(".zip"))
      {
         path = path.substring(0, path.lastIndexOf(".zip"));
      }

      return controller.execute(ManagedRequest.Factory.create(
         operationName,
         PathAddress.pathAddress(path),
         attributes,
         contentType)
      );
   }
}
