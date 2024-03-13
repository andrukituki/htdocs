/*
* Terminate an Incoming Call
*/

if (("callId" in window) && callId)
{
  display("Terminating the call");
  // Terminate the call
  connection.terminateCall(callId, deviceId, terminateSuccess, terminateFailure);
}
else
{
  alert("No call to Terminate.  You must be subscribed to the Incoming " +
        "Call events and be receiving a call.");
}

function terminateSuccess(connection)
{
  display("Call Terminated");
}

function terminateFailure(connection, error)
{
  display("Failed to terminate the call, error: " + error);
}
