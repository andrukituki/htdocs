/*
* Fetch the auto-forwarding settings for voicemail and fax.
*/

// Fetch the data from the server in an easily accessible format.
connection.fetchData("Meta_Subscriber_MetaSphere_AutoForwarding", fetchSuccess, fetchError);

function fetchSuccess(connection, dataType, data) {
  display("Retrieved auto-forwarding settings");
  display("Forwarding enabled: ", data.ForwardingStatus);
  if (data.ForwardingAddresses.length > 0) {
    display("Forwarding addresses: ");
    display(data.ForwardingAddresses);
  } else {
    display("There are no forwarding addresses");
  }
  
}

function fetchError(connection, error) {
  display("Failed to retrieve auto-forwarding settings: ");
  errorHandler(connection, error);
}


