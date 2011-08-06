package org.gatein.management.cli.crash.commands.scp;

import org.crsh.ssh.term.scp.SCPAction;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.api.ContentType;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.controller.ManagedRequest;
import org.gatein.management.api.controller.ManagementController;
import org.gatein.management.api.operation.OperationNames;

import javax.print.DocFlavor;
import java.io.IOException;
import java.io.InputStream;

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
   protected void execute(ManagementController controller, String path) throws Exception
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

               controller.execute(ManagedRequest.Factory.create(operationName, PathAddress.pathAddress(trim(path.split("/"))), inputStream, ContentType.ZIP));
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
