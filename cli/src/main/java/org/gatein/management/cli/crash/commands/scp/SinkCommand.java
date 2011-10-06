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
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.api.ContentType;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.controller.ManagedRequest;
import org.gatein.management.api.controller.ManagementController;
import org.gatein.management.api.operation.OperationNames;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class SinkCommand extends SCPCommand
{
   private static final Logger log = LoggerFactory.getLogger(SinkCommand.class);

   protected SinkCommand(SCPAction action)
   {
      super(action);
   }

   @Override
   protected void execute(ManagementController controller, String path, Map<String, List<String>> attributes) throws Exception
   {
      ack();
      String line = readLine();
      while (true)
      {
         switch (line.charAt(0))
         {
            case 'C': // single file copy
               int mode = Integer.parseInt(line.substring(1, 5));
               final int length = Integer.parseInt(line.substring(6, line.indexOf(' ', 6)));
               if (length == 0) throw new Exception("Empty file.");

               String fileName = line.substring(line.indexOf(' ', 6)+1);
               if (fileName == null) throw new Exception("Invalid file name specified in protocol message: " + line);
               ack();

               String operationName = OperationNames.IMPORT_RESOURCE;
               if (fileName.endsWith(".xml"))
               {
                  throw new Exception("Xml files not supported for import.");
               }

               InputStream inputStream = new InputStream()
               {
                  /**
                   * How many we've read so far.
                   */
                  int count = 0;

                  @Override
                  public int read() throws IOException
                  {
                     if (count < length)
                     {
                        int value = in.read();
                        if (value == -1)
                        {
                           throw new IOException("Abnormal end of stream reached");
                        }
                        count++;
                        return value;
                     }
                     else
                     {
                        return -1;
                     }
                  }
               };

               controller.execute(ManagedRequest.Factory.create(operationName, PathAddress.pathAddress(path), attributes, inputStream, ContentType.ZIP));
               ack();
               readAck();
               
               return;
            
            case 'D': // recursive directory copy
            case 'E':
               throw new Exception("Recursive directory copy is not supported.");
            case 'T':
               throw new Exception("Modification and access times not supported.");
            default:
               throw new Exception("Unknown protocol message: " + line);
         }
      }
   }
}
