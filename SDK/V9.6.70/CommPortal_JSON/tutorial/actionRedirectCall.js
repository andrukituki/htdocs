/*
* Redirect an Incoming Call
*/

if (("callId" in window) && callId)
{
  // Get the new number to redirect the call
  var newNumber = prompt("New number", "");

  display("Redirecting the call");
  // Redirect the call
  connection.redirectCall(callId, deviceId, newNumber, redirectSuccess, redirectFailure);
}
else
{
  alert("No call to Redirect.  You must be subscribed to the Incoming " +
        "Call events and be receiving a call.");
}

function redirectSuccess(connection)
{
  display("Call Redirected");
}

function redirectFailure(connection, error)
{
  display("Failed to redirect the call, error: " + error);
}
