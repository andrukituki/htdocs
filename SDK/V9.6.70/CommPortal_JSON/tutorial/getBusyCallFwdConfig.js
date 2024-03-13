/*
* Fetch the busy call forwarding settings.
*/

// Fetch the data from the server in an easily accessible format.
connection.fetchData("Meta_Subscriber_BusyCallForwarding", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved busy call forwarding settings");
  display("Subscribed to busy call forwarding: ", data.Subscribed);
  display("Busy call forwarding enabled: ", data.Enabled);
  display("Forwarding number: ", data.Number);
}

function fetchError(connection, error) {
  display("Failed to retrieve busy call forwarding settings: ");
  errorHandler(connection, error);
}


