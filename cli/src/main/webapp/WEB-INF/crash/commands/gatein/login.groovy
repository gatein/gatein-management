import org.crsh.command.ScriptException
import org.gatein.management.api.ContentType
import org.gatein.management.api.PathAddress
import org.gatein.management.api.controller.ManagedRequest
import org.gatein.management.api.exceptions.ResourceNotFoundException

assertConnected = {
  if (session == null) throw new ScriptException("Not connected !");
};

execute = { String operationName, PathAddress pathAddress, ContentType contentType, Closure printResult ->
  assertConnected();

  if (controller == null) throw new ScriptException("Management controller not available.");

  begin();

  try
  {
    response = controller.execute(ManagedRequest.Factory.create(operationName, pathAddress, contentType));
    if (response == null) return "No response for path $address";

    if (response.outcome.isSuccess())
    {
      address = pathAddress;
      return printResult(response.result);
    }
    else
    {
      return "Operation failure: $response.outcome.failureDescription";
    }
  }
  catch (ResourceNotFoundException e)
  {
    return "No resource found for path '$pathAddress'";
  }
  catch (Exception e)
  {
    throw new ScriptException("Management request failed for operation $operationName and path '$pathAddress'", e);
  }
  finally
  {
    end();
  }
};

//execute = { String operationName, PathAddress pathAddress, Closure printResult ->
//  assertConnected();
//
//  execute(operationName, pathAddress, null, printResult);
//};