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
   protected void execute(ManagementController controller, String path) throws Exception
   {
      ManagedResponse response = getResponse(controller, path);

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      response.writeResult(outputStream);
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

   private ManagedResponse getResponse(ManagementController controller, String path)
   {
      String operationName = OperationNames.EXPORT_RESOURCE;
      ContentType contentType = ContentType.ZIP;
      if (path.endsWith(".xml"))
      {
         operationName = OperationNames.READ_CONFIG_AS_XML;
         contentType = ContentType.XML;
         path = path.substring(0, path.lastIndexOf(".xml"));
      }
      else if (path.endsWith(".zip"))
      {
         path = path.substring(0, path.lastIndexOf(".zip"));
      }

      return controller.execute(ManagedRequest.Factory.create(
         operationName,
         PathAddress.pathAddress(trim(path.split("/"))),
         contentType)
      );
   }
}
