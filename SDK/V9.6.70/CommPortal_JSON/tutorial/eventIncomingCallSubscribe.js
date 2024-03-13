/*
* Subscribe to Incoming Call events.
*/

display("Subscribing to Incoming Call Events");

callId = undefined;
deviceId = undefined;

// Subscribe to Incoming Call events
var error = connection.setIncomingCallHandler(incomingHandler);

if (error)
{
  display("Error: " + error);
}
else
{
  display("Info: You are subscribed to IncomingCall events, now try to make " +
          "an incoming phone call to see what happens and then terminate, " +
          "redirect the call or use the example to unsubscribe.");
}

function incomingHandler(connection, number, incomingcalldata)
{
  display("-----------------------------------");
  display("Incoming Call Handler");
  display("- Call state: " + incomingcalldata.callState);
  display("- Calling number: " + number);

  if (incomingcalldata.callState == CommPortal.INCOMINGCALLSTATE_RINGING)
  {
    display("- Caller number: " + incomingcalldata.callerNumber);
    // Set the callId and deviceId that will be used to terminate or redirect
    // the call
    callId = incomingcalldata.callID;
    deviceId = number;
  }
  else
  {
    callId = undefined;
  }

  display("- Call ID: " + incomingcalldata.callID);
  display("- Call type: " + incomingcalldata.callType);
}