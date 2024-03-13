/*
* Fetch delayed call forwarding settings.
*/

// Fetch the data from the server in an easily accessible format.
connection.fetchData("Meta_Subscriber_DelayedCallForwarding", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved delayed call forwarding settings");
  display("Delayed call forwarding subscribed: ", data.Subscribed);
  display("Delayed call forwarding enabled: ", data.Enabled);
  display("Time to wait until forward: ", data.NoReplyTime);
  display("Forwarding number: ", data.Number);
}

function fetchError(connection, error) {
  display("Failed to retrieve delayed call forwarding settings: ");
  errorHandler(connection, error);                          
}
