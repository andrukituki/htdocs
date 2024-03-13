/*
* Unsubscribe from Incoming Call events.
*/

display("Unsubscribing from Incoming Call Events");

// Unsubscribe from Incoming Call events
var error = connection.clearIncomingCallHandler();

if (error)
{
  // Got error when removing the event
  display("Error: " + error + " for " + error.events.join(","));
}