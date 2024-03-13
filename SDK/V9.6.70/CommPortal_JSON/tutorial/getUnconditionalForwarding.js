/*
* Fetch unconditonal call forwarding settings.
*/

// Fetch the data from the server in an easily accessible format.
connection.fetchData("Meta_Subscriber_UnconditionalCallForwarding", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved unconditional call forwarding settings");
  display("Subscribed to unconditional call forwarding: ", data.Subscribed)
  display("Unconditional call forwarding enabled: ", data.Enabled)
  display("Forwarding number: ", data.Number)
}

function fetchError(connection, error) {
  display("Failed to retrieve unconditional call forwarding settings: ");
  errorHandler(connection, error);                          
}
